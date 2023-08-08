package teleblock.ui.view.chatitem;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewChatItemMiniGameInviteBinding;
import org.telegram.ui.ChatActivity;

import java.text.SimpleDateFormat;
import java.util.Map;

import teleblock.util.parse.ParseUtil;

/**
 * Time:2023/5/6
 * Author:Perry
 * Description：小游戏邀请view
 */
public class ChatItemMiniGameInviteView extends LinearLayout {
    
    private ViewChatItemMiniGameInviteBinding binding;
    
    public ChatItemMiniGameInviteView(Context context) {
        super(context);
        binding = ViewChatItemMiniGameInviteBinding.inflate(LayoutInflater.from(context), this,true);
        initView();
    }

    private void initView() {
        binding.tvContent.setText(LocaleController.getString("view_chat_item_mini_game_inviting1", R.string.view_chat_item_mini_game_inviting1));
        binding.tvGameName.setText(LocaleController.getString("view_chat_item_jump_game", R.string.view_chat_item_jump_game));
    }

    public void setData(ChatActivity chatActivity, MessageObject message) {
        //获取聊天内容
        Map<String, String> parseMap = ParseUtil.parse(message.messageText.toString());
        //发送人ID
        long userId = Long.parseLong(parseMap.get("fromUserId"));
        //看这个消息是谁发的
        if (chatActivity.getUserConfig().getClientUserId() == userId) {//自己发的
            binding.getRoot().setGravity(Gravity.RIGHT);
        } else {
            binding.getRoot().setGravity(Gravity.LEFT);
        }

        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvGameTime.setText(formatDate);
    }

}
