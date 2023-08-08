package teleblock.ui.popup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.PopupDeleteWalletBinding;

import razerdp.basepopup.BasePopupWindow;
import razerdp.util.animation.AlphaConfig;
import razerdp.util.animation.AnimationHelper;
import razerdp.util.animation.ScaleConfig;

/**
 * Time:2022/12/28
 * Author:Perry
 * Description：删除钱包窗口
 */
public class DeleteWalletPopup extends BasePopupWindow {

    private PopupDeleteWalletBinding binding;

    private DeleteWalletPopupListener listener;

    public DeleteWalletPopup(Dialog dialog, DeleteWalletPopupListener listener) {
        super(dialog);
        this.listener = listener;
        binding = PopupDeleteWalletBinding.inflate(LayoutInflater.from(dialog.getContext()));
        setContentView(binding.getRoot());
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onViewCreated(@NonNull View contentView) {
        super.onViewCreated(contentView);
        binding.tvDeleteWallet.setText(LocaleController.getString("ac_wallet_delete", R.string.ac_wallet_delete));
        binding.tvDeleteWallet.setOnClickListener(v -> {
            dismiss();
            listener.deleteWalletClick();
        });

        binding.tvCopyWallet.setText(LocaleController.getString("ac_wallet_copy", R.string.ac_wallet_copy));
        binding.tvCopyWallet.setOnClickListener(view -> {
            dismiss();
            listener.copyWalletAddress();
        });
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return AnimationHelper.asAnimation()
                .withScale(ScaleConfig.TOP_TO_BOTTOM.duration(200))
                .withAlpha(AlphaConfig.IN.duration(200))
                .toShow();
    }

    public interface DeleteWalletPopupListener {
        void deleteWalletClick();

        void copyWalletAddress();
    }
}