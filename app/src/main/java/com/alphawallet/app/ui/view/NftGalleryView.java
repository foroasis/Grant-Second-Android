package teleblock.ui.view;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.BaseFragment;

/**
 * 创建日期：2023/5/26
 * 描述：
 */
public class NftGalleryView extends FrameLayout {

    private BaseFragment fragment;

    public NftGalleryView(BaseFragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }
}