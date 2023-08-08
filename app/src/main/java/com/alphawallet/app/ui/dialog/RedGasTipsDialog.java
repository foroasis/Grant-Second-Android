package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogGastipsBinding;
import org.telegram.messenger.databinding.DialogRedGastipsBinding;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：红包gas dialog
 */
public class RedGasTipsDialog extends Dialog {
    private DialogRedGastipsBinding binding;

    public RedGasTipsDialog(@NonNull Context context) {
        super(context, R.style.dialog2);
        binding = DialogRedGastipsBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        setCancelable(true);

        binding.tvTitle.setText(LocaleController.getString("red_gas_tips_dialog_title", R.string.red_gas_tips_dialog_title));
        binding.tvContent.setText(LocaleController.getString("red_gas_tips_dialog_content", R.string.red_gas_tips_dialog_content));

        binding.tvTitle.setOnClickListener(view -> dismiss());
    }
}
