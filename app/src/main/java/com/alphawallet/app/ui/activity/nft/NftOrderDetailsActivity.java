package teleblock.ui.activity.nft;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActivityNftOrderDetailsBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Time:2023/5/31
 * Author:Perry
 * Description：nft订单详情页面
 */
public class NftOrderDetailsActivity extends BaseFragment {
    private ActivityNftOrderDetailsBinding binding;

    private NFTInfo nftInfo;
    private String hash;
    private long dialog_id;
    private boolean bubbleEnter;

    //只有从修改价格页面过来才会有这些
    private String nftMarketAddress;
    private String nftInputPrice;

    private Web3ConfigEntity.WalletNetworkConfigChainType chainType;

    public NftOrderDetailsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        nftInfo = (NFTInfo) getArguments().getSerializable("nft_info");
        hash = getArguments().getString("hash");
        dialog_id = getArguments().getLong("dialog_id");
        bubbleEnter = getArguments().getBoolean("bubble_enter", false);
        nftMarketAddress = getArguments().getString("nft_market_address");
        nftInputPrice = getArguments().getString("nft_input_price");

        chainType = BlockchainConfig.getChainType(nftInfo.chainId);
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivityNftOrderDetailsBinding.inflate(LayoutInflater.from(context));
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        initView();
        loadData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvPageTitle.setText(LocaleController.getString("nft_order_details", R.string.nft_order_details));
        binding.tvNftStatusTitle.setText(LocaleController.getString("nft_order_status", R.string.nft_order_status));
        binding.tvNftOperationDateTitle.setText(LocaleController.getString("nft_operation_date", R.string.nft_operation_date));
        binding.tvNftOrderMakerTitle.setText(LocaleController.getString("nft_order_maker", R.string.nft_order_maker));
        binding.tvNftPriceTitle.setText(LocaleController.getString("nft_requested_price", R.string.nft_requested_price));
        binding.tvNftOrderTakerTitle.setText(LocaleController.getString("nft_order_taker", R.string.nft_order_taker));
        binding.tvTransactionExplorer.setText(LocaleController.getString("nft_transaction_explorer", R.string.nft_transaction_explorer));
        binding.tvConfirmReturnToChat.setText(LocaleController.getString("nft_confirm_return_to_chat", R.string.nft_confirm_return_to_chat));
        binding.tvCancelOrder.setText(LocaleController.getString("nft_cancel_order", R.string.nft_cancel_order));
        binding.tvRetry.setText(LocaleController.getString("nft_order_retry", R.string.nft_order_retry));

        binding.ivClose.setOnClickListener(v -> finishFragment());
        binding.tvCancelOrder.setOnClickListener(v -> new ShelfNftDialog(getContext(), this::finishFragment));

        binding.tvRetry.setOnClickListener(v -> {
            binding.llDefaultErrorOpera.setVisibility(View.GONE);
            binding.llLoading.setVisibility(View.VISIBLE);
            String data;
            if (NFTInfo.ifGrounding(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal))) {
                //更新价格
                data = Web3AbiDataUtils.encodeUpdateListingData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id), WalletUtil.toWei(new BigDecimal(nftInputPrice), nftInfo.sell_decimal));
            } else {
                //上架nft
                data = Web3AbiDataUtils.encodeListItemData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id), WalletUtil.toWei(new BigDecimal(nftInputPrice), nftInfo.sell_decimal));
            }

            WalletTransferUtil.writeContract(nftInfo.chainId, nftMarketAddress, data, new WalletUtil.SendTransactionListener() {
                @Override
                public void paySuccessful(String hash) {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.loadNFTGalleryData, true);
                    NftOrderDetailsActivity.this.hash = hash;
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
                Browser.openUrl(getContext(), chainType.getExplorer_url() + "/tx/" +  hash);
            }
        });

        binding.tvConfirmReturnToChat.setOnClickListener(v -> {
            String msg = NftParseUtil.setParseStr(dialog_id, nftInfo, hash);
            getSendMessagesHelper().sendMessage(msg, dialog_id, null, null, null, true, null, null, null, true, 0, null,false);
            finishFragment();
        });
    }

    private void loadData() {
        //nft图片 名称
        binding.tvNftName.setText(nftInfo.name);
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);

        requestNftPrice();
        requestNftStatus(true);
    }

    /**
     * 获取nft信息
     */
    private void requestNftPrice() {
        //nft价格
        BlockFactory.get(nftInfo.chainId).getTransactionByHash(hash, new BlockCallback<>() {
            @Override
            public void onSuccess(EthTransaction data) {
                Transaction transaction = data.getResult();
                if (transaction != null) {
                    binding.tvNftPrice.setText(WalletUtil.priceCoinType(new BigDecimal(WalletUtil.fromWei(String.valueOf(transaction.getValue()), nftInfo.sell_decimal)), nftInfo.sell_coin, false));
                    GlideHelper.getDrawableGlide(binding.getRoot().getContext(), nftInfo.sell_icon, drawable -> binding.tvNftPrice.getHelper().setIconNormalLeft(drawable));

                    String fromAddress = transaction.getFrom();
                    String inputData = transaction.getInput().substring(10);
                    binding.tvNftOrderMaker.setText(WalletUtil.format10Address(fromAddress));
                    //type=true是上架 false是修改价格
                    boolean type = inputData.length() / 64 == 3;

                    List outputParameters = new ArrayList<TypeReference<Type>>();
                    outputParameters.addAll(
                            type ? Arrays.asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}) : Arrays.asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {})
                    );

                    List<Type> resultList = FunctionReturnDecoder.decode(inputData, outputParameters);
                    if (CollectionUtils.isEmpty(resultList)) {
                        binding.tvNftPrice.setText(WalletUtil.priceCoinType(new BigDecimal(WalletUtil.fromWei(nftInfo.price, nftInfo.sell_decimal)), nftInfo.sell_coin, false));
                    } else {
                        BigInteger price;
                        if (type) {
                            price = ((BigInteger) resultList.get(2).getValue());
                        } else {
                            price = transaction.getValue();
                        }

                        binding.tvNftPrice.setText(WalletUtil.priceCoinType(new BigDecimal(WalletUtil.fromWei(price.toString(), nftInfo.sell_decimal)), nftInfo.sell_coin, false));
                    }
                    GlideHelper.getDrawableGlide(binding.getRoot().getContext(), nftInfo.sell_icon, drawable -> binding.tvNftPrice.getHelper().setIconNormalLeft(drawable));
                }
            }

            @Override
            public void onError(String msg) {
                Timber.e("-->getTransactionByHash：" + msg);
            }
        });
    }

    /**
     * 获取nft状态
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
                            if (!bubbleEnter) {
                                binding.llDefaultOpera.setVisibility(View.VISIBLE);
                            }
                        } else {
                            binding.tvNftStatus.setTextColor(Color.parseColor("#FFFF5F5F"));
                            binding.tvNftStatus.setText(LocaleController.getString("nft_order_status_failed", R.string.nft_order_status_failed));
                            if (!bubbleEnter) {
                                binding.llDefaultErrorOpera.setVisibility(View.VISIBLE);
                            }
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