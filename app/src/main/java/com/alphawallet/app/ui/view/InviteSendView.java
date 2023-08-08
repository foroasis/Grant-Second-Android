package teleblock.ui.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRefreshBinding;
import org.telegram.messenger.databinding.ViewInviteSendEmptyBinding;
import org.telegram.messenger.databinding.ViewInviteSendHeaderBinding;
import org.telegram.messenger.databinding.ViewTokenEmptyBinding;

import okhttp3.Call;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.GameUserEntity;
import teleblock.model.InviteConfigEntity;
import teleblock.model.InviteSendEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.InviteSendInfoApi;
import teleblock.network.api.InviteSendListApi;
import teleblock.ui.activity.InviteFriendSecondAct;
import teleblock.ui.activity.InviteReceiveDetailAct;
import teleblock.ui.activity.WelcomeBonusFirstAct;
import teleblock.ui.adapter.InviteSendAdapter;

public class InviteSendView extends FrameLayout implements OnRefreshListener, OnLoadMoreListener, OnItemClickListener, OnItemChildClickListener {

    private LayoutBaseRefreshBinding binding;
    private ViewInviteSendHeaderBinding headerBinding;
    private InviteReceiveDetailAct activity;
    private InviteSendAdapter inviteSendAdapter;
    private int page;

    public InviteSendView(InviteReceiveDetailAct activity) {
        super(activity.getParentActivity());
        this.activity = activity;
        initView();
        initData();
    }

    private void initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inviteSendAdapter = new InviteSendAdapter();
        inviteSendAdapter.setEmptyView(createEmptyView());
        inviteSendAdapter.getEmptyLayout().setVisibility(GONE);
        inviteSendAdapter.addHeaderView(createHeaderView());
        inviteSendAdapter.getHeaderLayout().setVisibility(GONE);
        inviteSendAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        inviteSendAdapter.setOnItemClickListener(this);
        inviteSendAdapter.setOnItemChildClickListener(this);
        binding.recyclerView.setAdapter(inviteSendAdapter);
    }

    private View createEmptyView() {
        ViewInviteSendEmptyBinding binding = ViewInviteSendEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmpty.setText(LocaleController.getString("invite_friend_detail_tab_send_empty_text", R.string.invite_friend_detail_tab_send_empty_text));
        return binding.getRoot();
    }

    private View createHeaderView() {
        headerBinding = ViewInviteSendHeaderBinding.inflate(LayoutInflater.from(getContext()));
        headerBinding.tvUnreceivedAmountTitle.setText(LocaleController.getString("invite_friend_detail_tab_send_unreceived_title", R.string.invite_friend_detail_tab_send_unreceived_title));
        return headerBinding.getRoot();
    }

    private void initData() {
        getInfo();
        binding.refreshLayout.autoRefresh();
    }

    private void getInfo() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new InviteSendInfoApi())
                .request(new OnHttpListener<BaseBean<InviteSendEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<InviteSendEntity> result) {
                        inviteSendAdapter.getHeaderLayout().setVisibility(VISIBLE);
                        InviteSendEntity inviteSend = result.getData();
                        headerBinding.tvTotalAmount.setText(String.format(LocaleController.getString("invite_friend_detail_tab_send_total_text", R.string.invite_friend_detail_tab_send_total_text), inviteSend.total, inviteSend.earn_amount + " "+inviteSend.currency_name));
                        headerBinding.tvUnreceivedAmount.setText(inviteSend.unreceived_amount + " " + inviteSend.currency_name);
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                    }
                });
    }


    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        page = 1;
        loadData();
    }

    @Override
    public void onLoadMore() {
        page++;
        loadData();
    }

    private void loadData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new InviteSendListApi()
                        .setPage(page))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<InviteSendEntity.RecordEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<InviteSendEntity.RecordEntity>> result) {
                        if (page == 1) {
                            inviteSendAdapter.setList(result.getData().getData());
                        } else {
                            inviteSendAdapter.addData(result.getData().getData());
                        }
                        binding.refreshLayout.finishRefresh();
                        if (result.getData().whetherRemaining()) {
                            inviteSendAdapter.getLoadMoreModule().loadMoreComplete();
                        } else {
                            inviteSendAdapter.getLoadMoreModule().loadMoreEnd(true);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        binding.refreshLayout.finishRefresh();
                        inviteSendAdapter.getLoadMoreModule().loadMoreFail();
                    }

                    @Override
                    public void onEnd(Call call) {
                        inviteSendAdapter.getEmptyLayout().setVisibility(inviteSendAdapter.getData().isEmpty() ? VISIBLE : GONE);
                    }
                });
    }


    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }


    @Override
    public void onItemChildClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        InviteSendEntity.RecordEntity recordEntity = inviteSendAdapter.getItem(position);
        if (recordEntity.users_number == recordEntity.users_number_exec) return;
        if (recordEntity.type == 1) {
            InviteConfigEntity.Level level = new InviteConfigEntity.Level();
            level.id = recordEntity.level_id;
            level.amount = recordEntity.amount;
            level.numbers = recordEntity.users_number;
            level.currency_name = recordEntity.currency_name;
            Bundle args = new Bundle();
            args.putSerializable("level", level);
            args.putString("promotion_number",recordEntity.promotion_number);
            activity.presentFragment(new InviteFriendSecondAct(args, () -> activity.initData()));
        } else {
            activity.presentFragment(new WelcomeBonusFirstAct());
        }
    }
}