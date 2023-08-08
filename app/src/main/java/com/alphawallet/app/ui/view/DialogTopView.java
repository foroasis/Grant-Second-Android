package teleblock.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ViewDialogTopBinding;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;

public class DialogTopView extends FrameLayout {

    public ViewDialogTopBinding binding;

    public DialogTopView(Context context) {
        super(context);
        initView();
        initData();
    }

    private void initView() {
        binding = ViewDialogTopBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.tvTitle.setSelected(true); // 不设置这个属性,字体不会开始滚动
        binding.tvTitle.setText(LocaleController.getString("dialog_activity_top_redpacket_tips", R.string.dialog_activity_top_redpacket_tips));
        setOnClickListener(v -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.SWITCH_BONUS_LIST_PAGE));
        });
    }

    public void initData() {

    }

    public void updateStyle(int num) {
        binding.tvNum.setText(String.valueOf(num));
        setVisibility(num > 0 ? VISIBLE : GONE);
    }
}