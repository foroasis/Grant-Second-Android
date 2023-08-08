package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.LayoutNoticeDetailsBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.text.SimpleDateFormat;

import teleblock.model.NoticeEntity;
import teleblock.util.TelegramUtil;


public class NoticeDetailsActivity extends BaseFragment {
    private LayoutNoticeDetailsBinding binding;
    NoticeEntity noticeEntity;

    public NoticeDetailsActivity(NoticeEntity noticeEntity) {
        this.noticeEntity = noticeEntity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_back_black);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setCastShadows(false);
        actionBar.setBackgroundColor(Color.parseColor("#FFEFEFEF"));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        View view = initView();
        initData();
        return view;
    }

    private View initView() {
        binding = LayoutNoticeDetailsBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvSkip.setOnClickListener(v -> {
            TelegramUtil.gotoJump(this,noticeEntity.link);
        });
        return fragmentView = binding.getRoot();
    }

    private void initData() {
        Glide.with(getContext()).load(noticeEntity.icon).into(binding.ivIcon);
        binding.tvType.setText(noticeEntity.type);
        binding.tvTitle.setText(Html.fromHtml(noticeEntity.title));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        binding.tvTime1.setText(formatter.format(noticeEntity.timestamp * 1000));
        binding.tvTime2.setText(formatter.format(noticeEntity.timestamp * 1000));
        binding.tvContent.setText(Html.fromHtml(noticeEntity.content));
        binding.tvSkip.setText(LocaleController.getString("notice_tv_skip", R.string.notice_tv_skip));
        if (TextUtils.isEmpty(noticeEntity.content)) {
            binding.tvContent.setVisibility(View.GONE);
            binding.tvTime2.setVisibility(View.GONE);
            binding.line.setVisibility(View.GONE);
        } else {
            binding.tvTime1.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(noticeEntity.link)) {
            binding.tvSkip.setVisibility(View.GONE);
        }
    }
}
