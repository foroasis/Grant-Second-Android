package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.exoplayer2.util.Log;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.particle.base.ParticleNetwork;
import com.particle.base.data.WebOutput;
import com.particle.base.data.WebServiceCallback;
import com.particle.base.data.WebServiceError;
import com.particle.network.ParticleNetworkAuth;
import com.particle.network.service.LoginType;
import com.particle.network.service.SupportAuthType;
import com.particle.network.service.model.LoginOutput;
import com.particle.network.service.model.UserInfo;
import com.particle.network.service.model.Wallet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActGrantTaskDetailBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.blockchain.bnb.bean.AddressWallet;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.LoginManager;
import teleblock.model.GrantTaskEntity;
import teleblock.model.OrderResultEntity;
import teleblock.model.ParticleUserInfo;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.GrantTaskApi;
import teleblock.network.api.GroupAccreditApi;
import teleblock.network.api.MintCallbackApi;
import teleblock.network.api.WalletSocialApi;
import teleblock.network.api.blockchain.bnb.CyberConnectApi;
import teleblock.ui.adapter.GrantTaskProgressAdapter;
import teleblock.ui.dialog.CcProfileMintDialog;
import teleblock.ui.dialog.MintEssenceNftSuccessfulDialog;
import teleblock.ui.dialog.WalletAddDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.ui.dialog.WalletUnbindDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.CommonCallback;
import teleblock.widget.GlideHelper;

import org.telegram.tgnet.TLRPC;

/**
 * grant任务详情页
 */
public class GrantTaskDetailAct extends BaseFragment {

    private ActGrantTaskDetailBinding binding;
    private GrantTaskProgressAdapter grantTaskProgressAdapter;
    private GrantTaskEntity grantTaskEntity;
    private long chat_id;

