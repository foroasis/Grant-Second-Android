package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public class WalletImportFailDialog extends Dialog {

    public WalletImportFailDialog(Context context) {
        super(context, R.style.dialog2);
        setContentView(R.layout.dialog_wallet_import_fail);
        ((TextView) findViewById(R.id.tv_title)).setText(LocaleController.getString("ac_wallet_import_fail", R.string.ac_wallet_import_fail));
        ((TextView) findViewById(R.id.tv_message)).setText(LocaleController.getString("ac_wallet_import_fail_tips", R.string.ac_wallet_import_fail_tips));
        ((TextView) findViewById(R.id.tv_ok)).setText(LocaleController.getString("ac_wallet_import_fail_btn", R.string.ac_wallet_import_fail_btn));

        findViewById(R.id.tv_ok).setOnClickListener(v -> {
            dismiss();
        });
    }
}