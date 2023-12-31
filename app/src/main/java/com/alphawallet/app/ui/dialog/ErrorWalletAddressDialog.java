package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCommonTipsBinding;
import org.telegram.messenger.databinding.DialogErrorAddressBinding;

import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

public class ErrorWalletAddressDialog extends Dialog {
    public static final int TYPE_ERROR_BIND_ADDRESS = 2;//不是已绑定的钱包地址
    private final String address;
    private DialogErrorAddressBinding binding;
    private int type;

    public ErrorWalletAddressDialog(@NonNull Context context, int type, String address) {
        super(context, R.style.dialog2);
        this.type = type;
        this.address = address;
        binding = DialogErrorAddressBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.tvTitle.getHelper().setTextColorNormal(Color.BLACK);
        String text = address.toLowerCase().startsWith("0x") ? "Ethereum" : "Solana";
        binding.tvTitle.setText(String.format(LocaleController.getString("ac_wallet_tips", R.string.ac_wallet_tips), text));
        binding.tvContent.setText(String.format(LocaleController.getString("ac_wallet_content", R.string.ac_wallet_content), text));
        binding.tvConfirm.setText(LocaleController.getString("ac_wallet_btn", R.string.ac_wallet_btn));
        if (type == TYPE_ERROR_BIND_ADDRESS) {
            binding.tvTitle.setText(LocaleController.getString("chat_transfer_error_bind_address_title", R.string.chat_transfer_error_bind_address_title));
            String content = WalletUtil.formatAddress(address) + "\n" + (LocaleController.getString("chat_transfer_error_bind_address_tips", R.string.chat_transfer_error_bind_address_tips));
            binding.tvContent.setText(content);
            binding.tvConfirm.setText(LocaleController.getString("chat_transfer_error_bind_address_btn", R.string.chat_transfer_error_bind_address_btn));
        }

        binding.tvTitle.setOnClickListener(view -> dismiss());
        binding.tvConfirm.setOnClickListener(view -> onConfirm());
    }

    public void onConfirm() {
        dismiss();
    }
}