    public GrantTaskDetailAct(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        chat_id = getArguments().getLong("chat_id");
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        EventBus.getDefault().unregister(this);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        EventUtil.track(getContext(), EventUtil.Even.任务页, new HashMap<>());
        binding = ActGrantTaskDetailBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTaskTitle.setText(LocaleController.getString("grant_task_detail_title", R.string.grant_task_detail_title));
        binding.tvMintCompletedTip.setText(LocaleController.getString("grant_task_detail_mint_completed", R.string.grant_task_detail_mint_completed));
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
        binding.tvJoinGroup.setText(LocaleController.getString("grant_task_detail_join_group", R.string.grant_task_detail_join_group));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        grantTaskProgressAdapter = new GrantTaskProgressAdapter();
        binding.recyclerView.setAdapter(grantTaskProgressAdapter);

        binding.flBack.setOnClickListener(view -> finishFragment());
        binding.flRefresh.setOnClickListener(view -> {
            AlertDialog progressDialog = new AlertDialog(getContext(), 3);
            progressDialog.setCanCancel(false);
            showDialog(progressDialog);
            loadTaskData();
        });
        binding.tvMintNft.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.mint_Nft按钮, new HashMap<>());
            if (grantTaskEntity.contract_info.is_cyber_connect == 1) {
                collectEssenceNFT();
            } else {
                mintNft();
            }
        });
        binding.tvJoinGroup.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.加入空投群按钮, new HashMap<>());
            validateJoin();
        });

        grantTaskProgressAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrantTaskEntity.TaskInfoEntity item = grantTaskProgressAdapter.getItem(position);
            if (item != null && item.status == 0) {
                if ("connectWallet".equalsIgnoreCase(item.url)) {
                    new WalletUnbindDialog(getContext()).show();
                } else if ("particleConnectWallet".equalsIgnoreCase(item.url)) {
                    checkUserWallet();
                } else if ("particleSocial".equalsIgnoreCase(item.url)) {
                    checkAccountAndSecurity();
                } else if ("createCcProfile".equalsIgnoreCase(item.url)) {
                    String address = WalletDaoUtils.getCurrent().getAddress();
                    if (!WalletUtil.isEvmAddress(address)) {
                        new WalletListDialog(this).show();
                        ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
                        return;
                    }
                    TelegramUtil.verifyCCProfile(getContext(), address, b -> {
                        if (b) {
                            presentFragment(new Web3SocialCircleActivity());
                        } else {
                            new CcProfileMintDialog(this).show();
                        }
                    });
                } else {
                    Browser.openUrl(getContext(), item.url);
                }
            }
        });
    }

    private void initData() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.rlTopBar.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;
        binding.llBottom.setVisibility(View.GONE);
        loadTaskData();
    }

    private void showDetail(GrantTaskEntity grantTaskEntity) {
        this.grantTaskEntity = grantTaskEntity;
        GlideHelper.displayImage(binding.ivImage.getContext(), binding.ivImage, grantTaskEntity.banner_info.image);
        grantTaskProgressAdapter.setList(grantTaskEntity.task_info);
        binding.llBottom.setVisibility(View.VISIBLE);
        //底部完成任务提示
        binding.tvTaskCompletedTip.setText(grantTaskEntity.notice);
        boolean taskCompleted = true;
        for (GrantTaskEntity.TaskInfoEntity taskInfo : grantTaskProgressAdapter.getData()) {
            if (taskInfo.status == 0) {
                taskCompleted = false;
                break;
            }
        }

        if (taskCompleted) { // 任务都完成
            checkNftBalance(grantTaskEntity.contract_info, false);
        }
    }

    private void checkNftBalance(GrantTaskEntity.ContractInfoEntity contractInfo, boolean background) {
        if (WalletDaoUtils.getCurrent() == null) return;
        String address = WalletDaoUtils.getCurrent().getAddress();
        if (!WalletUtil.isEvmAddress(address)) {
            new WalletListDialog(this).show();
            ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
            return;
        }
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_loading", R.string.grant_task_detail_mint_nft_loading));
        String data = Web3AbiDataUtils.encodeBalanceOfData(address, contractInfo.token_id == 0 ? null : BigInteger.valueOf(contractInfo.token_id));
        BlockFactory.get(contractInfo.chain_id).ethCall(contractInfo.address, data, new BlockCallback<String>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                int num = Numeric.toBigInt(data).intValue();
                if (num > 0) {
                    binding.llMintNft.setVisibility(View.GONE);
                    binding.llJoinGroup.setVisibility(View.VISIBLE);
                    if (grantTaskEntity.airdrop_group_info.is_join_group != 1) {
                        binding.tvJoinGroup.setVisibility(View.GONE);
                    }
                } else {
                    if (background) {
                        binding.llMintNft.postDelayed(() -> checkNftBalance(contractInfo, true), 1000);
                    } else {
                        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                        binding.tvMintNft.setEnabled(true);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                ToastUtils.showLong(msg);
                binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                binding.tvMintNft.setEnabled(true);
            }
        });
    }

    private void validateJoin() {
        long chat_id = grantTaskEntity.airdrop_group_info.chat_id;
        TLRPC.Chat chat = getMessagesController().getChat(chat_id);
        if (!ChatObject.isNotInChat(chat)) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chat_id);
            presentFragment(new ChatActivity(args));
            return;
        }

        validate();
    }

    private void validate() {
        String address = WalletDaoUtils.getCurrent().getAddress();
        if (!WalletUtil.isEvmAddress(address)) {
            new WalletListDialog(this).show();
            ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
            return;
        }
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCancel(false);
        showDialog(progressDialog);
        EasyHttp.post(new ApplicationLifecycle())
                .api(new GroupAccreditApi()
                        .setGroup_id(grantTaskEntity.airdrop_group_info.group_id)
                        .setPayment_account(address)
                ).request(new OnHttpListener<BaseBean<OrderResultEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<OrderResultEntity> result) {
                        if (result.getCode() == 422) {
                            ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_not_satisfied", R.string.group_validate_join_validate_not_satisfied));
                        } else if (result.getData().ship != null) {
                            EventUtil.track(getContext(), EventUtil.Even.验证入群, new HashMap<>());
                            Browser.openUrl(getContext(), (result.getData().ship.url));
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_fail", R.string.group_validate_join_validate_fail));
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                    }
                });
    }

    private void loadTaskData() {
        EasyHttp.cancel(this.getClass().getSimpleName());
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new GrantTaskApi().setChat_id(chat_id))
                .request(new OnHttpListener<BaseBean<GrantTaskEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<GrantTaskEntity> result) {
                        showDetail(result.getData());
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                    }
                });
    }

    private void mintNft() {
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_loading", R.string.grant_task_detail_mint_nft_loading));
        binding.tvMintNft.setEnabled(false);
        GrantTaskEntity.ContractInfoEntity contractInfo = grantTaskEntity.contract_info;
        String data = Web3AbiDataUtils.encodeMintData(BigInteger.valueOf(contractInfo.token_id));
        WalletTransferUtil.writeContract(contractInfo.chain_id, contractInfo.address, data, new WalletUtil.SendTransactionListener() {
            @Override
            public void paySuccessful(String hash) {
                checkNftBalance(contractInfo, true);
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new MintCallbackApi())
                        .request(null);
            }

            @Override
            public void payError(String error) {
                ToastUtils.showLong(error);
                binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                binding.tvMintNft.setEnabled(true);
            }
        });
    }

    private void createEssenceNFT() {
        String address = WalletDaoUtils.getCurrent().getAddress();
        if (MMKVUtil.ccRefreshToken().isEmpty()) {
            CyberConnectApi.getCcAccessToken(address, new CommonCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    super.onSuccess(data);
                    createEssenceNFT();
                }

                @Override
                public void onError(String msg) {
                    ToastUtils.showLong(msg);
                }
            });
        } else {
            List<String> walletList = new ArrayList<>();
            walletList.add(address);
            CyberConnectApi.getAddresses(walletList, new CommonCallback<List<AddressWallet>>() {
                @Override
                public void onSuccess(List<AddressWallet> data) {
                    super.onSuccess(data);
                    try {
                        int profileID = data.get(0).wallet.profiles.edges.get(0).node.profileID;
                        CyberConnectApi.createEssenceNFT(profileID, new CommonCallback<String>() {
                            @Override
                            public void onSuccess(String data) {
                                super.onSuccess(data);
                                CyberConnectApi.checkAddressEssence(address, new CommonCallback<Boolean>() {
                                });
                            }

                            @Override
                            public void onError(String msg) {
                                ToastUtils.showLong(msg);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        onError(e.getMessage());
                    }
                }

                @Override
                public void onError(String msg) {
                    ToastUtils.showLong(msg);
                }
            });
        }
    }

    private void collectEssenceNFT() {
        String address = WalletDaoUtils.getCurrent().getAddress();
        if (!WalletUtil.isEvmAddress(address)) {
            new WalletListDialog(this).show();
            ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
            return;
        }
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_loading", R.string.grant_task_detail_mint_nft_loading));
        binding.tvMintNft.setEnabled(false);
        if (MMKVUtil.ccRefreshToken().isEmpty()) {
            CyberConnectApi.getCcAccessToken(address, new CommonCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    super.onSuccess(data);
                    collectEssenceNFT();
                }

                @Override
                public void onError(String msg) {
                    ToastUtils.showLong(msg);
                    binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                    binding.tvMintNft.setEnabled(true);
                }
            });
        } else {
            GrantTaskEntity.ContractInfoEntity contractInfo = grantTaskEntity.contract_info;
            CyberConnectApi.collectEssenceNFT(contractInfo.profileID, address, contractInfo.essenceID, new CommonCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    super.onSuccess(data);
                    checkNftBalance(contractInfo, true);
                    new MintEssenceNftSuccessfulDialog(getContext(), () -> {
                    }).show();
                }

                @Override
                public void onError(String msg) {
                    ToastUtils.showLong(msg);
                    binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                    binding.tvMintNft.setEnabled(true);
                }
            });
        }
    }

    private void checkUserWallet() {
        ETHWallet particleWallet = null;
        List<ETHWallet> list = WalletDaoUtils.loadAll();
        for (ETHWallet ethWallet : list) {
            if (ethWallet.getWalletType() == ETHWallet.TYPE_TSS) {
                if (WalletUtil.isEvmAddress(ethWallet.getAddress())) {
                    particleWallet = ethWallet;
                    break;
                }
            }
        }
        if (particleWallet == null) {
            new WalletAddDialog(this).show();
            return;
        }

        UserInfo userInfo = ParticleNetworkAuth.getUserInfo(ParticleNetwork.INSTANCE);
        if (userInfo == null) {
            ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                @Override
                public void success(@NonNull LoginOutput loginOutput) {
                    AndroidUtilities.runOnUIThread(() -> checkUserWallet(), 300);
                }

                @Override
                public void failure(@NonNull WebServiceError webServiceError) {
                }
            },null);
            return;
        }
        Wallet evmWallet = CollectionUtils.find(userInfo.getWallets(), item -> "evm_chain".equals(item.getChain_name()));
        if (!evmWallet.getPublicAddress().equalsIgnoreCase(particleWallet.getAddress())) {
            ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                @Override
                public void success(@NonNull LoginOutput loginOutput) {
                    AndroidUtilities.runOnUIThread(() -> checkUserWallet(), 300);
                }

                @Override
                public void failure(@NonNull WebServiceError webServiceError) {
                }
            },null);
            return;
        }

        ETHWallet currentWallet = WalletDaoUtils.getCurrent();
        if (!currentWallet.getAddress().equals(particleWallet.getAddress())) {
            WalletDaoUtils.updateCurrent(particleWallet.getId());
            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CHANGED));
        }
        WalletUtil.walletBind(particleWallet.getAddress(), MMKVUtil.currentChainConfig().getId(), particleWallet.getWalletType(), null);
    }


    //绑定社交账号
    private void checkAccountAndSecurity() {
        ETHWallet particleWallet = null;
        List<ETHWallet> list = WalletDaoUtils.loadAll();
        for (ETHWallet ethWallet : list) {
            if (ethWallet.getWalletType() == ETHWallet.TYPE_TSS) {
                if (WalletUtil.isEvmAddress(ethWallet.getAddress())) {
                    particleWallet = ethWallet;
                    break;
                }
            }
        }
        if (particleWallet == null) {
            new WalletAddDialog(this).show();
            return;
        }

        UserInfo userInfo = ParticleNetworkAuth.getUserInfo(ParticleNetwork.INSTANCE);
        if (userInfo == null) {
            ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                @Override
                public void success(@NonNull LoginOutput loginOutput) {
                    AndroidUtilities.runOnUIThread(() -> checkAccountAndSecurity(), 300);
                }

                @Override
                public void failure(@NonNull WebServiceError webServiceError) {
                }
            },null);
            return;
        }
        Wallet evmWallet = CollectionUtils.find(userInfo.getWallets(), item -> "evm_chain".equals(item.getChain_name()));
        if (!evmWallet.getPublicAddress().equalsIgnoreCase(particleWallet.getAddress())) {
            ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                @Override
                public void success(@NonNull LoginOutput loginOutput) {
                    AndroidUtilities.runOnUIThread(() -> checkAccountAndSecurity(), 300);
                }

                @Override
                public void failure(@NonNull WebServiceError webServiceError) {
                }
            },null);
            return;
        }

        ETHWallet currentWallet = WalletDaoUtils.getCurrent();
        if (!currentWallet.getAddress().equals(particleWallet.getAddress())) {
            WalletDaoUtils.updateCurrent(particleWallet.getId());
            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CHANGED));
        }
        ParticleNetworkAuth.openAccountAndSecurity(ParticleNetwork.INSTANCE, new WebServiceCallback<>() {
            @Override
            public void success(@NonNull WebOutput webOutput) {
            }

            @Override
            public void failure(@NonNull WebServiceError webServiceError) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ETHWallet ethWallet = WalletDaoUtils.getCurrent();
        UserInfo userInfo = ParticleNetworkAuth.getUserInfo(ParticleNetwork.INSTANCE);//本地userinfo不实时
        if (userInfo == null) return;
        Wallet evmWallet = CollectionUtils.find(userInfo.getWallets(), item -> "evm_chain".equals(item.getChain_name()));
        if (ethWallet != null && ethWallet.getWalletType() == ETHWallet.TYPE_TSS && ethWallet.getAddress().equalsIgnoreCase(evmWallet.getPublicAddress())) {
            String uuid = userInfo.getUuid();
            String token = userInfo.getToken();
            BlockExplorer.getParticleUserInfo(uuid, token, new BlockCallback<ParticleUserInfo>() {
                @Override
                public void onSuccess(ParticleUserInfo data) {
                    if (data == null) return;
                    String bindText = "";
                    String bindType = "";
                    if (!TextUtils.isEmpty(data.facebookId)) {
                        bindText = data.facebookId;
                        bindType = "facebook";
                    } else if (!TextUtils.isEmpty(data.twitterId)) {
                        bindText = data.twitterId;
                        bindType = "twitter";
                    } else if (!TextUtils.isEmpty(data.discordId)) {
                        bindText = data.discordId;
                        bindType = "discord";
                    } else if (!TextUtils.isEmpty(data.twitchId)) {
                        bindText = data.twitchId;
                        bindType = "twitch";
                    } else if (!TextUtils.isEmpty(data.microsoftId)) {
                        bindText = data.microsoftId;
                        bindType = "microsoft";
                    } else if (!TextUtils.isEmpty(data.phone)) {
                        bindText = data.phone;
                        bindType = "phone";
                    } else if (!TextUtils.isEmpty(data.email)) {
                        bindText = data.email;
                        bindType = "email";
                    } else if (!TextUtils.isEmpty(data.googleEmail)) {
                        bindText = data.googleEmail;
                        bindType = "googleEmail";
                    } else if (!TextUtils.isEmpty(data.googleId)) {
                        bindText = data.googleId;
                        bindType = "google";
                    }
                    if (!TextUtils.isEmpty(bindText) && !TextUtils.isEmpty(bindType)) {
                        EasyHttp.post(new ApplicationLifecycle())
                                .api(new WalletSocialApi().setParmas(ethWallet.getAddress(), bindType, bindText))
                                .request(new OnHttpListener<BaseBean<Object>>() {
                                    @Override
                                    public void onSucceed(BaseBean<Object> result) {
                                    }

                                    @Override
                                    public void onFail(Exception e) {
                                    }
                                });
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                loadTaskData();
                break;
        }
    }
}
