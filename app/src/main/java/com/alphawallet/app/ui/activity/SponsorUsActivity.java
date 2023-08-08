package teleblock.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivitySponsorusBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.BlockFactory;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.ui.SelectorSponsorPriceData;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.adapter.SelectorSponsorPriceAdp;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletTransferUtil;
import teleblock.widget.SpacesItemDecoration;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/9/21
 * Author:Perry
 * Description：打赏我们
 */
public class SponsorUsActivity extends BaseFragment {

    private ActivitySponsorusBinding binding;

    //选择的捐献金额数据
    private List<SelectorSponsorPriceData> selectorSponsorPriceDataList = new ArrayList<>();
    private SelectorSponsorPriceAdp mSelectorSponsorPriceAdp;

    //币种数据
    private long chainId;
    private Web3ConfigEntity.WalletNetworkConfigEntityItem ethCoinData;

    //我的钱包地址
    private String myWalletAddress;
    //官方收款地址
    private String toWalletAddress;

    //钱包余额
    private BigDecimal walletBalanceBigDecimal;
    //币种单价
    private BigDecimal coinPrice;

    //打赏金额
    private BigDecimal sponsorUsPrice;

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);//不显示actionbar
        binding = ActivitySponsorusBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        EventBus.getDefault().register(this);
        initData();
        return fragmentView;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initData() {
        mSelectorSponsorPriceAdp = new SelectorSponsorPriceAdp();

        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.001", "\uD83C\uDF6D", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.002", "\uD83C\uDF66", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.003", "\uD83C\uDF54", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.005", "\uD83E\uDD70", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.01", "\uD83E\uDD29", false));
        selectorSponsorPriceDataList.add(new SelectorSponsorPriceData("0.02", "\uD83D\uDCB8", false));

        //所有的数据
        List<Web3ConfigEntity.WalletNetworkConfigChainType> mWalletNetworkConfigChainTypeList = MMKVUtil.getWeb3ConfigData().getChainType();
        for (Web3ConfigEntity.WalletNetworkConfigChainType chainData : mWalletNetworkConfigChainTypeList) {
            if (1 == chainData.getId()) {
                chainId = chainData.getId();
                for (Web3ConfigEntity.WalletNetworkConfigEntityItem coinData : chainData.getCurrency()) {
                    if (coinData.isIs_main_currency()) {//获取eth主币
                        ethCoinData = coinData;
                    }
                }
            }
        }

        if (ethCoinData == null) {
            return;
        }

        //官方钱包地址
        toWalletAddress = MMKVUtil.getSystemMsg().wallet_address;

        getETHWalletInfoData();

        initView();
    }

    private void initView() {
        binding.ivBack.setOnClickListener(view -> finishFragment());
        binding.tvTips.setText(LocaleController.getString("sponsorus_tips", R.string.sponsorus_tips));
        binding.tvCoinType.setText(ethCoinData.getName());
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), ethCoinData.getIcon(), drawable -> binding.tvCoinType.getHelper().setIconNormalLeft(drawable));
        binding.tvCustomquantity.setText(LocaleController.getString("sponsorus_customquantity", R.string.sponsorus_customquantity));
        binding.tvSponsorus.setText(LocaleController.getString("sponsorus_sure", R.string.sponsorus_sure));
        binding.tvLoading.setText(LocaleController.getString("Loading", R.string.Loading));

        binding.rvPrice.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.rvPrice.addItemDecoration(new SpacesItemDecoration(3, 2, false));
        binding.rvPrice.setAdapter(mSelectorSponsorPriceAdp);
        mSelectorSponsorPriceAdp.setList(selectorSponsorPriceDataList);

        //适配器点击事件
        mSelectorSponsorPriceAdp.setOnItemClickListener((adapter, view, position) -> {
            mSelectorSponsorPriceAdp.selectorOpera(position);
            sponsorUsPrice = new BigDecimal(mSelectorSponsorPriceAdp.getData().get(position).getPrice());//打赏金额
        });

        //确定打赏
        binding.tvSponsorus.setOnClickListener(view -> {
            if (sponsorUsPrice == null) {
                ToastUtils.showShort(LocaleController.getString("toast_tips_qxzdsje", R.string.toast_tips_qxzdsje));
                return;
            }

            ETHWallet ethWallet = WalletDaoUtils.getCurrent();
            if (ethWallet != null && ethWallet.getChainId() == 99999) {
                new WalletListDialog(this).show();
                ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
                return;
            }

            //发起支付
            WalletTransferUtil.sendTransaction(
                    chainId,
                    toWalletAddress,
                    null,
                    null,
                    "",
                    sponsorUsPrice.toPlainString(),
                    ethCoinData.getDecimal(),
                    ethCoinData.getName(),
                    new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            ToastUtils.showLong(LocaleController.getString("toast_tips_dscg", R.string.toast_tips_dscg));
                        }

                        @Override
                        public void payError(String error) {
                            ToastUtils.showLong(error);
                        }
                    }
            );
        });

        //自定义币种和数量
        binding.tvCustomquantity.setOnClickListener(view -> {
            SendTransferActivity mTransferFragment = new SendTransferActivity(
                    getUserConfig().getCurrentUser(),
                    null,
                    toWalletAddress,
                    -1,
                    true, null
            );
            presentFragment(mTransferFragment);
        });
    }

    /**
     * 请求钱包和链相关数据
     */
    private void getETHWalletInfoData() {
        //我的钱包地址
        myWalletAddress = WalletDaoUtils.getCurrent().getAddress();
        if (WalletUtil.isEvmAddress(myWalletAddress)) {
            BlockExplorer blockExplorer = BlockFactory.get(chainId);
            new Thread(() -> {
                CountDownLatch countDownLatch = new CountDownLatch(2);
                //获取账户余额
                blockExplorer.getBalance(myWalletAddress, new BlockCallback<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger data) {
                        walletBalanceBigDecimal = new BigDecimal(WalletUtil.fromWei(data.toString(), ethCoinData.getDecimal()));
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(String msg) {
                        walletBalanceBigDecimal = new BigDecimal("0");
                        countDownLatch.countDown();
                    }
                });

                //币种单价请求
                WalletUtil.requestMainCoinPrice(ethCoinData.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
                    @Override
                    public void requestEnd() {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void requestError(String msg) {
                        coinPrice = new BigDecimal("0");
                    }

                    @Override
                    public void requestSuccessful(CurrencyPriceEntity resultData) {
                        coinPrice = new BigDecimal(String.valueOf(resultData.getUsd()));
                    }
                });

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                AndroidUtilities.runOnUIThread(() -> {
                    binding.tvSponsorus.setVisibility(View.VISIBLE);
                    binding.llLoading.setVisibility(View.GONE);

                    //钱包账户余额
                    String walletBalance = String.format(
                            LocaleController.getString("sponsorus_walletbalance", R.string.sponsorus_walletbalance)
                            , walletBalanceBigDecimal.toPlainString() + ethCoinData.getName()
                    ) + WalletUtil.toCoinPriceUSD(walletBalanceBigDecimal, coinPrice, 6);
                    binding.tvWalletAccount.setText(walletBalance);

                    //获取到单价之后，传递给适配器，重新计算
                    mSelectorSponsorPriceAdp.setPrice(coinPrice);
                    mSelectorSponsorPriceAdp.notifyDataSetChanged();
                });
            }).start();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                getETHWalletInfoData();
                break;
        }
    }
}