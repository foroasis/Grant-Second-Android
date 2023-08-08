package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActInviteFriendFirstBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;

import okhttp3.Call;
import teleblock.model.InviteConfigEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.InviteConfigApi;
import teleblock.ui.adapter.InviteLevelAdapter;
import teleblock.util.EventUtil;
import teleblock.util.WalletUtil;

/**
 * 红包拉新-第一步
 */
public class InviteFriendFirstAct extends BaseFragment implements OnItemClickListener {

    private ActInviteFriendFirstBinding binding;
    private InviteLevelAdapter inviteLevelAdapter;

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActInviteFriendFirstBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        EventUtil.track(getContext(), EventUtil.Even.红包选择页面展示, new HashMap<>());

        binding.rvInviteLevel.setLayoutManager(new LinearLayoutManager(getContext()));
        inviteLevelAdapter = new InviteLevelAdapter();
        inviteLevelAdapter.setOnItemClickListener(this);
        binding.rvInviteLevel.setAdapter(inviteLevelAdapter);

        binding.ivInviteBack.setOnClickListener(view -> finishFragment());
        binding.ivGotoDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentFragment(new InviteReceiveDetailAct());
            }
        });
        binding.tvInviteNext.setOnClickListener(v -> WalletUtil.getWalletInfo(wallet -> {
            Bundle args = new Bundle();
            args.putSerializable("level", inviteLevelAdapter.getLevel());
            presentFragment(new InviteFriendSecondAct(args, () -> {
                presentFragment(new InviteReceiveDetailAct());
                finishFragment();
            }));
        }));

        binding.tvInviteTitle.setText(LocaleController.getString("invite_friend_first_title", R.string.invite_friend_first_title));
        binding.tvInviteRuleTitle.setText(LocaleController.getString("invite_friend_first_rule_title", R.string.invite_friend_first_rule_title));
        binding.tvInviteRule.setText(LocaleController.getString("invite_friend_first_rule_text", R.string.invite_friend_first_rule_text));
        binding.tvInviteLevelTitle.setText(LocaleController.getString("invite_friend_first_level_title", R.string.invite_friend_first_level_title));
        binding.tvInviteNext.setText(LocaleController.getString("invite_friend_first_next_text", R.string.invite_friend_first_next_text));
    }

    private void initData() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.llInviteTop.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;
        binding.tvInviteNext.setVisibility(View.INVISIBLE);
        loadData();
    }

    private void loadData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new InviteConfigApi())
                .request(new OnHttpListener<BaseBean<InviteConfigEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<InviteConfigEntity> result) {
                        InviteConfigEntity inviteConfigEntity = result.getData();
                        inviteLevelAdapter.setList(inviteConfigEntity.level);
                        onItemClick(inviteLevelAdapter,null,0);
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }

                    @Override
                    public void onEnd(Call call) {
                        binding.animationView.cancelAnimation();
                        binding.animationView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        EventUtil.track(getContext(), EventUtil.Even.红包选择按钮点击, new HashMap<>());
        InviteConfigEntity.Level level = inviteLevelAdapter.getItem(position);
        inviteLevelAdapter.refreshData(level);
        binding.tvInviteNext.setVisibility(View.VISIBLE);
    }
}
