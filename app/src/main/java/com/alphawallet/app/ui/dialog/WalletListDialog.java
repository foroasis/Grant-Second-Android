package teleblock.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
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
import teleblock.ui.activity.UniversalDialog;
import teleblock.ui.activity.WalletManageActivity;
import teleblock.ui.adapter.WalletRvAdapter;
import teleblock.ui.popup.DeleteWalletPopup;
import teleblock.util.ETHWalletUtils;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * 描述：钱包列表弹窗
 */
public class WalletListDialog extends BaseBottomSheetDialog {
    private BaseFragment baseFragment;
    private DialogWalletListBinding binding;
    private WalletRvAdapter walletRvAdapter;

    public WalletListDialog(BaseFragment baseFragment) {
        super(baseFragment.getContext());
        EventBus.getDefault().register(this);
        this.baseFragment = baseFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogWalletListBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        binding.flAvatarFrom.setUserInfo(baseFragment.getUserConfig().getCurrentUser()).loadView();
        String name = "";
        TLRPC.User user = baseFragment.getUserConfig().getCurrentUser();
        if (!TextUtils.isEmpty(user.first_name)) {
            name += user.first_name;
        }
        if (!TextUtils.isEmpty(user.last_name)) {
            name += user.last_name;
        }
        binding.tvName.setText(name);
        binding.tvTitle.setText(LocaleController.getString("dg_select_wallet", R.string.dg_select_wallet));
        binding.tvCrateBtn.setText(LocaleController.getString("dg_select_wallet_create", R.string.dg_select_wallet_create));

        binding.walletRv.setLayoutManager(new LinearLayoutManager(baseFragment.getContext()));
        binding.walletRv.setAdapter(walletRvAdapter = new WalletRvAdapter());
        walletRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            WalletHubEntity walletHub = walletRvAdapter.getItem(position);
            if (walletHub.itemType == 0) return;
            ETHWallet wallet = walletHub.wallet;
            //先切换item
            WalletDaoUtils.updateCurrent(wallet.getId());
            if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, wallet.getWalletType())) {
                WCSessionManager.getInstance().init(wallet);
            }
            onItemClick(WalletDaoUtils.getCurrent());
            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CHANGED));
            WalletUtil.walletBind(wallet.getAddress(), MMKVUtil.currentChainConfig().getId(), wallet.getWalletType(), null);
            dismiss();
        });
        walletRvAdapter.addChildClickViewIds(R.id.iv_edit, R.id.iv_copy, R.id.iv_opera);
        walletRvAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            WalletHubEntity walletHub = walletRvAdapter.getItem(position);
            if (walletHub.itemType == 0) return;
            ETHWallet wallet = walletHub.wallet;
            if (view.getId() == R.id.iv_edit) {
                baseFragment.presentFragment(new WalletManageActivity(wallet));
            } else if (view.getId() == R.id.iv_copy) {
                ClipboardUtils.copyText(wallet.getAddress());
                BulletinFactory.of(baseFragment).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address)).show();
            } else if (view.getId() == R.id.iv_opera) {
                new DeleteWalletPopup(this, new DeleteWalletPopup.DeleteWalletPopupListener() {
                    @Override
                    public void deleteWalletClick() {
                        String title = WalletUtil.formatAddress(wallet.getAddress()) + "\n" + LocaleController.getString("dialog_delete_wallet_tips", R.string.dialog_delete_wallet_tips);
                        new UniversalDialog(getContext(), () -> {
                            deleteWallet(wallet, position);
                        }).setTitle(title).show();
                    }

                    @Override
                    public void copyWalletAddress() {
                        ClipboardUtils.copyText(wallet.getAddress());
                        ToastUtils.showLong(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address));
                    }
                }).showPopupWindow(view);
            }
        });

        binding.tvCrateBtn.setOnClickListener(v -> {
            dismiss();
            new WalletAddDialog(baseFragment).show();
        });
        binding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void initData() {
        List<ETHWallet> list = WalletDaoUtils.loadAll();
        if (list == null || list.size() == 0) return;

        List<Integer> hubType = new ArrayList<>();
        List<WalletHubEntity> hubList = new ArrayList<>();
        for (ETHWallet wallet : list) {
//            int type = wallet.getWalletType();
//            int tempType = type;
//            //链接的钱包有个区间段
//            if (Arrays.asList(ETHWallet.TYPE_CONNECT).contains(type)) {
//                tempType = 1;
//            }
//            //分组标题
//            if (!hubType.contains(tempType)) {
//                hubType.add(tempType);
//                hubList.add(getHubTitle(tempType));
//            }
            hubList.add(new WalletHubEntity(wallet));
        }

        walletRvAdapter.setList(hubList);
    }

    //item点击
    public void onItemClick(ETHWallet wallet) {
    }

    private WalletHubEntity getHubTitle(int type) {
        String title = "";
        if (ETHWallet.TYPE_DEFAULT == type) {
            title = LocaleController.getString("dg_select_wallet_identity_text", R.string.dg_select_wallet_identity_text);
        } else if (ETHWallet.TYPE_CREATE_OR_IMPORT == type) {
            title = LocaleController.getString("dg_select_wallet_create_text", R.string.dg_select_wallet_create_text);
        } else if (1 == type) {
            title = LocaleController.getString("dg_select_wallet_connect_text", R.string.dg_select_wallet_connect_text);
        } else if (ETHWallet.TYPE_TSS == type) {
            title = LocaleController.getString("dg_select_wallet_tss_text", R.string.dg_select_wallet_tss_text);
        }
        return new WalletHubEntity(title);
    }

    private void deleteWallet(ETHWallet wallet, int position) {
        //删除的是不是当前钱包
        boolean ifDeleteCurrent = wallet.getId() == WalletDaoUtils.getCurrent().getId();

        ETHWalletUtils.deleteWallet(wallet);
        String s = String.format(LocaleController.getString("ac_wallet_delete_ok_text", R.string.ac_wallet_delete_ok_text), wallet.getName());
        ToastUtils.showShort(s);
        walletRvAdapter.removeAt(position);

        if (ifDeleteCurrent) {
            if (WalletDaoUtils.loadAll().isEmpty()) {
                EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CHANGED));
                dismiss();
                return;
            }
            //更新选中钱包
            WalletDaoUtils.updateCurrent(WalletDaoUtils.loadAll().get(0).getId());
            EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CHANGED));
            dismiss();
        }
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.EDIT_WALLET_NAME_SUCCESSFUL:
                initData();
                break;
        }
    }
}