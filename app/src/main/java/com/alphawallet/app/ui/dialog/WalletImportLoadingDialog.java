package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.blankj.utilcode.util.ThreadUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.lang.ref.WeakReference;

public class WalletImportLoadingDialog extends Dialog {

    public WalletImportLoadingDialog(Context context) {
        super(context, R.style.dialog2);
        setContentView(R.layout.dialog_wallet_import);
        ((TextView) findViewById(R.id.tv_message)).setText(LocaleController.getString("ac_wallet_import_loading", R.string.ac_wallet_import_loading));
        ((TextView) findViewById(R.id.tv_tips)).setText(LocaleController.getString("ac_wallet_import_loading_text", R.string.ac_wallet_import_loading_text));
    }
}