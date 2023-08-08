package teleblock.ui.dialog;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogUserWalletListBinding;
import org.telegram.messenger.databinding.DialogWalletListBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.WCSessionManager;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.WalletHubEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.model.wallet.WalletInfo;
import teleblock.ui.activity.WalletManageActivity;
import teleblock.ui.adapter.WalletRvAdapter;
import teleblock.ui.adapter.WalletUserRvAdapter;
import teleblock.ui.popup.DeleteWalletPopup;
import teleblock.util.ETHWalletUtils;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * 描述：用户钱包列表弹窗
 */
public class UserWalletListDialog extends BaseBottomSheetDialog {
    private BaseFragment baseFragment;
    private DialogUserWalletListBinding binding;
    private WalletUserRvAdapter walletRvAdapter;
    private long tgUserId;
    private List<WalletInfo.WalletInfoItem> walletInfoItems;

    private WalletInfo.WalletInfoItem tempSelect;

    public UserWalletListDialog(BaseFragment baseFragment, long tgUserId, List<WalletInfo.WalletInfoItem> walletInfoItems) {
        super(baseFragment.getContext());
        this.baseFragment = baseFragment;
        this.tgUserId = tgUserId;
        this.walletInfoItems = walletInfoItems;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogUserWalletListBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        String name = "";
        TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController().getUser(Math.abs(tgUserId));
        if (user != null) {
            if (!TextUtils.isEmpty(user.first_name)) {
                name += user.first_name;
            }
            if (!TextUtils.isEmpty(user.last_name)) {
                name += user.last_name;
            }
        }
        String format = String.format(LocaleController.getString("user_wallet_title", R.string.user_wallet_title), name);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(format);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), format.indexOf(name), format.indexOf(name) + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvTitle.setText(spannableStringBuilder);
        binding.tvConfirmBtn.setText(LocaleController.getString("dg_select_user_wallet", R.string.dg_select_user_wallet));

        binding.walletRv.setLayoutManager(new LinearLayoutManager(baseFragment.getContext()));
        binding.walletRv.setAdapter(walletRvAdapter = new WalletUserRvAdapter());
        walletRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            binding.tvConfirmBtn.setEnabled(true);
            binding.tvConfirmBtn.setAlpha(1.0f);
            walletRvAdapter.changeSelect(position);
            tempSelect = walletRvAdapter.getItem(position);
        });

        binding.tvConfirmBtn.setText(LocaleController.getString("dg_select_user_wallet", R.string.dg_select_user_wallet));
        binding.tvConfirmBtn.setEnabled(false);
        binding.tvConfirmBtn.setAlpha(0.4f);
        binding.tvConfirmBtn.setOnClickListener(v -> {
            if (tempSelect == null) {
                return;
            }
            onWalletSelect(tempSelect);
            dismiss();
        });
    }

    private void initData() {
        for (WalletInfo.WalletInfoItem item : walletInfoItems) {
            item.setSelect(false);
        }
        walletRvAdapter.setList(walletInfoItems);
    }

    public void onWalletSelect(WalletInfo.WalletInfoItem walletInfoItem) {
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }
}