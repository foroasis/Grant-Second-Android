package com.alphawallet.app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogGroupPayConfirmBinding;

import teleblock.config.Constants;
import teleblock.manager.PayerGroupManager;
import teleblock.model.PrivateGroupEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.api.OrderPostApi;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：支付订单确认
 */
public class GroupPayConfirmDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupPayConfirmBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;
    private int payStatus;
    private int seconds = 60;
    private ThreadUtils.Task task;
    private String address;

    public GroupPayConfirmDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupPayConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("group_pay_confirm_title", R.string.group_pay_confirm_title));
        binding.tvPermitTitle.setText(LocaleController.getString("group_pay_confirm_permit_title", R.string.group_pay_confirm_permit_title));
        binding.tvStatusTitle.setText(LocaleController.getString("group_pay_confirm_permit_title", R.string.group_pay_confirm_status_title));

        binding.ivCloseDialog.setOnClickListener(this);
        binding.tvPayStart.setOnClickListener(this);
        binding.tvPayCancel.setOnClickListener(this);
    }

    private void initData() {
        WalletUtil.getWalletInfo(new WalletUtil.ObtionWalletInfoListener() {
            @Override
            public void walletInfo(ETHWallet wallet) {
                address = wallet.getAddress();
            }
        });
        GlideHelper.displayImage(getContext(), binding.ivGroupAvatar, privateGroup.getAvatar());
        binding.tvGroupName.setText(privateGroup.getTitle());
        GlideHelper.getDrawableGlide(getContext(), privateGroup.getCurrency_icon(), drawable -> {
            binding.tvPayAmount.getHelper().setIconNormalLeft(drawable);
        });

        binding.tvPayAmount.setText(privateGroup.getAmount());
        String format = LocaleController.getString("group_pay_confirm_pay_member_count", R.string.group_pay_confirm_pay_member_count);
        binding.tvPayMemberCount.setText(String.format(format, privateGroup.getShip()));
        binding.tvPayTime.setText(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("MM/dd HH:mm")));
        binding.tvFromAccount.setText(WalletUtil.formatAddress(address));
        binding.tvToAccount.setText(WalletUtil.formatAddress(privateGroup.getReceipt_account()));
        updatePayStatus(-1);
    }

    private void updatePayStatus(int payStatus) {
        this.payStatus = payStatus;
        if (payStatus == -1) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_off", R.string.group_pay_confirm_pay_off));
            binding.tvPayStart.setText(LocaleController.getString("group_pay_confirm_pay_confirm", R.string.group_pay_confirm_pay_confirm));
            binding.tvPaying.setText(LocaleController.getString("group_pay_confirm_paying", R.string.group_pay_confirm_paying));
            binding.tvPaySuccess.setText(LocaleController.getString("group_pay_confirm_pay_success", R.string.group_pay_confirm_pay_success));
            binding.tvPayCancel.setText(LocaleController.getString("group_pay_confirm_pay_cancel", R.string.group_pay_confirm_pay_cancel));
        } else if (payStatus == 0) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_off", R.string.group_pay_confirm_pay_off));
            binding.tvPayStatus.setTextColor(Color.parseColor("#FFD233"));
            binding.tvPayStart.setVisibility(View.GONE);
            binding.llPaying.setVisibility(View.VISIBLE);
        } else if (payStatus == 1) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_fail", R.string.group_pay_confirm_pay_fail));
            binding.tvPayStatus.setTextColor(Color.parseColor("#FF5F5F"));
            binding.tvPayStart.setVisibility(View.VISIBLE);
            binding.tvPayStart.setText(LocaleController.getString("group_pay_confirm_pay_continue", R.string.group_pay_confirm_pay_continue));
            binding.llPaying.setVisibility(View.GONE);
            binding.tvPayCancel.setText(LocaleController.getString("group_pay_confirm_pay_on", R.string.group_pay_confirm_pay_on));
        } else if (payStatus == 2) {
            binding.tvPayStatus.setText(LocaleController.getString("group_pay_confirm_pay_success", R.string.group_pay_confirm_pay_success));
            binding.tvPayStatus.setTextColor(Color.parseColor("#44D320"));
            binding.tvPaySuccess.setVisibility(View.VISIBLE);
            binding.llPaying.setVisibility(View.GONE);
            binding.tvPayCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close_dialog:
                dismiss();
                break;
            case R.id.tv_pay_start:
                sendTransaction();
                break;
            case R.id.tv_pay_cancel:
                if (payStatus == 1) {
                    Browser.openUrl(getContext(), Constants.getOfficialGroup(), true);
                }
                dismiss();
                break;
        }
    }

    private void sendTransaction() {
        updatePayStatus(0);
//        WalletUtil.requestGasFee(privateGroup.getChain_id(), privateGroup.getDecimal(), address, privateGroup.getReceipt_account(),
//                new BigDecimal(privateGroup.getAmount()), privateGroup.getContract_address(), new WalletUtil.RequestGasFeeListener() {
//                    @Override
//                    public void requestSuccessful(BigDecimal gasPrice, BigDecimal gasLimit) {
                        WalletUtil.sendTransaction(privateGroup.getChain_id(), privateGroup.getReceipt_account(),
                                null, null, privateGroup.getContract_address(),
                                privateGroup.getAmount(), privateGroup.getDecimal(), privateGroup.getCurrency_name(),
                                new WalletUtil.SendTransactionListener() {
                                    @Override
                                    public void paySuccessful(String hash) {
                                        orderPost(hash);
                                    }

                                    @Override
                                    public void payError(String error) {
                                        ToastUtils.showLong(error);
                                        updatePayStatus(1);
                                    }
                                });
//                    }
//                });
    }

    private void orderPost(String data) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new OrderPostApi()
                        .setGroup_id(privateGroup.getId())
                        .setTx_hash(data)
                        .setPayment_account(address)
                ).request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        PayerGroupManager.getInstance(fragment.getCurrentAccount()).addTxHash(data);
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
        updatePayStatus(2);
        new UplinkVerificationDialog(getContext()).show();
    }

    @Override
    public void show() {
        super.show();
        resetPeekHeight();
    }

    @Override
    public void dismiss() {
        if (task != null) {
            task.cancel();
        }
        super.dismiss();
    }
}
