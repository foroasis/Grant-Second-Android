package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ActivityBonusDetailBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.model.BonusDetailsEntity;
import teleblock.model.BonusStatusEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.BonusDetailsApi;
import teleblock.ui.adapter.BonusRecordAdapter;
import teleblock.ui.dialog.BonusReceiveDialog;

/**
 * 红包结果页
 */
public class BonusDetailActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ActivityBonusDetailBinding binding;
    private BonusStatusEntity entity;
    private TLRPC.User user;
    private BonusRecordAdapter bonusRecordAdapter;

    private BonusReceiveDialog.BonusReceiveDialogListener listener;

    public BonusDetailActivity(Bundle args, BonusReceiveDialog.BonusReceiveDialogListener listener) {
        super(args);
        this.listener = listener;
        entity = (BonusStatusEntity) args.getSerializable("BonusStatusEntity");
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoad);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        binding = ActivityBonusDetailBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.viewStatusBar.getLayoutParams().height = AndroidUtilities.statusBarHeight;
        binding.ivClose.setOnClickListener(view -> {
            finishFragment();
        });

        binding.rvReceiveRecord.setLayoutManager(new LinearLayoutManager(getContext()));
        bonusRecordAdapter = new BonusRecordAdapter(entity.currency_name);
        binding.rvReceiveRecord.setAdapter(bonusRecordAdapter);

        binding.tvRefundTip.setText(LocaleController.getString("dialog_send_redpackets_tips", R.string.dialog_send_redpackets_tips));
    }

    private void initData() {
        user = getMessagesController().getUser(entity.tg_user_id);
        updateUserInfo();
        getBonusDetail();
    }

    private void updateUserInfo() {
        binding.tvFromUser.setText(String.format(LocaleController.getString("bonus_detail_bonus_from_receiver", R.string.bonus_detail_bonus_from_receiver), UserObject.getUserName(user)));
        binding.flAvatar.setUserInfo(user).loadView();
    }

    private void getBonusDetail() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new BonusDetailsApi()
                        .setSecret_num(entity.secret_num))
                .request(new OnHttpListener<BaseBean<BonusDetailsEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<BonusDetailsEntity> result) {
                        showDetail(result.getData());
                    }

                    @Override
                    public void onFail(Exception e) {
                        showError();
                    }
                });
    }

    private void showDetail(BonusDetailsEntity data) {
        if (data == null) { // 红包异常
            showError();
            return;
        }
        boolean sender = data.tg_user_id == getUserConfig().getClientUserId(); // 发送者
        if (sender) {
            binding.tvFromUser.setText(String.format(LocaleController.getString("bonus_detail_bonus_from_sender", R.string.bonus_detail_bonus_from_sender), UserObject.getUserName(user)));
        } else {
            binding.tvFromUser.setText(String.format(LocaleController.getString("bonus_detail_bonus_from_receiver", R.string.bonus_detail_bonus_from_receiver), UserObject.getUserName(user)));
        }
        // 红包状态,1=发布待上链确认,2=上链确认,3=时间到期关闭,4=异常关闭,5=已经抢购完毕
        switch (data.status) {
            case 2: // 已确认
                if (data.is_get) {
                    showReceivedStatus(data);
                } else {
                    showWaitStatus(data);
                }
                break;
            case 3: // 已过期
                showExpiredStatus(data.source == 2, sender);
                break;
            case 5: // 已抢完
                if (data.is_get) {
                    showReceivedStatus(data);
                } else {
                    showFinishedStatus(data.source == 2, sender);
                }
                break;
        }
        if (data.source == 1) { // 群发
            if (data.num > data.num_exec) { // 没抢完
                int num = data.num - data.num_exec;
                String format = LocaleController.getString("bonus_detail_record_group_no_finished", R.string.bonus_detail_record_group_no_finished);
                binding.tvReceiveRecord.setText(String.format(format, data.num, num));
                if (num == 5 || num == 1 || num == 10) {
                    if (data.status == 2) listener.notRobCount(num);
                }
            } else {
                String format = LocaleController.getString("bonus_detail_record_group_finished", R.string.bonus_detail_record_group_finished);
                binding.tvReceiveRecord.setText(String.format(format, data.num));
            }
        }
        bonusRecordAdapter.setList(data.record);
    }

    private void showError() {
        binding.llBonusNormal.setVisibility(View.GONE);
        binding.llBonusError.setVisibility(View.VISIBLE);
        binding.tvErrorTitle.setText(LocaleController.getString("bonus_detail_error_title", R.string.bonus_detail_error_title));
        binding.tvErrorTip.setText(LocaleController.getString("bonus_detail_error_tip", R.string.bonus_detail_error_tip));
    }

    // 已领取
    private void showReceivedStatus(BonusDetailsEntity data) {
        binding.tvBonusTitle.setText(LocaleController.getString("bonus_detail_bonus_received_title", R.string.bonus_detail_bonus_received_title));
        BonusDetailsEntity.RecordEntity recordEntity = CollectionUtils.find(data.record, new CollectionUtils.Predicate<BonusDetailsEntity.RecordEntity>() {
            @Override
            public boolean evaluate(BonusDetailsEntity.RecordEntity item) {
                return item.tg_user_id == getUserConfig().getClientUserId();
            }
        });
        if (recordEntity != null) {
            SpanUtils.with(binding.tvReceiveStatus)
                    .append(recordEntity.amount).setFontSize(SizeUtils.sp2px(50))
                    .append(" " + data.currency_name)
                    .create();
            if (recordEntity.status == 1) { // 待上链
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_received_tip", R.string.bonus_detail_status_received_tip));
            } else {
                binding.tvReceiveTip.setText("$" + recordEntity.usd_amount);
            }
        }
        if (data.source == 2) { // 单发
            binding.tvReceiveRecord.setText(LocaleController.getString("bonus_detail_record_single_receiver_received", R.string.bonus_detail_record_single_receiver_received));
        }
    }

    // 待领取
    private void showWaitStatus(BonusDetailsEntity data) {
        binding.tvBonusTitle.setText(LocaleController.getString("bonus_detail_bonus_received_title", R.string.bonus_detail_bonus_received_title));
        if (data.source == 2) { // 单发
            binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_single_sender_wait_title", R.string.bonus_detail_status_single_sender_wait_title));
            binding.tvReceiveTip.setVisibility(View.GONE);
            String format = LocaleController.getString("bonus_detail_record_single_sender_wait", R.string.bonus_detail_record_single_sender_wait);
            binding.tvReceiveRecord.setText(String.format(format, data.amount + data.currency_name));
        } else {
//            binding.rlBonusBg.setSizeRatio(360, 190);
        }
    }

    // 已过期
    private void showExpiredStatus(boolean single, boolean sender) {
        binding.tvBonusTitle.setText(LocaleController.getString("bonus_detail_bonus_expired_title", R.string.bonus_detail_bonus_expired_title));
        if (single) {
            if (sender) {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_single_sender_expired_title", R.string.bonus_detail_status_single_sender_expired_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_single_sender_expired_tip", R.string.bonus_detail_status_single_sender_expired_tip));
                binding.tvReceiveRecord.setText(LocaleController.getString("bonus_detail_record_single_sender_expired", R.string.bonus_detail_record_single_sender_expired));
            } else {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_single_receiver_expired_title", R.string.bonus_detail_status_single_receiver_expired_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_single_receiver_expired_tip", R.string.bonus_detail_status_single_receiver_expired_tip));
                binding.tvReceiveRecord.setText(LocaleController.getString("bonus_detail_record_single_receiver_expired", R.string.bonus_detail_record_single_receiver_expired));
            }
        } else {
            if (sender) {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_group_expired_title", R.string.bonus_detail_status_group_expired_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_group_sender_expired_tip", R.string.bonus_detail_status_group_sender_expired_tip));
            } else {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_group_expired_title", R.string.bonus_detail_status_group_expired_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_group_receiver_expired_tip", R.string.bonus_detail_status_group_receiver_expired_tip));
            }
        }
    }

    // 已抢完
    private void showFinishedStatus(boolean single, boolean sender) {
        binding.tvBonusTitle.setText(LocaleController.getString("bonus_detail_bonus_finished_title", R.string.bonus_detail_bonus_finished_title));
        if (single) {
            if (sender) {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_single_sender_received_title", R.string.bonus_detail_status_single_sender_received_title));
                binding.tvReceiveTip.setVisibility(View.GONE);
                binding.tvReceiveRecord.setText(LocaleController.getString("bonus_detail_record_single_sender_received", R.string.bonus_detail_record_single_sender_received));
            }
        } else {
            if (sender) {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_group_finished_title", R.string.bonus_detail_status_group_finished_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_group_sender_finished_tip", R.string.bonus_detail_status_group_sender_finished_tip));
            } else {
                binding.tvReceiveStatus.setText(LocaleController.getString("bonus_detail_group_finished_title", R.string.bonus_detail_status_group_finished_title));
                binding.tvReceiveTip.setText(LocaleController.getString("bonus_detail_group_receiver_finished_tip", R.string.bonus_detail_status_group_receiver_finished_tip));
            }
        }
    }


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userInfoDidLoad) {
            user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(entity.tg_user_id);
            updateUserInfo();
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                bonusRecordAdapter.notifyDataSetChanged();
            }
        }
    }
}
