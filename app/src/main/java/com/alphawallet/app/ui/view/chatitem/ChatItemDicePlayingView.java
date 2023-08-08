package teleblock.ui.view.chatitem;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ResourceUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ViewChatItemDicePlayingBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import teleblock.ui.dialog.UserProfileDialog;
import teleblock.util.JsonUtil;
import teleblock.util.ViewUtil;
import teleblock.util.parse.ParseUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/3/22
 * Author:Perry
 * Description：骰子玩法item
 */
public class ChatItemDicePlayingView extends LinearLayout {
    private ViewChatItemDicePlayingBinding binding;

    public ChatItemDicePlayingView(Context context) {
        super(context);
        binding = ViewChatItemDicePlayingBinding.inflate(LayoutInflater.from(context), this,true);
        initView();
    }

    private void initView() {
        binding.tvHashValueTitle.setText(LocaleController.getString("game_hash_title", R.string.game_hash_title));
        binding.tvDicePointTitle.setText(LocaleController.getString("dice_point_title", R.string.dice_point_title));
        binding.tvGameName.setText(LocaleController.getString("dice_playing_name", R.string.dice_playing_name));
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

        //hash值
        binding.tvHashValue.setText(ViewUtil.hashStrLast2NumberHighlight((String) parseMap.get("hash")));
        //骰子点数
        List<Integer> points = JsonUtil.parseJsonToList(String.valueOf(parseMap.get("dicePoints")), Integer.class);

        //骰子点数总和
        int pointsSum = 0;
        for (int i = 0; i < points.size(); i++) {
            pointsSum = points.get(i) + pointsSum;
            if (i == 0) {
                loadGif(binding.ivDiceContainer1, points.get(i));
            } else if (i == 1) {
                loadGif(binding.ivDiceContainer2, points.get(i));
            } else if (i == 2){
                loadGif(binding.ivDiceContainer3, points.get(i));
            }
        }
        //骰子数总和
        binding.tvDicePoint.setText(String.valueOf(pointsSum));

        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvGameTime.setText(formatDate);
    }

    private void loadGif(ImageView iv, int value) {
        RequestOptions options = new RequestOptions()
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.DATA);

        Glide.with(getContext())
                .asGif()
                .load(ResourceUtils.getDrawableIdByName("dice_result_gif_" + value))
                .apply(options)
                .addListener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            if (value != 0) {
                                resource.setLoopCount(1); //设置次数
                            }
                        }
                        return false;
                    }
                }).into(iv);
    }
}
