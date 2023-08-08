package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityEditWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.wallet.ETHWallet;
import teleblock.util.ToastUtil;
import teleblock.util.WalletDaoUtils;

/**
 * Description：修改钱包名称
 */
public class WalletNameEditActivity extends BaseFragment {
    private ActivityEditWalletBinding binding;
    private ETHWallet wallet;

    public WalletNameEditActivity(ETHWallet wallet) {
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
        actionBar.setTitle(LocaleController.getString("ac_wallet_edit_title", R.string.ac_wallet_edit_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityEditWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTips.setText(LocaleController.getString("ac_wallet_edit_input", R.string.ac_wallet_edit_input));
        binding.etInput.setHint(wallet.getName());
        binding.ivClose.setOnClickListener(v -> binding.etInput.setText(""));
        binding.etInput.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String text = binding.etInput.getText().toString();
                if (TextUtils.isEmpty(text)) return false;
                boolean b = WalletDaoUtils.updateWalletName(wallet.getId(), text);
                if (b) {
                    ToastUtils.showShort(LocaleController.getString("ac_wallet_edit_ok", R.string.ac_wallet_edit_ok));
                    EventBus.getDefault().post(new MessageEvent(EventBusTags.EDIT_WALLET_NAME_SUCCESSFUL));
                    finishFragment();
                }
                return true;
            }
            return false;
        });
    }
}
