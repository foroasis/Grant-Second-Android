package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.particle.base.ParticleNetwork;
import com.particle.base.SolanaChain;
import com.particle.base.SolanaChainId;
import com.particle.base.data.WebServiceCallback;
import com.particle.base.data.WebServiceError;
import com.particle.network.ParticleNetworkAuth;
import com.particle.network.service.ChainChangeCallBack;
import com.particle.network.service.LoginPrompt;
import com.particle.network.service.LoginType;
import com.particle.network.service.SupportAuthType;
import com.particle.network.service.model.LoginOutput;
import com.particle.network.service.model.Wallet;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogUnbindWalletBinding;

import java.util.HashMap;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.LoginManager;
import teleblock.model.wallet.ETHWallet;
import teleblock.model.wallet.WalletInfo;
import teleblock.util.ETHWalletUtils;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.TronUtil;
import teleblock.util.WalletConnectUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import timber.log.Timber;

/**
 * Time:2022/12/20
 * Author:Perry
 * Description：未绑定钱包对话框
 */
public class WalletUnbindDialog extends BaseBottomSheetDialog {

    private DialogUnbindWalletBinding binding;
    private boolean ifHideCreateBtn;

    public WalletUnbindDialog(@NonNull Context context) {
        super(context);
    }


    public WalletUnbindDialog(@NonNull Context context, boolean ifHideCreateBtn) {
        super(context);
        this.ifHideCreateBtn = ifHideCreateBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogUnbindWalletBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    /**
     * 创建按钮默认样式
     */
    private void createBtnDefultStyle() {
        binding.tvConfirm.setText(LocaleController.getString("dialog_unbind_wallet_tips", R.string.dialog_unbind_wallet_tips));
        binding.tvCreateWallet.setText(LocaleController.getString("dialog_unbind_wallet_create_wallet", R.string.dialog_unbind_wallet_create_wallet));
        binding.tvCreateWallet.getHelper()
                .setTextColorNormal(Color.WHITE)
                .setBorderColorNormal(ColorUtils.getColor(R.color.theme_color))
                .setBorderWidthNormal(SizeUtils.dp2px(1))
                .setBackgroundColorNormal(ColorUtils.getColor(R.color.theme_color));
    }

    /**
     * 激活按钮样式
     */
    private void createBtnActivationStyle() {
        binding.tvConfirm.setText(LocaleController.getString("dialog_unbind_wallet_activation_tips", R.string.dialog_unbind_wallet_activation_tips));
        binding.tvCreateWallet.setText(LocaleController.getString("dialog_unbind_wallet_activation_wallet", R.string.dialog_unbind_wallet_activation_wallet));
        binding.tvCreateWallet.getHelper()
                .setTextColorNormal(ColorUtils.getColor(R.color.theme_color))
                .setBorderColorNormal(ColorUtils.getColor(R.color.theme_color))
                .setBorderWidthNormal(SizeUtils.dp2px(1))
                .setBackgroundColorNormal(Color.WHITE);
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("chat_transfer_meunbind_wallet", R.string.chat_transfer_meunbind_wallet));
        binding.tvConnectWalletTitle.setText(LocaleController.getString("dialog_unbind_wallet_connect_wallet_title", R.string.dialog_unbind_wallet_connect_wallet_title));

        binding.tvCreateWallet.setVisibility(ifHideCreateBtn ? View.GONE : View.VISIBLE);
        binding.tvConfirm.setVisibility(ifHideCreateBtn ? View.GONE : View.VISIBLE);
        binding.tvWalletTokenpocket.setVisibility(ifHideCreateBtn ? View.GONE : View.VISIBLE);

        //获取当前用户的nft
        TelegramUtil.getUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId(), new TelegramUtil.UserNftDataListener() {
            @Override
            public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                if (CollectionUtils.isEmpty(walletInfoList)) {
                    createBtnDefultStyle();
                    return;
                }

                List<WalletInfo.WalletInfoItem> walletInfo = walletInfoList.get(0).getWallet_info();
                if (CollectionUtils.isEmpty(walletInfo)) {
                    createBtnDefultStyle();
                    return;
                }

                boolean ifHavaParicleWallet = false;
                for (WalletInfo.WalletInfoItem walletInfoItem : walletInfo) {
                    if (walletInfoItem.getWallet_type().equals("100")) {
                        ifHavaParicleWallet = true;
                        break;
                    }
                }

                if (ifHavaParicleWallet) {
                    createBtnActivationStyle();
                } else {
                    createBtnDefultStyle();
                }
            }

            @Override
            public void nftDataRequestError(String errorMsg) {
                createBtnDefultStyle();
            }
        });

        //创建新钱包
        binding.tvCreateWallet.setOnClickListener(v -> {
            LoginManager.userLogin(() -> {
                ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                    @Override
                    public void success(LoginOutput loginOutput) {
                        Timber.i("success-->" + loginOutput);
                        Wallet evmWallet = CollectionUtils.find(loginOutput.getWallets(), item -> "evm_chain".equals(item.getChain_name()));
                        if (evmWallet != null) {
                            ETHWallet ethWallet = CollectionUtils.find(WalletDaoUtils.loadAll(), new CollectionUtils.Predicate<ETHWallet>() {
                                @Override
                                public boolean evaluate(ETHWallet item) {
                                    return item.getWalletType() == ETHWallet.TYPE_TSS && item.getAddress().equalsIgnoreCase(evmWallet.getPublicAddress());
                                }
                            });
                            if (ethWallet != null) { // 已创建过
                                WalletDaoUtils.updateCurrent(ethWallet.getId());
                            } else {
                                ethWallet = new ETHWallet();
                                ethWallet.setId(System.currentTimeMillis());
                                ethWallet.setName(ETHWalletUtils.generateNewWalletName());
                                ethWallet.setAddress(evmWallet.getPublicAddress());
                                ethWallet.setWalletType(ETHWallet.TYPE_TSS);
                                ethWallet.setChainId(1);
                                WalletDaoUtils.insertNewWallet(ethWallet);

                                // 同时生成Tron地址
//                            ethWallet = new ETHWallet();
//                            ethWallet.setId(System.currentTimeMillis());
//                            ethWallet.setName(ETHWalletUtils.generateNewWalletName());
//                            ethWallet.setAddress(TronUtil.toTronBase58(evmWallet.getPublicAddress()));
//                            ethWallet.setWalletType(ETHWallet.TYPE_TSS);
//                            ethWallet.setChainId(999);
//                            WalletDaoUtils.insert(ethWallet);
                            }
                            AndroidUtilities.runOnUIThread(() -> ParticleNetworkAuth.setChainInfo(ParticleNetwork.INSTANCE, new SolanaChain(SolanaChainId.Mainnet), new ChainChangeCallBack() {
                                @Override
                                public void success() {
                                    // 生成Solana地址
                                    ETHWallet ethWallet1 = new ETHWallet();
                                    ethWallet1.setId(System.currentTimeMillis());
                                    ethWallet1.setName(ETHWalletUtils.generateNewWalletName());
                                    ethWallet1.setAddress(ParticleNetworkAuth.getAddress(ParticleNetwork.INSTANCE));
                                    ethWallet1.setWalletType(ETHWallet.TYPE_TSS);
                                    ethWallet1.setChainId(99999);
                                    WalletDaoUtils.insert(ethWallet1);
                                    // 绑定钱包
                                    WalletUtil.walletBind(evmWallet.getPublicAddress(), MMKVUtil.currentChainConfig().getId(), BlockchainConfig.WalletIconType.MY_TSS_WALLET.typeId, new Runnable() {
                                        @Override
                                        public void run() {
                                            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CREATED));
                                            dismiss();
                                        }
                                    });
                                }

                                @Override
                                public void failure() {
                                    EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CREATED));
                                    dismiss();
                                }
                            }));
                        }
                    }

                    @Override
                    public void failure(WebServiceError webServiceError) {
                        Timber.e("failure-->" + webServiceError);
                        ToastUtils.showLong(webServiceError.getMessage());
                    }
                },null);
            }, null);
        });

        //链接钱包
        binding.tvWalletMetamask.setOnClickListener(v -> {
            WalletConnectUtil.walletConnect(BlockchainConfig.PKG_META_MASK);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_metamask点击, new HashMap<>());
            dismiss();
        });
        binding.tvWalletTtwallet.setOnClickListener(v -> {
            WalletConnectUtil.walletConnect(BlockchainConfig.PKG_TT_WALLET);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_TTWallet点击, new HashMap<>());
            dismiss();
        });
        binding.tvWalletImtoken.setOnClickListener(v -> {
            WalletConnectUtil.walletConnect(BlockchainConfig.PKG_IMTOKEN);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_imToken点击, new HashMap<>());
            dismiss();
        });
        binding.tvWalletTokenpocket.setOnClickListener(v -> {
            WalletConnectUtil.walletConnect(BlockchainConfig.PKG_TOKEN_POCKET);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_tokenpocket点击, new HashMap<>());
            dismiss();
        });
        binding.tvWalletPhantom.setOnClickListener(v -> {
            WalletConnectUtil.walletConnect(BlockchainConfig.PKG_PHANTOM);
            EventUtil.track(getContext(), EventUtil.Even.链接钱包_phantom点击, new HashMap<>());
            dismiss();
        });
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }
}
