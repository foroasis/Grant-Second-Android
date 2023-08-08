package teleblock.ui.view.chatitem;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ViewChatItemMonneyLeftBinding;
import org.telegram.ui.ChatActivity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import teleblock.ui.dialog.TransferDetatilsDialog;
import teleblock.util.parse.TransferParseUtil;
import teleblock.util.WalletUtil;

/**
 * Time:2023/3/9
 * Author:Perry
 * Description：转账页面view
 */
public class ChatItemTransferView extends LinearLayout {

    private ViewChatItemMonneyLeftBinding binding;

    public ChatItemTransferView(Context context) {
        super(context);
        binding = ViewChatItemMonneyLeftBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setData(
            ChatActivity chatActivity,
            MessageObject message
    ) {
        String text = message.messageText.toString();
        //解析后的值
        List<String> parseList = TransferParseUtil.parse(text);
        //发送人ID
        long fromUserId = Long.decode(parseList.get(1));
        //消息时间戳 单位秒
        long time = message.messageOwner.date;

        //点击显示转账详情对话框
        binding.rclContentview.setOnClickListener(view1 -> {
            new TransferDetatilsDialog(getContext(), time, parseList).show();
        });

        //转账圆角，用tg的圆角
        float cornerRadius = SizeUtils.dp2px(SharedConfig.bubbleRadius);

        //看这个转账的是向谁转账的
        if (chatActivity.getUserConfig().getClientUserId() == fromUserId) { //自己转的
            binding.ivRightIcon.setVisibility(View.VISIBLE);
            binding.ivLeftIcon.setVisibility(View.GONE);
            binding.rclContentview.getHelper()
                    .setCornerRadiusTopLeft(cornerRadius)
                    .setCornerRadiusTopRight(cornerRadius)
                    .setCornerRadiusBottomLeft(cornerRadius)
                    .setCornerRadiusBottomRight(0);
            binding.getRoot().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
            binding.tvName.setText(
                    String.format(
                            LocaleController.getString("chat_transfer_towhotransfer", R.string.chat_transfer_towhotransfer),
                            UserObject.getUserName(chatActivity.getCurrentUser())
                    )
            );
        } else {
            binding.ivRightIcon.setVisibility(View.GONE);
            binding.ivLeftIcon.setVisibility(View.VISIBLE);
            binding.rclContentview.getHelper()
                    .setCornerRadiusTopLeft(cornerRadius)
                    .setCornerRadiusTopRight(cornerRadius)
                    .setCornerRadiusBottomRight(cornerRadius)
                    .setCornerRadiusBottomLeft(0);
            binding.tvName.setText(LocaleController.getString("chat_transfer_toyoutransfer", R.string.chat_transfer_toyoutransfer));
            binding.getRoot().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        }

        //时间
        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(time * 1000);
        binding.tvTime.setText(formatDate);

        //金额
        BigDecimal priceDecimal = new BigDecimal(parseList.get(7));
        if (WalletUtil.decimalCompareTo(priceDecimal, new BigDecimal("0.00001"))) {
            binding.tvPrice.setText(priceDecimal.toPlainString());
        } else {
            binding.tvPrice.setText("< 0.00001");
        }

        //币种单位
        binding.tvCoinType.setText(parseList.get(3));
    }
}
