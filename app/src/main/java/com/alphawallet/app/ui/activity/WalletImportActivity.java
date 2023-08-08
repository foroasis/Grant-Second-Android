package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityImportWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.CameraScanActivity;

import java.util.Arrays;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.CameraScanEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.dialog.WalletImportFailDialog;
import teleblock.ui.dialog.WalletImportLoadingDialog;
import teleblock.ui.dialog.WalletImportSuccessDialog;
import teleblock.util.ETHWalletUtils;
import teleblock.util.StringUtil;
import teleblock.util.WalletDaoUtils;

/**
 * Description：导入钱包
 */
public class WalletImportActivity extends BaseFragment {
    private ActivityImportWalletBinding binding;

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_wallet_import_title", R.string.ac_wallet_import_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityImportWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTips1.setText(LocaleController.getString("ac_wallet_import_tip1", R.string.ac_wallet_import_tip1));
        binding.etInput.setHint(LocaleController.getString("ac_wallet_import_hint", R.string.ac_wallet_import_hint));
        binding.tvTips2.setText(LocaleController.getString("ac_wallet_import_tip2", R.string.ac_wallet_import_tip2));
        binding.tvBtn.setText(LocaleController.getString("ac_wallet_import_btn", R.string.ac_wallet_import_btn));
        binding.tvErrorWords.setText(LocaleController.getString("ac_wallet_import_tip3", R.string.ac_wallet_import_tip3));
        binding.tvBtn.setEnabled(false);
        binding.tvBtn.setAlpha(0.4f);
        binding.etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tvBtn.setEnabled(false);
                binding.tvBtn.setAlpha(0.4f);
                binding.tvErrorWords.setVisibility(View.GONE);
                String text = s.toString();
                if (TextUtils.isEmpty(text)) return;
                if (text.contains(" ")) {//助记词
                    String[] textArr = text.split(" ");
                    if (textArr.length < 12) {//助记词不正确
                        binding.tvErrorWords.setVisibility(View.VISIBLE);
                        return;
                    }
                }
                binding.tvBtn.setAlpha(1f);
                binding.tvBtn.setEnabled(true);
            }
        });
        binding.ivScan.setOnClickListener(v -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.SHOW_CAMERA_FUNCTION, CameraScanActivity.WALLET_IMPORT_SCAN));
        });
        binding.tvBtn.setOnClickListener(v -> {
            String text = binding.etInput.getText().toString();
            if (!StringUtil.isPrivateKey(text)) {
                ToastUtils.showLong(LocaleController.getString("ac_wallet_export_not_privatekey", R.string.ac_wallet_export_not_privatekey));
                return;
            }

            WalletImportLoadingDialog dialog = new WalletImportLoadingDialog(getContext());
            dialog.show();

            String[] mnemonic = null;
            if (text.contains(" ")) {
                mnemonic = text.split(" ");
            }

            String[] fMnemonic = mnemonic;
            String fPrivateKey = text;
            new Thread(() -> {
                ETHWallet ethWallet;
                if (fMnemonic != null) {
                    ethWallet = ETHWalletUtils.importMnemonic(Arrays.asList(fMnemonic), "");
                } else {
                    ethWallet = ETHWalletUtils.loadWalletByPrivateKey(fPrivateKey, "");
                }
                AndroidUtilities.runOnUIThread(() -> {
                    dialog.dismiss();
                    if (ethWallet != null) {
                        ethWallet.setWalletType(ETHWallet.TYPE_CREATE_OR_IMPORT);
                        WalletDaoUtils.insert(ethWallet);
                        new WalletImportSuccessDialog(getContext()) {
                            @Override
                            public void onClick() {
                                finishFragment();
                            }
                        }.show();
                    } else {
                        new WalletImportFailDialog(getContext()).show();
                    }
                });
            }).start();
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.CAMERA_SCAN_RESULT:
                CameraScanEntity scanEntity = (CameraScanEntity) messageEvent.getData();
                if (scanEntity.type == CameraScanActivity.WALLET_IMPORT_SCAN) {
                    String text = scanEntity.data;
                    String fText;
                    //判断是不是小狐狸钱包的二维码，如果是则按照这个进行截取
                    if (!text.isEmpty() && text.length() >= 52 && text.contains("0x") && text.toLowerCase().startsWith("ethereum:")) {
                        fText = text.substring(text.indexOf(":") + 1, text.indexOf(":") + 43);
                    } else {
                        fText = text;
                    }
                    binding.etInput.setText(fText);
                }
                break;
        }
    }
}
