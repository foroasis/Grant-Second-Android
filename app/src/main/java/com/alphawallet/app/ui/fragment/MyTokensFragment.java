package teleblock.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewRefreshRvBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;
import org.telegram.ui.LaunchActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.TokenBalance;
import teleblock.ui.adapter.MyTokensListAdapter;
import teleblock.ui.activity.MyWalletActivity;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * Time:2022/11/30
 * Author:Perry
 * Description：我的代币页面
 */
public class MyTokensFragment extends BaseFragment {

    private ViewRefreshRvBinding binding;
    private MyWalletActivity myWalletActivity;

    //钱包地址
    private String walletAddress;

    //链数据
    private Web3ConfigEntity.WalletNetworkConfigChainType chainData;

    private MyTokensListAdapter mMyTokensListAdapter;

    public static MyTokensFragment instance() {
        MyTokensFragment fragment = new MyTokensFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setParentFragment(MyWalletActivity parentFragment) {
        this.myWalletActivity = parentFragment;
    }

    /**
     * 每次切换链都要重新调用下这个方法
     *
     * @param chainData
     */
    public void setChainData(Web3ConfigEntity.WalletNetworkConfigChainType chainData) {
        this.chainData = chainData;
        obtionData();
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = ViewRefreshRvBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        chainData = MMKVUtil.currentChainConfig(true);
        //获取钱包地址
        walletAddress = WalletDaoUtils.getCurrent().getAddress();

        binding.refreshLayout.setEnableRefresh(false).setEnableLoadMore(false);
        binding.rv.setLayoutManager(new LinearLayoutManager(mActivity));

        mMyTokensListAdapter = new MyTokensListAdapter();
        binding.rv.setAdapter(mMyTokensListAdapter);

        mMyTokensListAdapter.setEmptyView(createEmptyView());
        mMyTokensListAdapter.getEmptyLayout().setVisibility(View.GONE);

        obtionData();
    }

    private View createEmptyView() {
        ViewTokenEmptyBinding binding = ViewTokenEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyToken.setText(LocaleController.getString("wallet_home_token_empty_text", R.string.wallet_home_token_empty_text));
        return binding.getRoot();
    }

    /**
     * 获取数据
     */
    private void obtionData() {
        if (getMyWalletActivity() == null) {
            return;
        }
        binding.rv.setTag(0);
        double[] totalAmount = {0.00};
        getMyWalletActivity().binding.flBalanceLoading.setVisibility(View.VISIBLE);
        mMyTokensListAdapter.setList(null);
        //获取代币列表数据
        List<Web3ConfigEntity.WalletNetworkConfigChainType> chainTypes = MMKVUtil.getWeb3ConfigData().getChainType();
        CollectionUtils.forAllDo(chainTypes, (index, item) -> {
            if (chainData != null && chainData.getId() != item.getId()) {
                return;
            }
            BlockFactory.get(item.getId()).getTokenList(walletAddress, new BlockCallback<List<TokenBalance>>() {
                @Override
                public void onSuccess(List<TokenBalance> data) {
                    for (TokenBalance tokenBalance : data) {
                        totalAmount[0] += tokenBalance.balanceUSD;
                    }
                    binding.rv.setTag((int) binding.rv.getTag() + 1);
                    mMyTokensListAdapter.addData(data);
                }

                @Override
                public void onEnd() {
                    if (chainData != null || (int) binding.rv.getTag() == chainTypes.size()) {
                        getMyWalletActivity().binding.tvWalletAccount.setText("$" + WalletUtil.bigDecimalScale(new BigDecimal(totalAmount[0]), 5));
                        getMyWalletActivity().stopBalanceLoading();
                        mMyTokensListAdapter.getEmptyLayout().setVisibility(mMyTokensListAdapter.getData().isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            });
        });
    }

    private MyWalletActivity getMyWalletActivity() {
        if (myWalletActivity != null) return myWalletActivity;
        LaunchActivity launchActivity = (LaunchActivity) getActivity();
        List<org.telegram.ui.ActionBar.BaseFragment> fragments = new ArrayList<>(launchActivity.getActionBarLayout().getFragmentStack());
        for (org.telegram.ui.ActionBar.BaseFragment fragment : fragments) {
            if (fragment instanceof MyWalletActivity) {
                return myWalletActivity = (MyWalletActivity) fragment;
            }
        }
        return null;
    }
}
