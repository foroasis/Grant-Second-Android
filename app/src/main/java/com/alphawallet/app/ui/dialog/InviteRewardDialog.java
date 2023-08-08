package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.DialogInviteRewardBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BonusGetEntity;
import teleblock.model.InviteRewardEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.ReceivePullNewBounsApi;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * 创建日期：2023/3/9
 * 描述：邀请奖励弹窗
 */
public class InviteRewardDialog extends BaseAlertDialog {

    private DialogInviteRewardBinding binding;
    private BaseFragment baseFragment;
    private InviteRewardEntity inviteReward;

    public InviteRewardDialog(BaseFragment baseFragment, InviteRewardEntity inviteReward) {
        super(baseFragment.getContext());
        this.baseFragment = baseFragment;
        this.inviteReward = inviteReward;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        binding = DialogInviteRewardBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        setCanceledOnTouchOutside(false);
        initView();
        initData();
    }

    private void initView() {
        binding.tvCreateWallet.setOnClickListener(v -> WalletUtil.getWalletInfo(wallet -> new WalletListDialog(baseFragment).show()));

        binding.tvTitle.setText(LocaleController.getString("invite_reward_dialog_title", R.string.invite_reward_dialog_title));
        TLRPC.User user = baseFragment.getMessagesController().getUser(Long.valueOf(inviteReward.send_tg_user_id));
        String username = user != null ? UserObject.getUserName(user) : LocaleController.getString("invite_reward_dialog_friend", R.string.invite_reward_dialog_friend);
        binding.tvDesc.setText(String.format(LocaleController.getString("invite_reward_dialog_desc", R.string.invite_reward_dialog_desc), username));
        binding.tvCreateWallet.setText(LocaleController.getString("invite_reward_dialog_create_wallet", R.string.invite_reward_dialog_create_wallet));
    }

    private void receiveReward() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new ReceivePullNewBounsApi()
                        .setPromotion_number(inviteReward.promotion_number)
                        .setReceipt_account(WalletDaoUtils.getCurrent().getAddress()))
                .request(new OnHttpListener<BaseBean<Object>>() {
                    @Override
                    public void onSucceed(BaseBean<Object> result) {
                        if (result.getCode() == 10010) {
                            ToastUtils.showLong(result.getMessage());
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                    }
                });
    }

    private void initData() {
        binding.tvAmount.setText(inviteReward.amount + " " + inviteReward.symbol);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CREATED:
            case EventBusTags.WALLET_CHANGED:
                receiveReward();
                dismiss();
                break;
        }
    }
}