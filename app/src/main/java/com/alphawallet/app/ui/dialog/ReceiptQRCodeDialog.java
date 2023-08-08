package teleblock.ui.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ClipboardUtils;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogReceiptQrcodeBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.DialogsActivity;

import java.util.HashMap;

import teleblock.blockchain.BlockchainConfig;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/12/1
 * Author:Perry
 * Description：收款二维码
 */
public class ReceiptQRCodeDialog extends BaseBottomSheetDialog {

    private BaseFragment dialogsActivity;
    //地址
    private String address;
    private DialogReceiptQrcodeBinding binding;

    private Bitmap qrCode;

    public ReceiptQRCodeDialog(@NonNull BaseFragment dialogsActivity, String address) {
        super(dialogsActivity.getParentActivity());
        this.dialogsActivity = dialogsActivity;
        this.address = address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogReceiptQrcodeBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("view_mywallet_tab_collection", R.string.view_mywallet_tab_collection));
        binding.tvWalletAddressTitle.setText(LocaleController.getString("dialog_receipt_qrcode_wallet_address_title", R.string.dialog_receipt_qrcode_wallet_address_title));
        binding.rtvCopy.setText(LocaleController.getString("LinkActionCopy", R.string.LinkActionCopy));
        binding.rtvShare.setText(LocaleController.getString("LinkActionShare", R.string.LinkActionShare));

        binding.ivClose.setOnClickListener(v -> {
            dismiss();
        });

        //钱包地址拼接
        String key = "ethereum:" + address + "@" + MMKVUtil.currentChainConfig().getId();
        //显示二维码
        binding.ivQrcode.setImageBitmap(qrCode = createQR(key, qrCode));
        //显示钱包地址
        binding.tvWalletAddress.setText(address);

        //复制
        binding.rtvCopy.setOnClickListener(v -> {
            ClipboardUtils.copyText(address);
            BulletinFactory.of(binding.getRoot(), dialogsActivity.getResourceProvider()).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address)).show();
        });

        //分享
        binding.rtvShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, address);
            dialogsActivity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink)), 500);
        });
    }

    public Bitmap createQR(String key, Bitmap oldBitmap) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            QRCodeWriter writer = new QRCodeWriter();
            writer.setIfShowIocn(false);
            Bitmap bitmap = writer.encode(key, 768, 768, hints, oldBitmap);
            return bitmap;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }
}
