package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogMintEssenceNftBinding;

/**
 * Time:2023/4/18
 * Author:Perry
 * Description：mint nft成功弹窗
 */
public class MintEssenceNftSuccessfulDialog extends Dialog {

    private DialogMintEssenceNftBinding binding;

    public MintEssenceNftSuccessfulDialog(@NonNull Context context, Runnable runnable) {
        super(context, R.style.dialog2);
        binding = DialogMintEssenceNftBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvTitle.setText(LocaleController.getString("dialog_mint_essence_title", R.string.dialog_mint_essence_title));
        binding.tvContent.setText(LocaleController.getString("dialog_mint_essence_content", R.string.dialog_mint_essence_content));
        binding.tvBtn.setText(LocaleController.getString("Done", R.string.Done));
        binding.animationView.playAnimation();

        binding.tvBtn.setOnClickListener(view -> {
            dismiss();
            runnable.run();
        });

        Glide.with(getContext())
                .asGif()
                .load(R.drawable.alphagramx_essence_nft)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(binding.ivGif);

    }
}
