package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityCreateWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.model.wallet.ETHWallet;
import teleblock.util.ETHWalletUtils;
import teleblock.util.WalletDaoUtils;

/**
 * Time:2022/12/9
 * Author:Perry
 * Description：创建钱包页面
 */
public class CreateWalletActivity extends BaseFragment {

    private ActivityCreateWalletBinding binding;

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_wallet_create_title", R.string.ac_wallet_create_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityCreateWalletBinding.inflate(LayoutInflater.from(context));

        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvConfirmCreate.setText(LocaleController.getString("ac_wallet_create_btn_title", R.string.ac_wallet_create_btn_title));
        binding.etInputName.setHint(ETHWalletUtils.generateNewWalletName());
        binding.tvInputTips.setText(LocaleController.getString("ac_wallet_create_input_tips", R.string.ac_wallet_create_input_tips));
        binding.tvInputTips.setTextColor(Color.parseColor("#56565c"));

        binding.etInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String walletName = binding.etInputName.getText().toString().trim();
                if (walletName.isEmpty()) {
                    binding.tvInputTips.setText(LocaleController.getString("ac_wallet_create_input_tips", R.string.ac_wallet_create_input_tips));
                    binding.tvInputTips.setTextColor(Color.parseColor("#56565c"));
                    binding.tvConfirmCreate.setEnabled(false);
                    binding.tvConfirmCreate.setAlpha(0.5f);
                } else {
                    if (walletName.length() > 10 ) {
                        binding.tvInputTips.setText(LocaleController.getString("ac_wallet_create_input_error", R.string.ac_wallet_create_input_error));
                        binding.tvInputTips.setTextColor(Color.parseColor("#FF4550"));
                        binding.tvConfirmCreate.setEnabled(false);
                        binding.tvConfirmCreate.setAlpha(0.5f);
                    } else {
                        binding.tvInputTips.setText("");
                        binding.tvConfirmCreate.setEnabled(true);
                        binding.tvConfirmCreate.setAlpha(1f);
                    }
                }
            }
        });

        //确认创建
        binding.tvConfirmCreate.setEnabled(false);
        binding.tvConfirmCreate.setOnClickListener(v -> {
            //钱包名称
            String walletName = binding.etInputName.getText().toString().trim();
            //判断名称是否存在
            if (WalletDaoUtils.walletNameChecking(walletName)) {
                ToastUtils.showLong(LocaleController.getString("ac_wallet_create_name_repeat", R.string.ac_wallet_create_name_repeat));
                return;
            }

            //创建钱包获取返回值
            ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<ETHWallet>() {
                @Override
                public ETHWallet doInBackground() {
                    return ETHWalletUtils.generateMnemonic(walletName, "");
                }

                @Override
                public void onSuccess(ETHWallet result) {
                    result.setWalletType(ETHWallet.TYPE_CREATE_OR_IMPORT);
                    WalletDaoUtils.insert(result);
                    presentFragment(new CreateWalletSuccessfulActivity(result), true);
                }
            });
        });
    }
}
