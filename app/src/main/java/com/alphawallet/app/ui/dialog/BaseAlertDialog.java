package teleblock.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import com.blankj.utilcode.util.KeyboardUtils;

import org.telegram.messenger.R;

public class BaseAlertDialog extends AlertDialog {

    public OnCloseListener onCloseListener;

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public interface OnCloseListener<T> {
        void onClose(T data);
    }

    public BaseAlertDialog(Context context) {
        super(context, R.style.CustomAlertDialog);
    }

    public BaseAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public BaseAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public BaseAlertDialog(Context context, OnCloseListener onCloseListener) {
        super(context);
        this.onCloseListener = onCloseListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    @Override
    public void dismiss() {
        KeyboardUtils.hideSoftInput(getWindow());
        super.dismiss();
    }
}
