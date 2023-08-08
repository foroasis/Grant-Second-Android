package teleblock.ui.view.chatitem;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.databinding.ViewChatItemNftBinding;
import org.telegram.ui.ChatActivity;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

import teleblock.model.wallet.NFTInfo;
import teleblock.ui.activity.nft.NftOrderDetailsActivity;
import teleblock.util.JsonUtil;
import teleblock.util.parse.ParseUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/6/1
 * Author:Perry
 * Description：nft样式view
 */
public class ChatItemNftView extends LinearLayout {

    private ViewChatItemNftBinding binding;

    public ChatItemNftView(Context context) {
        super(context);
        binding = ViewChatItemNftBinding.inflate(LayoutInflater.from(context), this,true);
        binding.tvTitle.setText(LocaleController.getString("nft_order", R.string.nft_order));
    }

    public void setData(ChatActivity chatActivity, MessageObject message) {
        //获取聊天内容
        Map<String, String> parseMap = ParseUtil.parse(message.messageText.toString());
        //发送人ID
        long userId = Long.parseLong(Objects.requireNonNull(parseMap.get("fromUserId")));
        //nft信息
        NFTInfo nftInfo = JsonUtil.parseJsonToBean(parseMap.get("nftInfo"), NFTInfo.class);
        //nft头像
        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);
        //nft名称
        binding.tvContent.setText(nftInfo.name);

        binding.getRoot().setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("nft_info", nftInfo);
            bundle.putString("hash", parseMap.get("hash"));
            bundle.putLong("dialog_id", chatActivity.getDialogId());
            bundle.putBoolean("bubble_enter", true);
            chatActivity.presentFragment(new NftOrderDetailsActivity(bundle));
        });

        //用tg的圆角
        float cornerRadius = SizeUtils.dp2px(SharedConfig.bubbleRadius);
        binding.llTopView.getHelper().setCornerRadiusTopLeft(cornerRadius).setCornerRadiusTopRight(cornerRadius).setCornerRadiusBottomRight(0).setCornerRadiusBottomLeft(0);

        //看这个消息是谁发的
        if (chatActivity.getUserConfig().getClientUserId() == userId) {//自己发的
            binding.ivRightIcon.setVisibility(View.VISIBLE);
            binding.ivLeftIcon.setVisibility(View.GONE);

            binding.rlBottomView.getHelper()
                    .setCornerRadiusTopLeft(0)
                    .setCornerRadiusTopRight(0)
                    .setCornerRadiusBottomLeft(cornerRadius)
                    .setCornerRadiusBottomRight(0);
            binding.getRoot().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        } else {
            binding.ivRightIcon.setVisibility(View.GONE);
            binding.ivLeftIcon.setVisibility(View.VISIBLE);

            binding.rlBottomView.getHelper()
                    .setCornerRadiusTopLeft(0)
                    .setCornerRadiusTopRight(0)
                    .setCornerRadiusBottomRight(cornerRadius)
                    .setCornerRadiusBottomLeft(0);
            binding.getRoot().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        }

        //消息时间戳 单位秒
        long time = message.messageOwner.date;
        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvTime.setText(formatDate);
    }

}
