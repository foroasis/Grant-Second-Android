package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.blankj.utilcode.util.ThreadUtils;
import com.ruffian.library.widget.RTextView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.lang.ref.WeakReference;

public class TransferLoadDialog extends Dialog {

    private WeakReference<Context> mContext;
    private volatile static TransferLoadDialog sDialog;
    private TextView tvMessage;
    private RTextView tv_cancel_btn;

    private TransferLoadDialog(Context context, String message) {
        super(context, R.style.dialog2);

        mContext = new WeakReference<>(context);

        setContentView(R.layout.dialog_transfer_load);
        setCanceledOnTouchOutside(true);
        tvMessage = findViewById(R.id.tv_message);
        tvMessage.setText(message);

        tv_cancel_btn = (RTextView) findViewById(R.id.tv_cancel_btn);
        tv_cancel_btn.setText(LocaleController.getString("dialog_clean_tv_cancel", R.string.dialog_clean_tv_cancel));
        tv_cancel_btn.setOnClickListener(view -> dismiss());
    }

    public static synchronized void showLoading(Context context, String message) {
        showLoading(context, message, false);
    }

    public static synchronized void showLoading(Context context, String message, boolean cancelable) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sDialog != null && sDialog.isShowing()) {
                    sDialog.dismiss();
                }
                sDialog = new TransferLoadDialog(context, message);
                sDialog.setCancelable(cancelable);
                sDialog.setCanceledOnTouchOutside(cancelable);

                if (sDialog != null && !sDialog.isShowing()) {
                    sDialog.show();
                }
            }
        });
    }

    public static synchronized void stopLoading() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sDialog != null && sDialog.isShowing()) {
                    sDialog.dismiss();
                }
                sDialog = null;
            }
        });
    }

    public static synchronized boolean isLoading() {
        return sDialog != null && sDialog.isShowing();
    }

    public static synchronized void updateLoading(String message) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sDialog != null && sDialog.isShowing()) {
                    sDialog.setLoadMessage(message);
                }
            }
        });
    }

    private void setLoadMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            tvMessage.setText(message);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }
}