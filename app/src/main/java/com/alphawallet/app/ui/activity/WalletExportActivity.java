package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ClipboardUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityEditWalletBinding;
import org.telegram.messenger.databinding.ActivityExportWalletBinding;
import org.telegram.messenger.databinding.ActivityImportWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.CameraScanActivity;
import org.telegram.ui.Components.BulletinFactory;

import java.util.Arrays;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.CameraScanEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.dialog.WalletImportFailDialog;
import teleblock.ui.dialog.WalletImportLoadingDialog;
import teleblock.ui.dialog.WalletImportSuccessDialog;
import teleblock.util.ETHWalletUtils;
import teleblock.util.WalletDaoUtils;

/**
 * Description：导出钱包
 */
public class WalletExportActivity extends BaseFragment {
    private ActivityExportWalletBinding binding;
    private ETHWallet wallet;

    public WalletExportActivity(ETHWallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_wallet_export_title", R.string.ac_wallet_export_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityExportWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvSub1.setText(LocaleController.getString("ac_wallet_export_sub1", R.string.ac_wallet_export_sub1));
        binding.tvDesc1.setHint(LocaleController.getString("ac_wallet_export_desc1", R.string.ac_wallet_export_desc1));
        binding.tvSub2.setText(LocaleController.getString("ac_wallet_export_sub2", R.string.ac_wallet_export_sub2));
        binding.tvDesc2.setText(LocaleController.getString("ac_wallet_export_desc2", R.string.ac_wallet_export_desc2));
        binding.tvSub3.setText(LocaleController.getString("ac_wallet_export_sub3", R.string.ac_wallet_export_sub3));
        binding.tvDesc3.setText(LocaleController.getString("ac_wallet_export_desc3", R.string.ac_wallet_export_desc3));
        binding.tvBtnCopy.setText(LocaleController.getString("ac_wallet_export_btn_copy", R.string.ac_wallet_export_btn_copy));

        binding.tvKey.setText(ETHWalletUtils.derivePrivateKey(wallet.getId(), ""));

        binding.tvBtnCopy.setOnClickListener(v -> {
            ClipboardUtils.copyText(binding.tvKey.getText().toString());
            BulletinFactory.of(this).createCopyBulletin(LocaleController.getString("ac_wallet_export_btn_copy_ok", R.string.ac_wallet_export_btn_copy_ok)).show();
        });
    }
}
