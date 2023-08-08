package teleblock.ui.activity.nft;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityNftSellPriceBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeDecoder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.wallet.NFTInfo;
import teleblock.ui.dialog.ShelfNftDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.PriceTextWatcher;

/**
 * Time:2023/5/30
 * Author:Perry
 * Description：nft设置修改价格
 */
public class NftSellPriceActivity extends BaseFragment {

    private ActivityNftSellPriceBinding binding;

    private NFTInfo nftInfo;
    private long dialog_id;

    private String nftPrice;
    private String nftMarketAddress;
    private String nftAddress;
    private BigInteger tokenId;

    //输入的金额
    private BigDecimal inputPrice;

    private AlertDialog progressDialog;

    public NftSellPriceActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        nftInfo = (NFTInfo) getArguments().getSerializable("nft_info");
        dialog_id = getArguments().getLong("dialog_id");

        for (Web3ConfigEntity.NftMarketAddress nftMarketAddress : MMKVUtil.getWeb3ConfigData().getNftMarketAddress()) {
            if (nftInfo.chainId == nftMarketAddress.getChain_id()) {
                this.nftMarketAddress = nftMarketAddress.getContract_address();
                break;
            }
        }

        nftPrice = WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal);
        nftAddress = nftInfo.contract_address;
        tokenId = BigInteger.valueOf(nftInfo.token_id);
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("nft_sell", R.string.nft_sell));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityNftSellPriceBinding.inflate(LayoutInflater.from(context));
        initView();
        requestWalletBalance();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("nft_requested_price", R.string.nft_requested_price));

        if (NFTInfo.ifGrounding(nftPrice)) {
            inputPrice = new BigDecimal(nftPrice);
            binding.etPrice.setText(nftPrice);
            binding.tvLeftBtn.setText(LocaleController.getString("nft_cancel_order", R.string.nft_cancel_order));
            binding.tvRightBtn.setText(LocaleController.getString("nft_update_order", R.string.nft_update_order));
        } else {
            binding.tvLeftBtn.setText(LocaleController.getString("nft_back", R.string.nft_back));
            binding.tvRightBtn.setText(LocaleController.getString("nft_submit_price", R.string.nft_submit_price));
        }

        binding.tvLeftBtn.setOnClickListener(v -> {
            AndroidUtilities.hideKeyboard(binding.etPrice);
            if (NFTInfo.ifGrounding(nftPrice)) {
                new ShelfNftDialog(getContext(), this::offShelfNft);
            } else {
                finishFragment();
            }
        });

        binding.tvRightBtn.setOnClickListener(v -> {
            AndroidUtilities.hideKeyboard(binding.etPrice);
            if (WalletUtil.decimalCompareTo(inputPrice, BigDecimal.ZERO)) {
                if (progressDialog == null) {
                    progressDialog = new AlertDialog(getContext(), 3);
                    progressDialog.setCancelable(true);
                }
                approve(true);
                progressDialog.show();
            } else {
                ToastUtils.showLong("请输入金额");
            }
        });

        //输入金额限制判断
        binding.etPrice.addTextChangedListener(new PriceTextWatcher(binding.etPrice) {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                String moneyStr = binding.etPrice.getText().toString().trim();
                try {
                    inputPrice = new BigDecimal(moneyStr);
                } catch (Exception e) {
                    inputPrice = BigDecimal.ZERO;
                }
            }
        });

        //nft图片
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);

        //nft交易单位和图标
        binding.tvCoinSymbol.setText(nftInfo.sell_coin);
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), nftInfo.sell_icon, drawable -> binding.tvCoinSymbol.getHelper().setIconNormalLeft(drawable));
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
                        binding.tvWalletBalance.setText(WalletUtil.priceCoinType(WalletUtil.bigDecimalScale(mainCoinData.getBalance(), 6), nftInfo.sell_coin, false));
                    }
                });
    }

    /**
     * 授权
     */
    private void approve(boolean needApprove) {
        String data = Web3AbiDataUtils.encodeGetApprovedData(tokenId);
        BlockFactory.get(nftInfo.chainId).ethCall(nftAddress, data, new BlockCallback<>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                if (!nftMarketAddress.equalsIgnoreCase(FunctionReturnDecoder.decodeAddress(data))) {
                    if (!needApprove) {
                        approve(false);
                        return;
                    }
                    data = Web3AbiDataUtils.encodeApproveData(nftMarketAddress, tokenId);
                    WalletTransferUtil.writeContract(nftInfo.chainId, nftAddress, data, new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            approve(false);
                        }

                        @Override
                        public void payError(String error) {
                            ToastUtils.showLong(error);
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    sellNft();
                }
            }

            @Override
            public void onError(String msg) {
                ToastUtils.showLong(msg);
                progressDialog.dismiss();
            }
        });
    }

    /**
     * 上架nft
     */
    private void sellNft() {
        String data;
        if (NFTInfo.ifGrounding(nftPrice)) {
            //更新价格
            data = Web3AbiDataUtils.encodeUpdateListingData(nftAddress, tokenId, WalletUtil.toWei(inputPrice, nftInfo.sell_decimal));
        } else {
            //上架nft
            data = Web3AbiDataUtils.encodeListItemData(nftAddress, tokenId, WalletUtil.toWei(inputPrice, nftInfo.sell_decimal));
        }

        WalletTransferUtil.writeContract(nftInfo.chainId, nftMarketAddress, data, new WalletUtil.SendTransactionListener() {
            @Override
            public void paySuccessful(String hash) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.loadNFTGalleryData, true);
                Bundle bundle = new Bundle();
                bundle.putSerializable("nft_info", nftInfo);
                bundle.putString("hash", hash);
                bundle.putLong("dialog_id", dialog_id);

                bundle.putString("nft_market_address", nftMarketAddress);
                bundle.putString("nft_input_price", inputPrice.toString());

                presentFragment(new NftOrderDetailsActivity(bundle), true);
                progressDialog.dismiss();
            }

            @Override
            public void payError(String error) {
                progressDialog.dismiss();
                ToastUtils.showLong(error);
            }
        });
    }

    /**
     * 下架nft
     */
    private void offShelfNft() {
        progressDialog.show();
        String data = Web3AbiDataUtils.encodeCancelListingData(nftAddress, tokenId);
        WalletTransferUtil.writeContract(nftInfo.chainId, nftMarketAddress, data, new WalletUtil.SendTransactionListener() {
            @Override
            public void paySuccessful(String hash) {
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.loadNFTGalleryData, true);
                progressDialog.dismiss();
                finishFragment();
            }

            @Override
            public void payError(String error) {
                progressDialog.dismiss();
                ToastUtils.showLong(error);
            }
        });
    }
}