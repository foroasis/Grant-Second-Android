package teleblock.ui.activity.nft;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityBuyNftBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.wallet.ETHWallet;
import teleblock.model.wallet.NFTInfo;
import teleblock.ui.dialog.NftPayReviewDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/6/1
 * Author:Perry
 * Description：nft下单页面
 */
public class NftBuyActivity extends BaseFragment {

    private ActivityBuyNftBinding binding;

    private NFTInfo nftInfo;
    private long dialog_id;

    public NftBuyActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        nftInfo = (NFTInfo) getArguments().getSerializable("nft_info");
        dialog_id = getArguments().getLong("dialog_id");
        EventBus.getDefault().register(this);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        EventBus.getDefault().unregister(this);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("nft_buy", R.string.nft_buy));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityBuyNftBinding.inflate(LayoutInflater.from(context));
        initView();
        requestWalletBalance();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("nft_you_pay", R.string.nft_you_pay));
        binding.tvPayAddressTitle.setText(LocaleController.getString("nft_you_pay_address", R.string.nft_you_pay_address));
        binding.tvChange.setText(LocaleController.getString("nft_pay_address_change", R.string.nft_pay_address_change));
        binding.tvReview.setText(LocaleController.getString("nft_pay_review_order", R.string.nft_pay_review_order));

        //选择钱包地址
        binding.rlWalletAddress.setOnClickListener(v -> new WalletListDialog(this).show());

        binding.tvReview.setOnClickListener(v -> new NftPayReviewDialog(getContext(), nftInfo) {
            @Override
            public void toSuccessfulPage(String hash, String nftMarketAddress) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.loadNFTGalleryData);
                Bundle bundle = new Bundle();
                bundle.putSerializable("nft_info", nftInfo);
                bundle.putString("hash", hash);
                bundle.putLong("dialog_id", dialog_id);
                bundle.putString("nft_market_address", nftMarketAddress);

                presentFragment(new BuyNftResultActivity(bundle), true);
            }
        }.show());

        //nft
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);
        //nft价格
        binding.tvPrice.setText(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal));
        //币种名称和图标
        binding.tvCoinName.setText(nftInfo.sell_coin);
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), nftInfo.sell_icon, drawable -> binding.tvCoinName.getHelper().setIconNormalLeft(drawable));
        //钱包地址
        binding.tvWalletAddress.setText(WalletDaoUtils.getCurrent().getAddress());
    }

    /**
     * 请求余额
     */
    private void requestWalletBalance() {
        List<MyCoinListData> allCoinList = new ArrayList<>();
        WalletUtil.requestWalletCoinBalance(
                WalletDaoUtils.getCurrent().getAddress(),
                nftInfo.chainId,
                allCoinList, () -> {
                    MyCoinListData mainCoinData = null;
                    for (MyCoinListData myCoinListData : allCoinList) {
                        if (myCoinListData.isIs_main_currency()) {
                            mainCoinData = myCoinListData;
                            break;
                        }
                    }

                    if (mainCoinData != null) {
                        binding.tvWalletBalance.setText(WalletUtil.priceCoinType(WalletUtil.bigDecimalScale(mainCoinData.getBalance(), 6), mainCoinData.getSymbol(), false));
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                binding.tvWalletAddress.setText(WalletDaoUtils.getCurrent().getAddress());
                break;
        }
    }
}