package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityManageWalletBinding;
import org.telegram.messenger.databinding.ActivityMnemonicWalletBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.Arrays;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.adapter.MnemonicRvAdapter;
import teleblock.util.WalletDaoUtils;

/**
 * Description：管理助记词
 */
public class WalletMnemonicActivity extends BaseFragment {
    private ActivityMnemonicWalletBinding binding;
    private ETHWallet wallet;

    private MnemonicRvAdapter adapter;

    public WalletMnemonicActivity(ETHWallet wallet) {
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
        //getParentActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ac_wallet_mnemonic_title", R.string.ac_wallet_mnemonic_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityMnemonicWalletBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvWalletMnemonicTipText.setText(LocaleController.getString("ac_wallet_mnemonic_tip_text", R.string.ac_wallet_mnemonic_tip_text));
        binding.tvWalletMnemonicTipDesc.setText(LocaleController.getString("ac_wallet_mnemonic_tip_desc", R.string.ac_wallet_mnemonic_tip_desc));
        binding.tvWalletMnemonicWarningText.setText(LocaleController.getString("ac_wallet_mnemonic_warning_text", R.string.ac_wallet_mnemonic_warning_text));
        binding.tvWalletMnemonicWarningDesc.setText(LocaleController.getString("ac_wallet_mnemonic_warning_desc", R.string.ac_wallet_mnemonic_warning_desc));
        binding.tvWalletMnemonicKeepSafeText.setText(LocaleController.getString("ac_wallet_mnemonic_keep_safe_text", R.string.ac_wallet_mnemonic_keep_safe_text));
        binding.tvWalletMnemonicKeepSafeDesc.setText(LocaleController.getString("ac_wallet_mnemonic_keep_safe_desc", R.string.ac_wallet_mnemonic_keep_safe_desc));
        binding.tvWalletMnemonicBackupText.setText(LocaleController.getString("ac_wallet_mnemonic_backup_text", R.string.ac_wallet_mnemonic_backup_text));
        binding.tvWalletMnemonicBackupTipsText.setText(LocaleController.getString("ac_wallet_mnemonic_backup_tips_text", R.string.ac_wallet_mnemonic_backup_tips_text));
        binding.tvWalletMnemonicBackupWarningText.setText(LocaleController.getString("ac_wallet_mnemonic_backup_warning_text", R.string.ac_wallet_mnemonic_backup_warning_text));
        binding.tvStartBackup.setText(LocaleController.getString("ac_wallet_mnemonic_start_backup_text", R.string.ac_wallet_mnemonic_start_backup_text));
        binding.tvBackupOk.setText(LocaleController.getString("ac_wallet_mnemonic_backup_ok_text", R.string.ac_wallet_mnemonic_backup_ok_text));

        // Rv
        binding.mnemonicRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.mnemonicRv.setAdapter(adapter = new MnemonicRvAdapter());
        binding.mnemonicRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = 2;
                outRect.bottom = 2;
            }
        });

        //click
        binding.tvStartBackup.setOnClickListener(v -> {
            binding.llTipsContent.setVisibility(View.GONE);
            binding.llMnemonicContent.setVisibility(View.VISIBLE);
        });
        binding.tvBackupOk.setOnClickListener(v -> finishFragment());

    }

    private void initData() {
        String[] words = wallet.getMnemonic().split(" ");
        adapter.setList(Arrays.asList(words));
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
