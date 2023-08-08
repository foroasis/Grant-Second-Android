package teleblock.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.particle.base.ParticleNetwork;
import com.particle.base.data.WebOutput;
import com.particle.base.data.WebServiceCallback;
import com.particle.base.data.WebServiceError;
import com.particle.network.ParticleNetworkAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityEditWalletBinding;
import org.telegram.messenger.databinding.ActivityManageWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.CameraScanActivity;

import teleblock.blockchain.WCSessionManager;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.CameraScanEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.util.ETHWalletUtils;
import teleblock.util.WalletDaoUtils;

/**
 * Description：管理钱包
 */
public class WalletManageActivity extends BaseFragment {
    private ActivityManageWalletBinding binding;
    private ETHWallet wallet;

    public WalletManageActivity(ETHWallet wallet) {
        this.wallet = wallet;
    }

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
        actionBar.setTitle(wallet.getName());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityManageWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvManageOption.setText(LocaleController.getString("ac_wallet_manage_option", R.string.ac_wallet_manage_option));
        binding.tvManageEditName.setText(LocaleController.getString("ac_wallet_manage_edit_name", R.string.ac_wallet_manage_edit_name));
        binding.tvManageKey.setText(LocaleController.getString("ac_wallet_manage_key", R.string.ac_wallet_manage_key));
        binding.tvManageMnemonic.setText(LocaleController.getString("ac_wallet_manage_mnemonic", R.string.ac_wallet_manage_mnemonic));

        if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, wallet.getWalletType())) {//链接的钱包
            binding.tvDeleteWallet.setText(LocaleController.getString("ac_wallet_cancel_bind", R.string.ac_wallet_cancel_bind));
        } else {
            binding.tvDeleteWallet.setText(LocaleController.getString("ac_wallet_delete", R.string.ac_wallet_delete));
        }

        //修改名称
        binding.llEditName.setOnClickListener(v -> presentFragment(new WalletNameEditActivity(wallet)));
        //导出私钥
        binding.llShowKey.setOnClickListener(v -> {
            if (wallet.getWalletType() == ETHWallet.TYPE_TSS) return;
            if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, wallet.getWalletType())) {
                ToastUtils.showShort(LocaleController.getString("ac_wallet_key_error_tips", R.string.ac_wallet_key_error_tips));
                return;
            }
            presentFragment(new WalletExportActivity(wallet));
        });
        //查看助记词
        binding.llShowMnemonic.setOnClickListener(v -> {
            if (wallet.getWalletType() == ETHWallet.TYPE_TSS) return;
            if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, wallet.getWalletType())) {
                ToastUtils.showShort(LocaleController.getString("ac_wallet_mnemonic_error_tips", R.string.ac_wallet_mnemonic_error_tips));
                return;
            }

            if (StringUtils.isEmpty(wallet.getMnemonic())) {
                ToastUtils.showShort(LocaleController.getString("ac_wallet_mnemonic_error_tips1", R.string.ac_wallet_mnemonic_error_tips1));
                return;
            }
            presentFragment(new WalletMnemonicActivity(wallet));
        });
        //删除钱包
        binding.tvDeleteWallet.setOnClickListener(v -> {
            if (wallet.getId() == WalletDaoUtils.getCurrent().getId()) {
                ToastUtils.showShort(LocaleController.getString("ac_wallet_delete_error_text", R.string.ac_wallet_delete_error_text));
                return;
            }
            String delStr = String.format(LocaleController.getString("ac_wallet_delete_tips", R.string.ac_wallet_delete_tips), wallet.getName());
            AlertDialog alertDialog = new AlertDialog.Builder(getContext(), getResourceProvider())
                    .setTitle(LocaleController.getString("ac_wallet_delete", R.string.ac_wallet_delete))
                    .setMessage(delStr)
                    .setPositiveButton(LocaleController.getString("ac_wallet_delete_confirm", R.string.ac_wallet_delete_confirm), (dialog, which) -> {
                        if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, wallet.getWalletType())) {
                            WCSessionManager.getInstance().disConnect(false);
                        } else if (wallet.getWalletType() == ETHWallet.TYPE_TSS && ParticleNetworkAuth.isLogin(ParticleNetwork.INSTANCE)) {
                            ParticleNetworkAuth.logout(ParticleNetwork.INSTANCE, new WebServiceCallback<WebOutput>() {
                                @Override
                                public void success(@NonNull WebOutput webOutput) {
                                    deleteWallet();
                                }

                                @Override
                                public void failure(@NonNull WebServiceError webServiceError) {

                                }
                            });
                            return;
                        }
                        deleteWallet();
                    })
                    .setNegativeButton(LocaleController.getString("ac_wallet_delete_cancel", R.string.ac_wallet_delete_cancel), null)
                    .show();
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            }
        });
    }

    private void deleteWallet() {
        ETHWalletUtils.deleteWallet(wallet);
        String s = String.format(LocaleController.getString("ac_wallet_delete_ok_text", R.string.ac_wallet_delete_ok_text), wallet.getName());
        ToastUtils.showShort(s);
        EventBus.getDefault().post(new MessageEvent(EventBusTags.DELETE_WALLET_SUCCESSFUL));
        finishFragment();
    }

    private void initData() {
        binding.tvManageNameShow.setText(wallet.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.EDIT_WALLET_NAME_SUCCESSFUL:
                wallet = WalletDaoUtils.getCurrent();
                initData();
                break;
        }
    }
}
