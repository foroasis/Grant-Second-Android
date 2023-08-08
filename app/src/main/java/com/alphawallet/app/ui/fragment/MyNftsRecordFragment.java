package teleblock.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewNftEmptyBinding;
import org.telegram.messenger.databinding.ViewRefreshRvBinding;

import java.util.ArrayList;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTMetadata;
import teleblock.model.wallet.NFTResponse;
import teleblock.ui.activity.NFTDetailsActivity;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.widget.divider.CustomItemDecoration;

/**
 * Time:2022/12/2
 * Author:Perry
 * Description：我的nft列表
 */
public class MyNftsRecordFragment extends BaseFragment {

    private ViewRefreshRvBinding binding;
    private org.telegram.ui.ActionBar.BaseFragment parentFragment;

    //钱包地址
    private String walletAddress;

    //链数据
    private Web3ConfigEntity.WalletNetworkConfigChainType chainData;

    //分页用到的
    private String cursor = "";

    //nft data
    private NFTAssetsAdapter nftListAdapter;

    public static MyNftsRecordFragment instance() {
        MyNftsRecordFragment fragment = new MyNftsRecordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setParentFragment(org.telegram.ui.ActionBar.BaseFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    /**
     * 每次切换链都要重新调用下这个方法
     *
     * @param chainData
     */
    public void setChainData(Web3ConfigEntity.WalletNetworkConfigChainType chainData) {
        if (chainData==null) return;
        this.chainData = chainData;
        obtionData(true);
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = ViewRefreshRvBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        chainData = (Web3ConfigEntity.WalletNetworkConfigChainType) getArguments().getSerializable("chainData");
        //获取钱包地址
        walletAddress = WalletDaoUtils.getCurrent().getAddress();
        binding.refreshLayout.setEnableRefresh(false).setEnableLoadMore(false);
        binding.rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rv.addItemDecoration(new CustomItemDecoration(2, 20f, 20f, true));

        //初始化适配器
        nftListAdapter = new NFTAssetsAdapter();
        nftListAdapter.setEmptyView(createEmptyView());
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                NFTInfo nftInfo = nftListAdapter.getItem(position);
                parentFragment.presentFragment(new NFTDetailsActivity(nftInfo));
            }
        });
        binding.rv.setAdapter(nftListAdapter);

        nftListAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> obtionData(false));

        obtionData(true);
    }

    private View createEmptyView() {
        ViewNftEmptyBinding binding = ViewNftEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyNft.setText(LocaleController.getString("nft_assets_empty_text", R.string.nft_assets_empty_text));
        return binding.getRoot();
    }

    /**
     * 获取数据
     */
    private void obtionData(boolean refresh) {
        if (nftListAdapter == null) {
            return;
        }

        if (!refresh && TextUtils.isEmpty(cursor)) {
            nftListAdapter.getLoadMoreModule().loadMoreEnd(true);
            return;
        }

        if (refresh) {
            binding.rv.post(() -> {
                nftListAdapter.setList(new ArrayList<>());
            });
            cursor = "";
        }

        //获取nft列表数据
        BlockFactory.get(getChainData().getId()).getNftList(walletAddress, cursor, new BlockCallback<NFTResponse>() {
            @Override
            public void onProgress(int index, String data) {
                NFTInfo nftInfo = JsonUtil.parseJsonToBean(data, NFTInfo.class);
                if (nftInfo != null) {
                    nftListAdapter.setData(index, nftInfo);
                }
            }

            @Override
            public void onSuccess(NFTResponse data) {
                nftListAdapter.getLoadMoreModule().loadMoreComplete();
                if (CollectionUtils.isEmpty(data.assets)) {
                    nftListAdapter.getLoadMoreModule().loadMoreEnd(true);
                    if (refresh) {
                        nftListAdapter.setList(new ArrayList<>());
                    }
                } else {
                    if (data.assets.size() < 10) {
                        nftListAdapter.getLoadMoreModule().loadMoreEnd(true);
                    } else {
                        cursor = data.next;
                    }

                    if (refresh) {
                        nftListAdapter.setList(data.assets);
                    } else {
                        nftListAdapter.addData(data.assets);
                    }
                }

                nftListAdapter.getEmptyLayout().setVisibility(nftListAdapter.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String msg) {
                nftListAdapter.getLoadMoreModule().loadMoreComplete();
                if (refresh) {
                    nftListAdapter.setList(new ArrayList<>());
                    nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                } else {
                    nftListAdapter.getLoadMoreModule().loadMoreFail();
                }
            }
        });
    }

    private Web3ConfigEntity.WalletNetworkConfigChainType getChainData() {
        if (chainData == null) {
            chainData = MMKVUtil.currentChainConfig();
        }
        return chainData;
    }
}
