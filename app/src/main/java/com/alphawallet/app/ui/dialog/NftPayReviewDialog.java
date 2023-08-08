package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogNftPayReviewBinding;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/6/1
 * Author:Perry
 * Description：nft下单预览页面
 */
public class NftPayReviewDialog extends BaseBottomSheetDialog {

    private DialogNftPayReviewBinding binding;

    private NFTInfo nftInfo;
    private String nftMarketAddress;
    private Web3ConfigEntity.WalletNetworkConfigChainType chainType;

    private boolean loading = true;

    public NftPayReviewDialog(@NonNull Context context, NFTInfo nftInfo) {
        super(context);
        this.nftInfo = nftInfo;

        for (Web3ConfigEntity.NftMarketAddress nftMarketAddress : MMKVUtil.getWeb3ConfigData().getNftMarketAddress()) {
            if (nftInfo.chainId == nftMarketAddress.getChain_id()) {
                this.nftMarketAddress = nftMarketAddress.getContract_address();
                break;
            }
        }

        chainType = BlockchainConfig.getChainType(nftInfo.chainId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogNftPayReviewBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.tvPageTitle.setText(LocaleController.getString("nft_pay_review_order_title", R.string.nft_pay_review_order_title));
        binding.tvNftContractAddressTitle.setText(LocaleController.getString("nft_contract_address", R.string.nft_contract_address));
        binding.tvNftOrderMakerTitle.setText(LocaleController.getString("nft_order_maker", R.string.nft_order_maker));
        binding.tvNftPriceTitle.setText(LocaleController.getString("nft_requested_price", R.string.nft_requested_price));
        binding.tvNftOrderTakerTitle.setText(LocaleController.getString("nft_order_taker", R.string.nft_order_taker));
        binding.tvNftEstimatedFeesTitle.setText(LocaleController.getString("nft_estimated_fees", R.string.nft_estimated_fees));
        binding.tvConfirm.setText(LocaleController.getString("nft_pay_review_confirm", R.string.nft_pay_review_confirm));

        binding.ivClose.setOnClickListener(v -> dismiss());

        binding.rlBottomView.setOnClickListener(v -> {
            if (loading) return;
            buyNft();
        });

        //nft图片
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);
        //nft名称
        binding.tvNftName.setText(nftInfo.name);
        //合约地址
        binding.tvNftContractAddress.setText(nftInfo.contract_address);
        //卖家地址
        binding.tvNftOrderMaker.setText(WalletUtil.format10Address(nftInfo.seller));
        //nft价格
        binding.tvNftPrice.setText(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal) + nftInfo.sell_coin);
        //nft价格图标
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), nftInfo.sell_icon, drawable -> binding.tvNftPrice.getHelper().setIconNormalLeft(drawable));
        //买家地址
        binding.tvNftOrderTaker.setText(WalletUtil.format10Address(WalletDaoUtils.getCurrent().getAddress()));

        binding.tvNftContractAddress.setOnClickListener(v -> {
            if (chainType != null) {
                Browser.openUrl(getContext(), chainType.getExplorer_url() + "address/" + nftInfo.contract_address);
            }
        });

        //请求gas费用
        WalletUtil.requestGasFee(
                nftInfo.chainId,
                nftInfo.sell_decimal,
                nftInfo.seller,
                WalletDaoUtils.getCurrent().getAddress(),
                new BigDecimal(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal)),
                nftMarketAddress,
                Web3AbiDataUtils.encodeBuyItemData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id)),
                (gasFee, gasLimit) -> {
                    //gas费
                    String gasPrice = WalletUtil.fromWei((Convert.fromWei(gasFee.toPlainString(), Convert.Unit.GWEI).multiply(gasLimit)).toPlainString(), 9);
                    binding.tvNftEstimatedFees.setText(gasPrice + nftInfo.sell_coin);

                    showLoading(false);
                });
    }

    /**
     * 购买nft
     */
    private void buyNft() {
        showLoading(true);

        String data = Web3AbiDataUtils.encodeBuyItemData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id));
        WalletTransferUtil.writeContract(nftInfo.chainId, nftMarketAddress, data, BigInteger.valueOf(Long.parseLong(nftInfo.price)),
                new WalletUtil.SendTransactionListener() {
                    @Override
                    public void paySuccessful(String hash) {
                        showLoading(false);
                        toSuccessfulPage(hash, nftMarketAddress);
                        dismiss();
                    }

                    @Override
                    public void payError(String error) {
                        showLoading(false);
                        ToastUtils.showLong(error);
                    }
                });
    }

    //跳转到成功页面
    public void toSuccessfulPage(String hash, String nftMarketAddress) {
    }

    private void showLoading(boolean b) {
        if (b) {
            loading = true;
            binding.pbLoading.setVisibility(View.VISIBLE);
            binding.tvConfirm.setVisibility(View.INVISIBLE);
        } else {
            loading = false;
            binding.pbLoading.setVisibility(View.INVISIBLE);
            binding.tvConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }
}
