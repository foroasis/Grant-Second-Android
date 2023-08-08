package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActTaskRewardBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;

import okhttp3.Call;
import teleblock.manager.PayerGroupManager;
import teleblock.model.OasisNftInfoEntity;
import teleblock.model.PrivateGroupEntity;
import teleblock.model.RewardInfoEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.OasisNftInfoApi;
import teleblock.network.api.RewardInfoApi;
import teleblock.network.api.RewardReceiveApi;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2023/3/20
 * 描述：任务奖励
 */
public class TaskRewardAct extends BaseFragment {

    private ActTaskRewardBinding binding;
    private String airdropId;
    private String link;

    private OasisNftInfoEntity mOasisNftInfoEntity;
    private PrivateGroupEntity private_group;

    public TaskRewardAct(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        airdropId = getArguments().getString("id");
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public boolean isLightStatusBar() {
        return false;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActTaskRewardBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.ivBack.setOnClickListener(view -> finishFragment());
        binding.tvRewardReceive.setOnClickListener(v -> {
            if (airdropId.equals("AC00006")) {
                if (LocaleController.getString("JoinGroup", R.string.JoinGroup).equals(binding.tvRewardReceive.getText().toString())) {
                    if (private_group != null) {
                        EventUtil.track(getContext(), EventUtil.Even.OasisNft领取后加群, new HashMap<>());
                        PayerGroupManager.getInstance(UserConfig.selectedAccount).handleShopInfo(this, private_group);
                    }
                } else {
                    TLRPC.Chat chat = getMessagesController().getChat(Math.abs(MMKVUtil.getSystemMsg().oasis_chat_id));
                    if (ChatObject.isChannel(chat) && !chat.creator && ChatObject.isNotInChat(chat)) {
                        ToastUtils.showLong(LocaleController.getString("airdrop_mint_tips1", R.string.airdrop_mint_tips1));
                    } else {
                        //领取nft
                        receiveReward();
                    }
                }
            } else if (!StringUtils.isEmpty(link)) {
                if (link.startsWith("https://") || link.startsWith("http://")) {
                    Browser.openUrl(getContext(), link);
                } else if (link.startsWith("alphagram://")) {
                    String roteKeywords = link.replace("alphagram://", "");
                    if (roteKeywords.startsWith("GrantTaskDetail?chatId=")) {
                        //领取did身份
                        long chatId = Long.parseLong(roteKeywords.split("=")[1]);
                        Bundle args = new Bundle();
                        args.putLong("chat_id", chatId);
                        presentFragment(new GrantTaskDetailAct(args));
                    }
                }
            } else {
                //签到
                EventUtil.track(getContext(), EventUtil.Even.签到按钮点击, new HashMap<>());
                receiveReward();
            }
        });
    }

    private void initData() {
        EasyHttp.cancel(this.getClass().getSimpleName());
        EasyHttp.post(new ApplicationLifecycle())
                .api(new RewardInfoApi().setId(airdropId))
                .tag(this.getClass().getSimpleName())
                .request(new OnHttpListener<BaseBean<RewardInfoEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<RewardInfoEntity> result) {
                        RewardInfoEntity rewardInfo = result.getData();
                        if (rewardInfo != null) {
                            link = rewardInfo.link;

                            binding.tvRewardTitle.setText(rewardInfo.title);
                            binding.tvRewardDesc.setText(rewardInfo.description);
                            binding.tvRewardAmount.setText(rewardInfo.reward);

                            if (rewardInfo.id.equals("AC00006")) {
                                private_group = rewardInfo.private_group;

                                EasyHttp.post(new ApplicationLifecycle())
                                        .api(new OasisNftInfoApi())
                                        .request(new OnHttpListener<BaseBean<OasisNftInfoEntity>>() {
                                            @Override
                                            public void onSucceed(BaseBean<OasisNftInfoEntity> result) {
                                                mOasisNftInfoEntity = result.getData();
                                            }

                                            @Override
                                            public void onFail(Exception e) {
                                            }

                                            @Override
                                            public void onEnd(Call call) {
                                                if (mOasisNftInfoEntity != null && mOasisNftInfoEntity.getNft_token_id() != 0) {
                                                    binding.tvRewardReceive.setText(LocaleController.getString("JoinGroup", R.string.JoinGroup));
                                                } else {
                                                    binding.tvRewardReceive.setText(rewardInfo.button);
                                                }
                                            }
                                        });
                            } else {
                                binding.tvRewardReceive.setText(rewardInfo.button);
                            }
                            if (StringUtils.isEmpty(link)) {
                                binding.tvRewardDesc.setGravity(Gravity.CENTER);
                                binding.ivTopBg.setImageResource(R.drawable.image_background_mission);
                                binding.ivIcon.setImageResource(R.drawable.reward_receive_success_ic);
                            } else {
                                binding.tvRewardDesc.setGravity(Gravity.START);
                                GlideHelper.displayImage(binding.ivTopBg.getContext(), binding.ivTopBg, rewardInfo.background_image);
                                GlideHelper.displayImage(binding.ivIcon.getContext(), binding.ivIcon, rewardInfo.icon);
                            }

                            if (rewardInfo.id.equals("AC00006")) {
                                EventUtil.track(getContext(), EventUtil.Even.OasisNft首页活动入口, new HashMap<>());
                                binding.tvRewardDesc.setGravity(Gravity.START);
                            }

                            if (rewardInfo.id.equals("AC00001")) {
                                EventUtil.track(getContext(), EventUtil.Even.签到页面展示, new HashMap<>());
                            }

                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }
                });
    }

    private void receiveReward() {
        EventUtil.track(getContext(), EventUtil.Even.OasisNft领取按钮点击, new HashMap<>());
        RewardReceiveApi requestApi = new RewardReceiveApi();
        requestApi.setId(airdropId);
        if (airdropId.equals("AC00006")) {
            ETHWallet ethWallet = WalletDaoUtils.getCurrent();
            if (ethWallet != null && ethWallet.getChainId() == 99999) {
                new WalletListDialog(this).show();
                ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
                return;
            }
            requestApi.setReceipt_account(ethWallet.getAddress());
            requestApi.setTg_user_name(getUserConfig().getCurrentUser().username);
        }

        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCancel(false);
        showDialog(progressDialog);
        EasyHttp.post(new ApplicationLifecycle())
                .api(requestApi)
                .request(new OnHttpListener<BaseBean<String>>() {
                    @Override
                    public void onSucceed(BaseBean<String> result) {
                        ToastUtils.showLong(result.getMessage());
                        if (result.isRequestSucceed() && airdropId.equals("AC00006")) {
                            binding.tvRewardReceive.setText(LocaleController.getString("JoinGroup", R.string.JoinGroup));
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (!TextUtils.isEmpty(e.getMessage())) {
                            ToastUtils.showLong(e.getMessage());
                        }
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                        if (!airdropId.equals("AC00006")) {
                            finishFragment();
                        }
                    }
                });
    }
}