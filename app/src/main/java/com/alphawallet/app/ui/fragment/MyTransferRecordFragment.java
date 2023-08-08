package teleblock.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewRefreshRvBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.BlockchainConfig;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.TransferHistoryEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.TransferHistoryApi;
import teleblock.ui.adapter.MyTransferRecordAdapter;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * Time:2022/11/30
 * Author:Perry
 * Description：我的转账记录页面
 */
public class MyTransferRecordFragment extends BaseFragment {

    private ViewRefreshRvBinding binding;
    private org.telegram.ui.ActionBar.BaseFragment parentFragment;

    //转账记录适配器
    private MyTransferRecordAdapter mMyTransferRecordAdapter;

    //钱包地址
    private String walletAddress;

    //链数据
    private Web3ConfigEntity.WalletNetworkConfigChainType chainData;

    //页数
    private int page = 1;

    public static MyTransferRecordFragment instance() {
        MyTransferRecordFragment fragment = new MyTransferRecordFragment();
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
        chainData = MMKVUtil.currentChainConfig();
        //获取钱包地址
        walletAddress = WalletDaoUtils.getCurrent().getAddress();

        binding.refreshLayout.setEnableRefresh(false).setEnableLoadMore(false);
        binding.rv.setLayoutManager(new LinearLayoutManager(mActivity));

        mMyTransferRecordAdapter = new MyTransferRecordAdapter(walletAddress);
        mMyTransferRecordAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mMyTransferRecordAdapter.setEmptyView(createEmptyView());
        mMyTransferRecordAdapter.getEmptyLayout().setVisibility(View.GONE);
        binding.rv.setAdapter(mMyTransferRecordAdapter);

        //上拉加载
        mMyTransferRecordAdapter.getLoadMoreModule().setEnableLoadMore(true);
        mMyTransferRecordAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            obtionData(false);
        });

        requestTransferHistoryData();

        obtionData(true);
    }

    private View createEmptyView() {
        ViewTokenEmptyBinding binding = ViewTokenEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyToken.setText(LocaleController.getString("wallet_home_transaction_empty_text", R.string.wallet_home_transaction_empty_text));
        return binding.getRoot();
    }

    /**
     * 获取转账历史记录
     */
    private void requestTransferHistoryData() {
        EasyHttp.cancel("requestTransferHistoryData");
        EasyHttp.post(new ApplicationLifecycle())
                .tag("requestTransferHistoryData")
                .api(new TransferHistoryApi())
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<TransferHistoryEntity>>>() {
            @Override
            public void onSucceed(BaseBean<BaseLoadmoreModel<TransferHistoryEntity>> result) {
                if (!CollectionUtils.isEmpty(result.getData().getData())) {
                    mMyTransferRecordAdapter.setTransferHistoryEntityList(result.getData().getData());
                }
            }

            @Override
            public void onFail(Exception e) {
            }
        });
    }

    /**
     * 获取数据
     */
    private void obtionData(boolean ifRefresh) {
        if (mMyTransferRecordAdapter == null) {
            return;
        }
        if (ifRefresh) {
            page = 1;
            requestCoinPrice();
            binding.rv.post(() -> {
                mMyTransferRecordAdapter.setList(new ArrayList<>());
            });
        } else {
            page++;
        }

        //获取交易记录
        BlockFactory.get(getChainData().getId()).getTransactionsByAddress(walletAddress, page, new BlockCallback<List<TransactionInfo>>() {
            @Override
            public void onSuccess(List<TransactionInfo> data) {
                if (page == 1) {
                    mMyTransferRecordAdapter.setList(data);
                } else {
                    mMyTransferRecordAdapter.addData(data);
                }
                if (CollectionUtils.size(data) >= 10) {
                    mMyTransferRecordAdapter.getLoadMoreModule().loadMoreComplete();
                } else {
                    mMyTransferRecordAdapter.getLoadMoreModule().loadMoreEnd(true);
                }
                mMyTransferRecordAdapter.getEmptyLayout().setVisibility(mMyTransferRecordAdapter.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String msg) {
                if (ifRefresh) {
                    mMyTransferRecordAdapter.setList(new ArrayList<>());
                    mMyTransferRecordAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                } else {
                    mMyTransferRecordAdapter.getLoadMoreModule().loadMoreFail();
                }
                mMyTransferRecordAdapter.getLoadMoreModule().loadMoreComplete();
            }
        });
    }

    /**
     * 获取主币价格
     */
    private void requestCoinPrice() {
        Web3ConfigEntity.WalletNetworkConfigEntityItem mainCurrencyData = BlockchainConfig.getMainCurrency(getChainData());
        if (mainCurrencyData == null) {
            return;
        }

        WalletUtil.requestMainCoinPrice(mainCurrencyData.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
            @Override
            public void requestEnd() {
            }

            @Override
            public void requestError(String msg) {
            }

            @Override
            public void requestSuccessful(CurrencyPriceEntity resultData) {
                mMyTransferRecordAdapter.updateData(String.valueOf(resultData.getUsd()));
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
