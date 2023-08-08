package teleblock.ui.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ResourceUtils;
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
import org.telegram.messenger.databinding.DialogAddWalletBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.LoginManager;
import teleblock.model.wallet.ETHWallet;
import teleblock.util.ETHWalletUtils;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletConnectUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import timber.log.Timber;

/**
 * 描述：添加钱包页面
 */
public class WalletAddDialog extends BaseBottomSheetDialog {

    private BaseFragment baseFragment;
    private DialogAddWalletBinding binding;

    public WalletAddDialog(BaseFragment baseFragment) {
        super(baseFragment.getContext());
        this.baseFragment = baseFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogAddWalletBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_add_wallet_title", R.string.dialog_add_wallet_title));
        binding.tvConnectWalletTitle.setText(LocaleController.getString("dialog_unbind_wallet_connect_wallet_title", R.string.dialog_unbind_wallet_connect_wallet_title));
        binding.tvCreateWallet.setText(LocaleController.getString("dialog_add_wallet_create", R.string.dialog_add_wallet_create));
        binding.tvCreateSolanaWallet.setText(LocaleController.getString("dialog_add_wallet_create_solana", R.string.dialog_add_wallet_create_solana));

        binding.tvCreateWallet.setOnClickListener(v -> {
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

                        AndroidUtilities.runOnUIThread(() -> createSolanaWallet(evmWallet.getPublicAddress()));
                    }
                }

                @Override
                public void failure(WebServiceError webServiceError) {
                    Timber.e("failure-->" + webServiceError);
                    ToastUtils.showLong(webServiceError.getMessage());
                }
            },null);
        });

        binding.tvCreateSolanaWallet.setOnClickListener(v -> {
            createSolanaWallet("");
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

    private void createSolanaWallet(String evmAddress) {
        ParticleNetworkAuth.setChainInfo(ParticleNetwork.INSTANCE, new SolanaChain(SolanaChainId.Mainnet), new ChainChangeCallBack() {
            @Override
            public void success() {
                ETHWallet solanaWallet = CollectionUtils.find(WalletDaoUtils.loadAll(), new CollectionUtils.Predicate<ETHWallet>() {
                    @Override
                    public boolean evaluate(ETHWallet item) {
                        return item.getWalletType() == ETHWallet.TYPE_TSS && item.getAddress().equalsIgnoreCase(ParticleNetworkAuth.getAddress(ParticleNetwork.INSTANCE));
                    }
                });
                // 生成Solana地址
                if (solanaWallet == null) {
                    ETHWallet ethWallet = new ETHWallet();
                    ethWallet.setId(System.currentTimeMillis());
                    ethWallet.setName(ETHWalletUtils.generateNewWalletName());
                    ethWallet.setAddress(ParticleNetworkAuth.getAddress(ParticleNetwork.INSTANCE));
                    ethWallet.setWalletType(ETHWallet.TYPE_TSS);
                    ethWallet.setChainId(99999);
                    if (TextUtils.isEmpty(evmAddress)) {
                        WalletDaoUtils.insertNewWallet(ethWallet);
                    } else {
                        WalletDaoUtils.insert(ethWallet);
                    }
                }
                // 绑定钱包
                String address = TextUtils.isEmpty(evmAddress) ? ParticleNetworkAuth.getAddress(ParticleNetwork.INSTANCE) : evmAddress;
                long chainId = TextUtils.isEmpty(evmAddress) ? 99999 : MMKVUtil.currentChainConfig().getId();
                WalletUtil.walletBind(address, chainId, BlockchainConfig.WalletIconType.MY_TSS_WALLET.typeId, new Runnable() {
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
        });
    }

    private void initData() {
        boolean hasTssEvm = false;
        boolean hasTssSolana = false;
        List<ETHWallet> list = WalletDaoUtils.loadAll();
        for (ETHWallet ethWallet : list) {
            if (ethWallet.getWalletType() == ETHWallet.TYPE_TSS) {
                if (WalletUtil.isEvmAddress(ethWallet.getAddress())) {
                    hasTssEvm = true;
                } else if (WalletUtil.isSolanaAddress(ethWallet.getAddress())) {
                    hasTssSolana = true;
                }
            }
        }
        if (hasTssEvm) {
            binding.tvCreateWallet.setEnabled(false);
            binding.tvCreateWallet.setText(LocaleController.getString("dialog_add_wallet_created", R.string.dialog_add_wallet_created));
            binding.tvCreateWallet.getHelper()
                    .setIconNormalLeft(null)
                    .setTextColorNormal(Color.parseColor("#1A000000"))
                    .setBorderColorNormal(Color.parseColor("#1A000000"));

            if (!hasTssSolana) {
                binding.tvCreateSolanaWallet.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void show() {
        super.show();
//        resetPeekHeight();
    }
}