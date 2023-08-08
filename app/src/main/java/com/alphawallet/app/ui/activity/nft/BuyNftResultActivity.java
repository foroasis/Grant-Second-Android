package teleblock.ui.activity.nft;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActivityBuyNftResultBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.ui.dialog.ShelfNftDialog;
import teleblock.util.TimeUtil;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.util.parse.NftParseUtil;
import teleblock.widget.GlideHelper;
import timber.log.Timber;

/**
 * Time:2023/6/2
 * Author:Perry
 * Description：购买nft结果页
 */
public class BuyNftResultActivity extends BaseFragment {

    private ActivityBuyNftResultBinding binding;

    private NFTInfo nftInfo;
    private String hash;
    private long dialog_id;
    private String nftMarketAddress;

    private Web3ConfigEntity.WalletNetworkConfigChainType chainType;

    public BuyNftResultActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        nftInfo = (NFTInfo) getArguments().getSerializable("nft_info");
        hash = getArguments().getString("hash");
        dialog_id = getArguments().getLong("dialog_id");
        nftMarketAddress = getArguments().getString("nft_market_address");

        chainType = BlockchainConfig.getChainType(nftInfo.chainId);
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivityBuyNftResultBinding.inflate(LayoutInflater.from(context));
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        initView();
        loadData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvPageTitle.setText(LocaleController.getString("nft_pay_result_title", R.string.nft_pay_result_title));
        binding.tvNftStatusTitle.setText(LocaleController.getString("nft_order_status", R.string.nft_order_status));
        binding.tvNftOperationDateTitle.setText(LocaleController.getString("nft_operation_date", R.string.nft_operation_date));
        binding.tvTranscationFeesTitle.setText(LocaleController.getString("nft_transaction_fees", R.string.nft_transaction_fees));
        binding.tvTransactionExplorer.setText(LocaleController.getString("nft_transaction_explorer", R.string.nft_transaction_explorer));
        binding.tvConfirmReturnToChat.setText(LocaleController.getString("nft_confirm_return_to_chat", R.string.nft_confirm_return_to_chat));
        binding.tvCancelOrder.setText(LocaleController.getString("nft_cancel_order", R.string.nft_cancel_order));
        binding.tvRetry.setText(LocaleController.getString("nft_order_retry", R.string.nft_order_retry));

        binding.ivClose.setOnClickListener(v -> finishFragment());

        binding.tvCancelOrder.setOnClickListener(v -> new ShelfNftDialog(getContext(), this::finishFragment));

        binding.tvRetry.setOnClickListener(v -> {
            binding.llDefaultErrorOpera.setVisibility(View.GONE);
            binding.llLoading.setVisibility(View.VISIBLE);

            //购买nft
            String data = Web3AbiDataUtils.encodeBuyItemData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id));
            WalletTransferUtil.writeContract(nftInfo.chainId, nftMarketAddress, data, BigInteger.valueOf(Long.parseLong(nftInfo.price)),
                    new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            BuyNftResultActivity.this.hash = hash;
                            requestNftStatus(true);
                        }

                        @Override
                        public void payError(String error) {
                            ToastUtils.showLong(error);
                            binding.llLoading.setVisibility(View.GONE);
                        }
                    });
        });

        binding.tvTransactionExplorer.setOnClickListener(v -> {
            if (chainType != null) {
                Browser.openUrl(getContext(), chainType.getExplorer_url() + "/tx/" + hash);
            }
        });

        binding.tvConfirmReturnToChat.setOnClickListener(v -> {
            String msg = NftParseUtil.setParseStr(dialog_id, nftInfo, hash);
            getSendMessagesHelper().sendMessage(msg, dialog_id, null, null, null, true, null, null, null, true, 0, null, false);
            finishFragment();
        });
    }

    private void loadData() {
        //nft图片 名称 价格
        binding.tvNftName.setText(nftInfo.name);
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);
        binding.tvTranscationFees.setText(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal) + nftInfo.sell_coin);

        requestNftStatus(true);
    }

    /**
     * 获取nft状态
     *
     * @param b
     */
    private void requestNftStatus(boolean b) {
        BlockFactory.get(nftInfo.chainId).getTransactionReceipt(hash, new BlockCallback<>() {
            @Override
            public void onSuccess(EthGetTransactionReceipt data) {
                TransactionReceipt transaction = data.getResult();
                if (transaction != null) {
                    if (b) {
                        getNftOperaTime(transaction.getBlockHash());
                    }

                    if (StringUtils.isEmpty(transaction.getStatus())) {
                        binding.tvNftStatus.setTextColor(Color.parseColor("#ffff9300"));
                        binding.tvNftStatus.setText(LocaleController.getString("nft_order_status_pending", R.string.nft_order_status_pending));
                        requestNftStatus(false);
                    } else {
                        binding.llLoading.setVisibility(View.GONE);

                        BigInteger statusQuantity = Numeric.decodeQuantity(transaction.getStatus());
                        if (statusQuantity.equals(BigInteger.ONE)) {
                            binding.tvNftStatus.setTextColor(Color.parseColor("#FF4DD025"));
                            binding.tvNftStatus.setText(LocaleController.getString("nft_order_status_completed", R.string.nft_order_status_completed));
                            binding.ivStatusIcon.setImageResource(R.drawable.icon_success_sell_nft);

                            binding.llDefaultOpera.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvNftStatus.setTextColor(Color.parseColor("#FFFF5F5F"));
                            binding.tvNftStatus.setText(LocaleController.getString("nft_order_status_failed", R.string.nft_order_status_failed));
                            binding.ivStatusIcon.setImageResource(R.drawable.icon_fail_sell_nft);

                            binding.llDefaultErrorOpera.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    requestNftStatus(b);
                }
            }

            @Override
            public void onError(String msg) {
                ToastUtils.showLong(msg);
                finishFragment();
            }
        });
    }

    /**
     * 获取nft操作时间
     *
     * @param blockHash
     */
    private void getNftOperaTime(String blockHash) {
        BlockFactory.get(nftInfo.chainId).getBlockByHash(blockHash, new BlockCallback<>() {
            @Override
            public void onSuccess(EthBlock data) {
                EthBlock.Block block = data.getBlock();
                if (block != null) {
                    long timestamp = Long.decode(block.getTimestamp().toString());
                    binding.tvNftOperationDate.setText(TimeUtil.getDate2String(timestamp * 1000));
                }
            }

            @Override
            public void onError(String msg) {
                Timber.e("-->getBlockByHash：" + msg);
            }
        });
    }
}
