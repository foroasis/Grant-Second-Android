package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActInviteReceiceDetailBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import teleblock.ui.view.InviteReceiveView;
import teleblock.ui.view.InviteSendView;
import teleblock.widget.ViewPageAdapter;

/**
 * 创建日期：2023/3/16
 * 描述：红包拉新详情
 */
public class InviteReceiveDetailAct extends BaseFragment {

    private ActInviteReceiceDetailBinding binding;
    private boolean isUserSelf = true;
    private boolean formWelcomeBonus;

    public InviteReceiveDetailAct() {
    }

    public InviteReceiveDetailAct(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if (getArguments() != null) {
            isUserSelf = getArguments().getBoolean("isUserSelf");
            formWelcomeBonus = getArguments().getBoolean("formWelcomeBonus");
        }
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("invite_friend_detail_title", R.string.invite_friend_detail_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActInviteReceiceDetailBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvGotoSend.setOnClickListener(v -> presentFragment(formWelcomeBonus ? new WelcomeBonusFirstAct() : new InviteFriendFirstAct()));

        binding.tvWithdrawalTitle.setText(LocaleController.getString("invite_friend_detail_withdrawal_title", R.string.invite_friend_detail_withdrawal_title));
        binding.tvGotoWithdrawal.setText(LocaleController.getString("invite_friend_detail_goto_withdrawal", R.string.invite_friend_detail_goto_withdrawal));
        binding.tvGotoSend.setText(LocaleController.getString("invite_friend_detail_goto_send", R.string.invite_friend_detail_goto_send));
    }

    public void initData() {
        initTabLayout();
    }

    private void initTabLayout() {
        String[] titles = {
                LocaleController.getString("invite_friend_detail_tab_send_title", R.string.invite_friend_detail_tab_send_title),
                LocaleController.getString("invite_friend_detail_tab_receive_title", R.string.invite_friend_detail_tab_receive_title)
        };
        List<View> views = new ArrayList<>();
        views.add(new InviteSendView(this));
        views.add(new InviteReceiveView(this));
        binding.tabLayout.setTitle(titles);
        binding.viewPager.setAdapter(new ViewPageAdapter(views));
        binding.viewPager.setOffscreenPageLimit(views.size());
        binding.tabLayout.setViewPager(binding.viewPager);
        if (!isUserSelf) binding.tabLayout.setCurrentTab(1);
    }
}