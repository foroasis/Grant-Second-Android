package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogErrorAddressBinding;

/**
 * Time:2022/12/7
 * Author:Perry
 * Description：支付错误提示框
 */
public class TransferErrorDialog extends Dialog {

    private DialogErrorAddressBinding binding;

    public TransferErrorDialog(@NonNull Context context, String errorStr) {
        super(context, R.style.dialog2);
        binding = DialogErrorAddressBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvTitle.getHelper().setTextColorNormal(Color.parseColor("#EB5757"));
        binding.tvTitle.setText("Error");
        binding.tvContent.setText(errorStr);
        binding.tvConfirm.setText(LocaleController.getString("ac_wallet_btn", R.string.ac_wallet_btn));

        binding.tvTitle.setOnClickListener(v -> dismiss());
        binding.tvConfirm.setOnClickListener(v -> dismiss());
    }
}
