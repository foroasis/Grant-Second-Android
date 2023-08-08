package teleblock.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogUniversalBinding;

/**
 * Time:2023/3/1
 * Author:Perry
 * Description：通用对话框
 */
public class UniversalDialog extends Dialog {

    private DialogUniversalBinding binding;
    private Runnable runnable;

    public UniversalDialog(@NonNull Context context, Runnable runnable) {
        super(context, R.style.dialog2);
        this.runnable = runnable;
        binding = DialogUniversalBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.getRoot().setOnClickListener(view -> dismiss());
        binding.tvCancel.setText(LocaleController.getString("Cancel", R.string.Cancel));
        binding.tvConfirm.setText(LocaleController.getString("dialog_delete_wallet_confirm", R.string.dialog_delete_wallet_confirm));

        binding.tvCancel.setOnClickListener(view -> dismiss());
        binding.tvConfirm.setOnClickListener(view -> {
            runnable.run();
            dismiss();
        });
    }

    public UniversalDialog setTitle(String title) {
        binding.tvTitle.setText(title);
        return this;
    }
}
