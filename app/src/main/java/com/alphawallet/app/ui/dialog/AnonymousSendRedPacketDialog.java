package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogAnonymousSendRedpacketBinding;

/**
 * Time:2023/1/29
 * Author:Perry
 * Description：匿名状态下 发送红包提示
 */
public class AnonymousSendRedPacketDialog extends Dialog {

    private DialogAnonymousSendRedpacketBinding binding;

    public AnonymousSendRedPacketDialog(@NonNull Context context) {
        super(context, R.style.dialog2);
        binding = DialogAnonymousSendRedpacketBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvContent.setText(LocaleController.getString("dialog_anonymous_red_packet_content", R.string.dialog_anonymous_red_packet_content));
        binding.tvBtn.setText(LocaleController.getString("dialog_anonymous_red_packet_know", R.string.dialog_anonymous_red_packet_know));

        binding.tvBtn.setOnClickListener(view -> dismiss());
    }
}
