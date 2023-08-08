package teleblock.ui.view.chatitem;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ViewChatItemGameHashSingledoubleBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.Map;

import teleblock.config.GameType;
import teleblock.ui.dialog.UserProfileDialog;
import teleblock.util.ViewUtil;
import teleblock.util.parse.ParseUtil;

/**
 * Time:2023/3/10
 * Author:Perry
 * Description：聊天页面游戏view
 */
public class ChatItemGameView extends LinearLayout {
    private ViewChatItemGameHashSingledoubleBinding binding;

    public ChatItemGameView(@NonNull Context context) {
        super(context);
        binding = ViewChatItemGameHashSingledoubleBinding.inflate(LayoutInflater.from(context), this, true);
        initView();
    }

    private void initView() {
        binding.tvGameNicknameTitle.setText(LocaleController.getString("game_type_title", R.string.game_type_title));
        binding.tvGameNicknameTitleType.setText(LocaleController.getString("type", R.string.type));
        binding.tvGameLaidPriceTitle.setText(LocaleController.getString("game_laid_price_title", R.string.game_laid_price_title));
        binding.tvGameLaidPriceTx.setText(LocaleController.getString("game_price_title", R.string.game_price_title));
        binding.tvGameLaidPriceTitleType.setText(LocaleController.getString("type", R.string.type));
        binding.tvHashValueTitle.setText(LocaleController.getString("game_hash_title", R.string.game_hash_title));
        binding.tvGameResultTitle.setText(LocaleController.getString("game_result_title", R.string.game_result_title));
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

        //游戏信息
        GameType.GameInfoType gameInfoType = GameType.getGameInfoByGameId(Integer.parseInt(parseMap.get("gameId")));
        //游戏别称
        binding.tvGameNickname.setText(gameInfoType.gameNickName);
        //游戏全称
        binding.tvGameName.setText(gameInfoType.gameName);
        //游戏图标
        binding.tvGameName.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(gameInfoType.gameIcon));

        //游戏下注类型
        String gameBetType = (String) gameInfoType.gameWays.get(String.valueOf(parseMap.get("gameType")));
        binding.tvType.setText(gameBetType);

        //下注金额
        String gameBetPrice = parseMap.get("price") + " " + parseMap.get("symbol");
        binding.tvGameLaidPrice.setText(gameBetPrice);

        //hash值
        binding.tvHashValue.setText(ViewUtil.hashStrLastNumberHighlight((String) parseMap.get("hash")));

        //游戏结果
        int gameStatus = Integer.parseInt(parseMap.get("gameStatus"));
        if (gameStatus == 2) {//中奖
            binding.ivTopImg.setImageResource(R.drawable.icon_win_top_bg);
            binding.tvGameWinOrLose.getHelper().setIconNormalTop(ResourceUtils.getDrawable(R.drawable.icon_message_win));
            binding.tvGameWinOrLose.setText(LocaleController.getString("game_wine_message", R.string.game_wine_message));
            binding.tvGameResultTips.setText(
                    String.format(LocaleController.getString("game_win_tips", R.string.game_win_tips)
                            , parseMap.get("winPrice") + " " + parseMap.get("symbol")
                    )
            );
            binding.tvGameResultTips.setTextColor(Color.parseColor("#FFFFAE12"));
            binding.tvGameResult.setText(LocaleController.getString("game_result_win", R.string.game_result_win));
        } else if (gameStatus == 3) {//未中奖
            binding.ivTopImg.setImageResource(R.drawable.icon_lose_top_bg);
            binding.tvGameWinOrLose.getHelper().setIconNormalTop(ResourceUtils.getDrawable(R.drawable.icon_message_lose));
            binding.tvGameWinOrLose.setText(LocaleController.getString("game_lose_message", R.string.game_lose_message));
            binding.tvGameResultTips.setText(LocaleController.getString("game_lose_tips", R.string.game_lose_tips));
            binding.tvGameResultTips.setTextColor(Color.parseColor("#FF828282"));
            binding.tvGameResult.setText(LocaleController.getString("game_result_lose", R.string.game_result_lose));
        }

        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvGameTime.setText(formatDate);
    }
}
