package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogShelfNftBinding;

/**
 * Time:2023/6/1
 * Author:Perry
 * Description：下架nft
 */
public class ShelfNftDialog extends Dialog {
    private DialogShelfNftBinding binding;

    public ShelfNftDialog(@NonNull Context context, Runnable runnable) {
        super(context, R.style.dialog2);
        binding = DialogShelfNftBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvTitle.setText(LocaleController.getString("shelf_nft_title", R.string.shelf_nft_title));
        binding.tvBack.setText(LocaleController.getString("shelf_nft_back", R.string.shelf_nft_back));
        binding.tvConfirm.setText(LocaleController.getString("shelf_nft_confirm", R.string.shelf_nft_confirm));

        binding.tvBack.setOnClickListener(v -> dismiss());

        binding.tvConfirm.setOnClickListener(v -> {
            runnable.run();
            dismiss();
        });
    }
}
