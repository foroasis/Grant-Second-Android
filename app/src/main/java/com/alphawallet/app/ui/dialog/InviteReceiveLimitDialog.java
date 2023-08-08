package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogInviteReceiveLimitBinding;

import java.util.HashMap;

import teleblock.ui.activity.WelcomeBonusFirstAct;
import teleblock.util.EventUtil;
import teleblock.util.WalletUtil;

/**
 * 创建日期：2023/3/9
 * 描述：领取上限弹窗
 */
public class InviteReceiveLimitDialog extends BaseAlertDialog {

    private DialogInviteReceiveLimitBinding binding;
    private int code;
    private String msg;
    private Runnable runnable;

    public InviteReceiveLimitDialog(Context context, int code, String msg, Runnable runnable) {
        super(context);
        this.code = code;
        this.msg = msg;
        this.runnable = runnable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogInviteReceiveLimitBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        initView();
    }

    private void initView() {
        binding.tvSend.setVisibility(code == 140001 ? View.INVISIBLE : View.VISIBLE);

        binding.tvSend.setOnClickListener(v -> {
            runnable.run();
            dismiss();
        });
        binding.tvCancel.setOnClickListener(v -> dismiss());

        binding.tvTitle.setText(LocaleController.getString("invite_receive_limit_title", R.string.invite_receive_limit_title));
        binding.tvDesc.setText(msg);
        binding.tvSend.setText(LocaleController.getString("invite_receive_limit_send", R.string.invite_receive_limit_send));
        binding.tvCancel.setText(LocaleController.getString("invite_receive_limit_cancel", R.string.invite_receive_limit_cancel));
    }
}