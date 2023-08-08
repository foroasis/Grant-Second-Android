package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.ruffian.library.widget.RFrameLayout;
import com.ruffian.library.widget.RTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogInviteFriendConfirmBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

import teleblock.model.InviteConfigEntity;

/**
 * 创建日期：2023/3/9
 * 描述：欢迎红包弹窗
 */
public class WelcomeBonusDialog extends BaseAlertDialog {

    private DialogInviteFriendConfirmBinding binding;
    private InviteConfigEntity.Level level;
    private List<TLRPC.User> users;

    public WelcomeBonusDialog(Context context, InviteConfigEntity.Level level, List<TLRPC.User> users, OnCloseListener onCloseListener) {
        super(context, onCloseListener);
        this.level = level;
        this.users = users;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogInviteFriendConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        initView();
        initData();
    }

    private void initView() {
        binding.tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseListener.onClose(null);
                dismiss();
            }
        });
        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.tvTitle.setText(LocaleController.getString("invite_friend_confirm_title", R.string.invite_friend_confirm_title));
        binding.tvAmountTitle.setText(LocaleController.getString("invite_friend_confirm_amount_title", R.string.invite_friend_confirm_amount_title));
        binding.tvSendFriendsTitle.setText(LocaleController.getString("invite_friend_confirm_send_friends_title", R.string.invite_friend_confirm_send_friends_title));
        binding.tvReceiveTip.setText(LocaleController.getString("invite_friend_confirm_receive_tip", R.string.invite_friend_confirm_receive_tip));
        binding.tvSend.setText(LocaleController.getString("invite_friend_confirm_sennd_text", R.string.invite_friend_confirm_sennd_text));
        binding.tvCancel.setText(LocaleController.getString("invite_friend_confirm_cancel_text", R.string.invite_friend_confirm_cancel_text));
    }

    private void initData() {
        binding.tvAmount.setText(level.amount + " " + level.currency_name);
        ViewGroup.LayoutParams layoutParams = LayoutHelper.createLinear(36, 36, Gravity.CENTER, -8, 0, 0, 0);
        for (int i = 0; i < users.size(); i++) {
            if (i == 7) {
                binding.llSendFriends.addView(createMoreView(users.size() - 7), layoutParams);
                break;
            } else if (i == 0) {
                binding.llSendFriends.addView(createUserView(users.get(i)), LayoutHelper.createLinear(36, 36));
            } else {
                binding.llSendFriends.addView(createUserView(users.get(i)), layoutParams);
            }
        }
    }

    private View createMoreView(int number) {
        RTextView rTextView = new RTextView(getContext());
        rTextView.getHelper().setBackgroundColorNormal(Color.WHITE).setCornerRadius(SizeUtils.dp2px(20));
        rTextView.setTextSize(13);
        rTextView.setTextColor(Color.parseColor("#000000"));
        rTextView.setGravity(Gravity.CENTER);
        rTextView.setText(number + "");
        rTextView.setIncludeFontPadding(false);
        return rTextView;
    }

    private View createUserView(TLRPC.User user) {
        RFrameLayout layout = new RFrameLayout(getContext());
        layout.getHelper().setBorderColorNormal(Color.WHITE).setBorderWidthNormal(AndroidUtilities.dp(2)).setCornerRadius(SizeUtils.dp2px(20));
        BackupImageView avatarImageView = new BackupImageView(getContext());
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(20));
        layout.addView(avatarImageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER));
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        avatarImageView.setForUserOrChat(user, avatarDrawable);
        avatarImageView.drawNftView = false;
        return layout;
    }
}