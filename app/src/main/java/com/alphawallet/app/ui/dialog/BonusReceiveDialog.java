package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogBonusReceiveBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ChatActivity;

import java.io.Serializable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import teleblock.database.KKVideoMessageDB;
import teleblock.model.BonusGetEntity;
import teleblock.model.BonusStatusEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.BonusGetApi;
import teleblock.network.api.BonusStatusApi;
import teleblock.ui.activity.BonusDetailActivity;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * 红包领取弹框
 */
public class BonusReceiveDialog extends BaseBottomSheetDialog {

    private volatile static BonusReceiveDialog dialog;
    private DialogBonusReceiveBinding binding;
    private ChatActivity fragment;
    private BonusStatusEntity entity;
    private BonusReceiveDialogListener listener;
    private Timer timer;
    private TimerTask task;

    public BonusReceiveDialog(ChatActivity fragment, BonusStatusEntity entity, BonusReceiveDialogListener listener) {
        super(fragment.getContext());
        this.fragment = fragment;
        this.entity = entity;
        this.listener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogBonusReceiveBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
        initData();
        startTicker();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_bonus_receive_title", R.string.dialog_bonus_receive_title));
        binding.tvBonusEndTitle.setText(LocaleController.getString("dialog_bonus_receive_start_title", R.string.dialog_bonus_receive_start_title));
        binding.tvReceiveStart.setText(LocaleController.getString("dialog_bonus_receive_start", R.string.dialog_bonus_receive_start));
        binding.tvReceiveWaitTip.setText(LocaleController.getString("dialog_bonus_receive_wait_tip", R.string.dialog_bonus_receive_wait_tip));
        binding.tvLookDetail.setText(LocaleController.getString("dialog_bonus_look_detail", R.string.dialog_bonus_look_detail));
        binding.tvReceiveSuccess.setText(LocaleController.getString("dialog_bonus_receive_success", R.string.dialog_bonus_receive_success));

        binding.tvReceiveStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.tvReceiveStart.setVisibility(View.GONE);
                binding.tvReceiveWaitTip.setVisibility(View.VISIBLE);
                binding.llReceiveWaiting.setVisibility(View.VISIBLE);
                checkReceiveStatus();
            }
        });
        binding.tvLookDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putSerializable("BonusStatusEntity", entity);
                BonusDetailActivity activity = new BonusDetailActivity(args, listener);
                fragment.presentFragment(activity);
                dismiss();
            }
        });
        binding.tvReceiveSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void initData() {
        binding.ivBonusBg.getLayoutParams().width = ScreenUtils.getScreenWidth() + SizeUtils.dp2px(46);
        binding.tvBonusSymbol.setText(entity.currency_name);
        binding.tvBenediction.setText(entity.message);
        startTicker();
        getBonus();
    }

    private void startTicker() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!binding.tvReceiveStart.isEnabled()) {
                            binding.tvBonusAmount.setText(new Random().nextInt(1000) + "." + new Random().nextInt(1000));
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 500);
    }

    private void getBonus() {
        binding.tvReceiveStart.setEnabled(false);
        EasyHttp.post(new ApplicationLifecycle())
                .api(new BonusGetApi()
                        .setSecret_num(entity.secret_num)
                        .setReceipt_account(WalletDaoUtils.getCurrent().getAddress()))
                .request(new OnHttpListener<BaseBean<BonusGetEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<BonusGetEntity> result) {
                        BonusGetEntity bonusGetEntity = result.getData();
                        int status = 888; // 红包已抢到
                        if (result.getCode() == 10011) status = 5; // 红包已抢完
                        else if (result.getCode() == 10012) status = 888; // 红包已抢过
                        else if (result.getCode() == 10013) status = 3; // 红包已过期
                        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertRedpacketStatusData(entity.secret_num, status);
                        listener.updateStatus();
                        if (bonusGetEntity == null) {
                            Bundle args = new Bundle();
                            args.putSerializable("BonusStatusEntity", entity);
                            BonusDetailActivity activity = new BonusDetailActivity(args, listener);
                            fragment.presentFragment(activity);
                            dismiss();
                            return;
                        }
                        binding.tvBonusEndTitle.setText(LocaleController.getString("dialog_bonus_receive_end_title", R.string.dialog_bonus_receive_end_title));
                        binding.tvReceiveStart.setEnabled(true);
                        binding.tvBonusAmount.setText(bonusGetEntity.amount);
                        binding.animationView.playAnimation();
                    }

                    @Override
                    public void onFail(Exception e) {
                        Bundle args = new Bundle();
                        args.putSerializable("BonusStatusEntity", entity);
                        BonusDetailActivity activity = new BonusDetailActivity(args, listener);
                        fragment.presentFragment(activity);
                        dismiss();
                    }
                });
    }

    private void checkReceiveStatus() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new BonusStatusApi()
                                .setSecret_num(entity.secret_num))
                        .request(new OnHttpListener<BaseBean<BonusStatusEntity>>() {

                            @Override
                            public void onSucceed(BaseBean<BonusStatusEntity> result) {
                                BonusStatusEntity entity = result.getData();
                                if (entity.status == 2 || entity.status == 5) {
                                    task.cancel();
                                    binding.tvReceiveWaitTip.setVisibility(View.GONE);
                                    binding.llReceiveWaiting.setVisibility(View.GONE);
                                    binding.tvLookDetail.setVisibility(View.VISIBLE);
                                    binding.tvReceiveSuccess.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFail(Exception e) {

                            }
                        });
            }
        };
        timer.schedule(task, 600, 3000);
    }

    @Override
    public void show() {
        super.show();
        FrameLayout bottomSheet = findViewById(R.id.design_bottom_sheet);
        bottomSheet.getLayoutParams().height = ScreenUtils.getAppScreenHeight();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);  // 设置下滑跳过折叠态
    }

    public static synchronized void showDialog(ChatActivity fragment, String secret_num, BonusReceiveDialogListener listener) {
        AlertDialog progressDialog = new AlertDialog(fragment.getContext(), 3);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        EasyHttp.post(new ApplicationLifecycle())
                .api(new BonusStatusApi()
                        .setSecret_num(secret_num))
                .request(new OnHttpListener<BaseBean<BonusStatusEntity>>() {
                    @Override
                    public void onEnd(Call call) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onSucceed(BaseBean<BonusStatusEntity> result) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        BonusStatusEntity entity = result.getData();
                        if (entity == null) return;
                        if (fragment.getCurrentChat() != null) { // 预加载一些群成员
                            TelegramUtil.loadChannelParticipants(-fragment.getDialogId(), 100, 300);
                        }
                        KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertRedpacketStatusData(entity.secret_num, entity.is_get ? 888 : entity.status);
                        listener.updateStatus();
                        boolean sender = entity.tg_user_id == fragment.getUserConfig().getClientUserId(); // 发送者
                        if (entity.source == 2 && sender || entity.is_get || entity.status != 2) { // 除了未领取都进详情
                            Bundle args = new Bundle();
                            args.putSerializable("BonusStatusEntity", entity);
                            BonusDetailActivity activity = new BonusDetailActivity(args, listener);
                            fragment.presentFragment(activity);
                            return;
                        }
                        String address = WalletDaoUtils.getCurrent().getAddress().toLowerCase();
                        if (entity.chain_id == 999) { // 波场链
                            if (!WalletUtil.isTronAddress(address)) {
                                ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_tron_address", R.string.dialog_bonus_receive_select_tron_address));
                                return;
                            }
                        } else if (entity.chain_id == 99999) { // Solana链
                            if (!WalletUtil.isSolanaAddress(address)) {
                                ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_solana_address", R.string.dialog_bonus_receive_select_solana_address));
                                return;
                            }
                        } else { // EVM链
                            if (!WalletUtil.isEvmAddress(address)) {
                                ToastUtils.showLong(LocaleController.getString("dialog_bonus_receive_select_evm_address", R.string.dialog_bonus_receive_select_evm_address));
                                return;
                            }
                        }
                        dialog = new BonusReceiveDialog(fragment, result.getData(), listener);
                        dialog.setCanceledOnTouchOutside(false);
                        if (dialog != null && !dialog.isShowing()) {
                            dialog.show();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    public interface BonusReceiveDialogListener {

        void updateStatus();

        void notRobCount(int num);
    }
}