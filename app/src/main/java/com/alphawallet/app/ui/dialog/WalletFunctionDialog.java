package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogWalletFunctionBinding;


/**
 * Description：钱包功能弹窗
 */
public class WalletFunctionDialog extends BaseBottomSheetDialog {
    private DialogWalletFunctionBinding binding;
    private boolean singleChat;

    public WalletFunctionDialog(@NonNull Context context, boolean singleChat) {
        super(context);
        this.singleChat = singleChat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogWalletFunctionBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("chat_wallet_function_title", R.string.chat_wallet_function_title));
        binding.tvDesc.setText(LocaleController.getString("chat_wallet_function_desc", R.string.chat_wallet_function_desc));
        binding.tvItemTrans.setText(LocaleController.getString("chat_wallet_function_item_trans", R.string.chat_wallet_function_item_trans));
        binding.tvItemRedpack.setText(LocaleController.getString("chat_wallet_function_item_redpack", R.string.chat_wallet_function_item_redpack));
        binding.tvItemNft.setText(LocaleController.getString("chat_wallet_function_item_nft", R.string.chat_wallet_function_item_nft));
        binding.tvItemHash.setText(LocaleController.getString("chat_wallet_function_item_hash", R.string.chat_wallet_function_item_hash));

        if (singleChat) {
            binding.line1.setVisibility(View.VISIBLE);
            binding.layoutTrans.setVisibility(View.VISIBLE);
        } else {
            binding.line1.setVisibility(View.GONE);
            binding.layoutTrans.setVisibility(View.GONE);
        }

        binding.layoutTrans.setOnClickListener(view -> {
            dismiss();
            onItemClick(0);
        });
        binding.layoutRedpack.setOnClickListener(view -> {
            dismiss();
            onItemClick(1);
        });
        binding.layoutNft.setOnClickListener(view -> {
            dismiss();
            onItemClick(2);
        });
        binding.layoutHash.setOnClickListener(view -> {
            dismiss();
            onItemClick(3);
        });
    }

    public void onItemClick(int position) {

    }
}
