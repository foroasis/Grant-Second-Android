package teleblock.ui.view.chatitem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ViewChatItemRedpacketBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.List;

import teleblock.database.KKVideoMessageDB;
import teleblock.ui.dialog.BonusReceiveDialog;
import teleblock.ui.dialog.UserProfileDialog;
import teleblock.util.ViewUtil;
import teleblock.util.WalletUtil;
import teleblock.util.parse.RedPacketParseUtil;

/**
 * Time:2023/3/9
 * Author:Perry
 * Description：聊天页面红包view
 */
public class ChatItemRedPacketView extends LinearLayout {

    private ViewChatItemRedpacketBinding binding;

    public ChatItemRedPacketView(Context context) {
        super(context);
        binding = ViewChatItemRedpacketBinding.inflate(LayoutInflater.from(context), this, true);
        binding.tvSeeRedpack.setText(LocaleController.getString("chat_activity_redpackets_see", R.string.chat_activity_redpackets_see));
        binding.tvType.setText(LocaleController.getString("chat_activity_redpackets_type", R.string.chat_activity_redpackets_type));
        binding.tvTitle.setText(String.format(LocaleController.getString("chat_activity_redpackets_title", R.string.chat_activity_redpackets_title), LocaleController.getString("app_name", R.string.app_name)));
    }

    public void setData(
            ChatActivity chatActivity,
            MessageObject message,
            ChatActivity.ChatActivityAdapter chatActivityAdapter,
            int position
    ) {
        TLRPC.Chat currentChat = chatActivity.getCurrentChat();

        String text = message.messageText.toString();
        //解析后的值
        List<String> parseList = RedPacketParseUtil.parse(text);
        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //发送人ID
        long fromUserId = Long.decode(parseList.get(2));
        //红包唯一表示标识
        String secret = parseList.get(3);

        //从数据库获取钱包状态
        int status = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).getRedpacketStatusData(secret);

        //红包点击事件
        binding.tvSeeRedpack.setOnClickListener(v -> {
            if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                ToastUtils.showLong(LocaleController.getString("chat_activity_no_group_people_redpacket", R.string.chat_activity_no_group_people_redpacket));
                return;
            }

            WalletUtil.getWalletInfo(wallet -> BonusReceiveDialog.showDialog(chatActivity, secret, new BonusReceiveDialog.BonusReceiveDialogListener() {
                @Override
                public void updateStatus() {
                    chatActivityAdapter.notifyItemChanged(position);
                }

                @Override
                public void notRobCount(int num) {
                }
            }));
        });


        if (chatActivity.isSingleChat()) {
            binding.flGroupAvatar.setVisibility(View.GONE);
        } else {
            binding.flGroupAvatar.setVisibility(View.VISIBLE);
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setInfo(currentChat);

            BackupImageView backupImageView = new BackupImageView(getContext());
            backupImageView.setRoundRadius(AndroidUtilities.dp(30f / 2f));
            backupImageView.setForUserOrChat(currentChat, avatarDrawable);
            binding.flGroupAvatar.addView(backupImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        if (parseList.size() < 6) {
            binding.ivCardBg.setImageResource(R.drawable.bonus_receive_list_bg);
        } else {
            Bitmap backgroundBit = null;
            try {
                //链ID
                long chainId = Long.parseLong(parseList.get(4));
                //币种符号
                String symbol = parseList.get(5);
                //背景图片名称
                String backgroudImgName = "image_red_pocket_" + chainId + symbol.toLowerCase();
                //红包背景图片
                backgroundBit = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(backgroudImgName, "drawable", getContext().getPackageName()));
            } catch (Exception e) {
            }
            if (backgroundBit == null) {
                backgroundBit = BitmapFactory.decodeResource(getResources(), R.drawable.bonus_receive_list_bg);
            }
            binding.ivCardBg.setImageBitmap(backgroundBit);
        }


        //获取发送人信息
        TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(fromUserId);
        //最左边的头像
        binding.flAvatarLeft.removeAllViews();
        //点击弹出用户信息
        binding.flAvatarLeft.setOnClickListener(v -> {
            new UserProfileDialog(chatActivity, fromUserId).show();
        });
        binding.flAvatarLeft.setVisibility(chatActivity.getUserConfig().getClientUserId() == fromUserId || chatActivity.isSingleChat() ? View.GONE : View.VISIBLE);
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

        //看这个红包是谁发的
        if (chatActivity.getUserConfig().getClientUserId() == fromUserId) {//自己发的
            binding.ivRightIcon.setVisibility(View.VISIBLE);
            binding.ivLeftIcon.setVisibility(View.GONE);
            binding.rlBottomView.getHelper().setCornerRadiusBottomLeft(SizeUtils.dp2px(9f)).setCornerRadiusBottomRight(0);
            binding.getRoot().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        } else {
            binding.ivLeftIcon.setVisibility(View.VISIBLE);
            binding.ivRightIcon.setVisibility(View.GONE);
            binding.rlBottomView.getHelper().setCornerRadiusBottomRight(SizeUtils.dp2px(9f)).setCornerRadiusBottomLeft(0);
            binding.getRoot().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        }

        //设置红包提示文字
        ViewUtil.setRedPacketContentStyle(binding.tvContent, status, UserObject.getUserName(user));
        if (status == 888) {
            binding.rlBottomView.getHelper().setBackgroundColorNormal(Color.parseColor("#D4D4D4"));
            binding.llContentView.setBackgroundColor(Color.parseColor("#D4D4D4"));
            binding.vLine.setBackgroundColor(Color.parseColor("#C9C9C9"));
            binding.vCardBg.setVisibility(View.VISIBLE);

            Drawable left_raw_icon = ResourceUtils.getDrawable(R.drawable.redpacket_left_horn_gray);
//                        left_raw_icon.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#D4D4D4"), PorterDuff.Mode.MULTIPLY));
            binding.ivLeftIcon.setImageDrawable(left_raw_icon);

            Drawable right_raw_icon = ResourceUtils.getDrawable(R.drawable.redpacket_right_horn_gray);
//                        right_raw_icon.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#D4D4D4"), PorterDuff.Mode.MULTIPLY));
            binding.ivRightIcon.setImageDrawable(right_raw_icon);

        } else {
            binding.rlBottomView.getHelper().setBackgroundColorNormal(Color.WHITE);
            binding.llContentView.setBackgroundColor(Color.WHITE);
            binding.vLine.setBackgroundColor(Color.parseColor("#F2F2F2"));
            binding.vCardBg.setVisibility(View.GONE);

            Drawable left_raw_icon = ResourceUtils.getDrawable(R.drawable.redpacket_left_horn);
            left_raw_icon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
            binding.ivLeftIcon.setImageDrawable(left_raw_icon);

            Drawable right_raw_icon = ResourceUtils.getDrawable(R.drawable.redpacket_right_horn);
            right_raw_icon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
            binding.ivRightIcon.setImageDrawable(right_raw_icon);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvTime.setText(formatDate);
    }
}
