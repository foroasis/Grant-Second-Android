package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogUplinkVerificationBinding;
import org.telegram.messenger.databinding.DialogWalletLoginBinding;

/**
 * 钱包登录
 */
public class WalletLoginDialog extends BaseAlertDialog {

    private DialogWalletLoginBinding binding;
    private static WalletLoginDialog sDialog;

    public WalletLoginDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogWalletLoginBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = ScreenUtils.getScreenWidth();
        getWindow().getAttributes().height = ScreenUtils.getScreenHeight();
        initView();
    }

    private void initView() {
//        binding.tvTitle.setText(LocaleController.getString("uplink_verification_title", R.string.uplink_verification_title));
    }

    public static synchronized void showLoading(Context context) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sDialog != null && sDialog.isShowing()) {
                    sDialog.dismiss();
                }
                sDialog = new WalletLoginDialog(context);
                sDialog.show();
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
}