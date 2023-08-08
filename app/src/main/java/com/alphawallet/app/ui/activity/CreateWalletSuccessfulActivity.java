package teleblock.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ClipboardUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityCreateWalletSuccessfulBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;

import teleblock.model.wallet.ETHWallet;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/12/9
 * Author:Perry
 * Description：创建钱包成功页面
 */
public class CreateWalletSuccessfulActivity extends BaseFragment {

    private ActivityCreateWalletSuccessfulBinding binding;
    private ETHWallet ethWallet;

    public CreateWalletSuccessfulActivity(ETHWallet ethWallet) {
        this.ethWallet = ethWallet;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivityCreateWalletSuccessfulBinding.inflate(LayoutInflater.from(context));
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_wallet_create_successful_title", R.string.dialog_wallet_create_successful_title));
        binding.tvImportanttipsTitle.setText(LocaleController.getString("dialog_wallet_create_successful_importanttips_title", R.string.dialog_wallet_create_successful_importanttips_title));
        binding.tvImportanttipsContent.setText(LocaleController.getString("dialog_wallet_create_successful_importanttips_content", R.string.dialog_wallet_create_successful_importanttips_content));
        binding.tvBackupmnemonics.setText(LocaleController.getString("dialog_wallet_create_successful_backupmnemonics", R.string.dialog_wallet_create_successful_backupmnemonics));
        binding.tvExportingPrivateKey.setText(LocaleController.getString("dialog_wallet_create_successful_exporting_private_key", R.string.dialog_wallet_create_successful_exporting_private_key));
        binding.tvBackupHome.setText(LocaleController.getString("dialog_wallet_create_successful_backup_home", R.string.dialog_wallet_create_successful_backup_home));

        binding.ivClose.setOnClickListener(v -> {
            finishFragment();
        });

        binding.tvBackupHome.setOnClickListener(v -> {
            finishFragment();
        });

        binding.tvWalletName.setText(ethWallet.getName());
        binding.tvWalletAddress.setText(ethWallet.getAddress());
        binding.tvCoinType.setText("0.00" + MMKVUtil.currentChainConfig().getMain_currency_name());

        //复制钱包地址
        binding.tvWalletAddress.setOnClickListener(v -> {
            ClipboardUtils.copyText(ethWallet.getAddress());
            BulletinFactory.of(this).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address)).show();
        });

        //备份助记词
        binding.tvBackupmnemonics.setOnClickListener(v -> {
            presentFragment(new WalletMnemonicActivity(ethWallet));
        });

        //导出私钥
        binding.tvExportingPrivateKey.setOnClickListener(v -> {
            presentFragment(new WalletExportActivity(ethWallet));
        });
    }
}