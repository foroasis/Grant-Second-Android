package teleblock.ui.view.chatitem;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ViewChatItemPromotionBonusBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import teleblock.database.KKVideoMessageDB;
import teleblock.model.BonusStatusEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.PromotionBounsStatusApi;
import teleblock.network.api.ReceivePromotionBonusApi;
import teleblock.ui.activity.InviteReceiveDetailAct;
import teleblock.ui.activity.WelcomeBonusFirstAct;
import teleblock.ui.dialog.InviteReceiveLimitDialog;
import teleblock.ui.dialog.UserProfileDialog;
import teleblock.util.EventUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.util.parse.ParseUtil;

/**
 * Time:2023/3/16
 * Author:Perry
 * Description：欢迎红包样式
 */
public class ChatItemPromotionBounsView extends LinearLayout {
    private ViewChatItemPromotionBonusBinding binding;

    public ChatItemPromotionBounsView(Context context) {
        super(context);
        binding = ViewChatItemPromotionBonusBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setData(
            ChatActivity chatActivity,
            MessageObject message,
            ChatActivity.ChatActivityAdapter chatActivityAdapter,
            int position
    ) {
        //获取聊天内容
        Map<String, String> parseMap = ParseUtil.parse(message.messageText.toString());
        //发送人ID
        long userId = Long.parseLong(parseMap.get("fromUserId"));
        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //红包唯一表示标识
        String secret = parseMap.get("secretKey");
        //从数据库获取红包状态 1=发布待上链确认,2=上链确认,3=时间到期关闭,4=异常关闭,5=已经抢购完毕 888已经领取
        int status = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).getRedpacketStatusData(secret);

        //红包点击
        binding.rclContentview.setOnClickListener(view -> {
            //跳转到红包详情页面
            if (chatActivity.getUserConfig().getClientUserId() == userId) {
                indexBonusDetail(chatActivity, true);
            } else {//对方发的消息
                if (status == 1 || status == 2 || status == 0) {
                    WalletUtil.getWalletInfo(wallet -> {
                        clickBounsOpera(chatActivity, secret, () -> chatActivityAdapter.notifyItemChanged(position));
                    });
                } else {
                    indexBonusDetail(chatActivity, false);
                }
            }
        });

        //看这个消息是谁发的
        if (chatActivity.getUserConfig().getClientUserId() == userId) {//自己发的
            binding.getRoot().setGravity(Gravity.RIGHT);
            binding.groupViewsForm.setVisibility(VISIBLE);
            binding.groupViewsTo.setVisibility(GONE);

            binding.tvFromBounsTitle.setText(LocaleController.getString("view_chat_item_pullnew_bouns_from_title", R.string.view_chat_item_pullnew_bouns_from_title));
            binding.tvCoinPrice.setText(parseMap.get("totalMoney") + " " + parseMap.get("symbol"));
            binding.tvCoinNum.setText(String.format(LocaleController.getString("view_chat_item_pullnew_bouns_from_num", R.string.view_chat_item_pullnew_bouns_from_num), parseMap.get("totalNum")));
            binding.tvTitle.setText(LocaleController.getString("view_chat_item_pullnew_bouns_title2", R.string.view_chat_item_pullnew_bouns_title2));
            binding.ivRedpacketStatus.setImageResource(R.drawable.pullnew_redpacket_unclick);
            binding.vTopView.setAlpha(1f);
        } else {
            binding.getRoot().setGravity(Gravity.LEFT);
            binding.groupViewsForm.setVisibility(GONE);
            binding.groupViewsTo.setVisibility(VISIBLE);
            binding.tvToBounsTitle.setText(LocaleController.getString("view_chat_item_pullnew_bouns_to_title", R.string.view_chat_item_pullnew_bouns_to_title));
            if (status == 888 || status == 3 || status == 5) {
                binding.tvBounsStatus.setVisibility(VISIBLE);
                binding.tvTitle.setText(LocaleController.getString("view_chat_item_pullnew_bouns_title3", R.string.view_chat_item_pullnew_bouns_title3));
                binding.ivRedpacketStatus.setImageResource(R.drawable.pullnew_redpacket_click);
                binding.vTopView.setAlpha(0.8f);
                if (status == 888) { //已经领取
                    binding.tvBounsStatus.setText(LocaleController.getString("view_chat_item_pullnew_bouns_status3", R.string.view_chat_item_pullnew_bouns_status3));
                } else if (status == 3) {//过期
                    binding.tvBounsStatus.setText(LocaleController.getString("view_chat_item_pullnew_bouns_status2", R.string.view_chat_item_pullnew_bouns_status2));
                } else if (status == 5) {//领完
                    binding.tvBounsStatus.setText(LocaleController.getString("view_chat_item_pullnew_bouns_status1", R.string.view_chat_item_pullnew_bouns_status1));
                }
            } else {
                binding.vTopView.setAlpha(1f);
                binding.tvBounsStatus.setVisibility(GONE);
                binding.tvTitle.setText(LocaleController.getString("view_chat_item_pullnew_bouns_title1", R.string.view_chat_item_pullnew_bouns_title1));
                binding.ivRedpacketStatus.setImageResource(R.drawable.pullnew_redpacket_unclick);
            }
        }

        //获取发送人信息
        TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(userId);
        //最左边的头像
        binding.flAvatarLeft.removeAllViews();
        //点击弹出用户信息
        binding.flAvatarLeft.setOnClickListener(v -> {
            new UserProfileDialog(chatActivity, userId).show();
        });
        binding.flAvatarLeft.setVisibility(chatActivity.getUserConfig().getClientUserId() == userId || chatActivity.isSingleChat() ? View.GONE : View.VISIBLE);

        //头像显示状态
        if (binding.flAvatarLeft.getVisibility() == View.VISIBLE) {
            //tg原生头像
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(user);
            BackupImageView avatarImageView = new BackupImageView(getContext());
            avatarImageView.setRoundRadius(AndroidUtilities.dp(40));
            avatarImageView.setForUserOrChat(user, avatarDrawable);
            avatarImageView.drawNftView = false;
            binding.flAvatarLeft.addView(avatarImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvTime.setText(formatDate);
    }

    /**
     * 点击红包操作
     *
     * @param secret
     */
    private void clickBounsOpera(ChatActivity chatActivity, String secret, Runnable runnable) {
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCancel(false);
        progressDialog.show();

        //获取红包状态
        EasyHttp.post(new ApplicationLifecycle())
                .tag(chatActivity.getClass().getSimpleName())
                .api(new PromotionBounsStatusApi().setSecret_num(secret))
                .request(new OnHttpListener<BaseBean<BonusStatusEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<BonusStatusEntity> result) {
                        if (result.getData() != null) {
                            BonusStatusEntity bonusStatusEntity = result.getData();
                            if (bonusStatusEntity.is_get) {//已领取
                                KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertRedpacketStatusData(secret, 888);
                                runnable.run();
                                progressDialog.dismiss();
                            } else {//未领取
                                if (bonusStatusEntity.status == 1 || bonusStatusEntity.status == 2) {
                                    String address = WalletDaoUtils.getCurrent().getAddress().toLowerCase();
                                    if (bonusStatusEntity.chain_id == 999) { // 波场链
                                        if (!WalletUtil.isTronAddress(address)) {
                                            ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_tron_address", R.string.dialog_bonus_receive_select_tron_address));
                                            return;
                                        }
                                    } else if (bonusStatusEntity.chain_id == 99999) { // Solana链
                                        if (!WalletUtil.isSolanaAddress(address)) {
                                            ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_solana_address", R.string.dialog_bonus_receive_select_solana_address));
                                            return;
                                        }
                                    } else { // EVM链
                                        if (!WalletUtil.isEvmAddress(address)) {
                                            ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_evm_address", R.string.dialog_bonus_receive_select_evm_address));
                                            return;
                                        }
                                    }
                                    //领取红包
                                    EasyHttp.post(new ApplicationLifecycle())
                                            .tag(chatActivity.getClass().getSimpleName())
                                            .api(new ReceivePromotionBonusApi().setSecret_num(secret).setReceipt_account(address))
                                            .request(new OnHttpListener<BaseBean<Object>>() {
                                                @Override
                                                public void onSucceed(BaseBean<Object> result) {
                                                    if (result.getCode() == 140001 || result.getCode() == 14002) {
                                                        new InviteReceiveLimitDialog(getContext(), result.getCode(), result.getMessage(), () -> {
                                                            EventUtil.track(getContext(), EventUtil.Even.欢迎红包入口点击, new HashMap<>());
                                                            WalletUtil.getWalletInfo(wallet -> chatActivity.presentFragment(new WelcomeBonusFirstAct()));
                                                        }).show();
                                                    } else {
                                                        int status = 888; // 红包已抢到
                                                        if (result.getCode() == 10011) {
                                                            status = 5; // 红包已抢完
                                                        } else if (result.getCode() == 10012) {
                                                            status = 888; // 红包已抢过
                                                        } else if (result.getCode() == 10013) {
                                                            status = 3; // 红包已过期
                                                        }
                                                        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertRedpacketStatusData(secret, status);
                                                        runnable.run();

                                                        if (result.isRequestSucceed()) {
                                                            ToastUtils.showLong(LocaleController.getString("view_chat_item_pullnew_bouns_receive_successful", R.string.view_chat_item_pullnew_bouns_receive_successful));
                                                            indexBonusDetail(chatActivity, false);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFail(Exception e) {
                                                    ToastUtils.showLong(e.getMessage());
                                                }

                                                @Override
                                                public void onEnd(Call call) {
                                                    progressDialog.dismiss();
                                                }
                                            });
                                } else {
                                    KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertRedpacketStatusData(secret, bonusStatusEntity.status);
                                    runnable.run();
                                    progressDialog.dismiss();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        progressDialog.dismiss();
                        ToastUtils.showLong(e.getMessage());
                    }
                });
    }

    private void indexBonusDetail(ChatActivity chatActivity, boolean isUserSelf) {
        //直接跳转到红包详情
        Bundle args = new Bundle();
        args.putBoolean("isUserSelf", isUserSelf);
        args.putBoolean("formWelcomeBonus", true);
        chatActivity.presentFragment(new InviteReceiveDetailAct(args));
    }
}