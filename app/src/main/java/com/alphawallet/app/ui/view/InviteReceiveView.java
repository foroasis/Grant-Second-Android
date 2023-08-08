package teleblock.ui.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
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
import org.telegram.messenger.databinding.ViewInviteReceiveHeaderBinding;

import teleblock.model.BaseLoadmoreModel;
import teleblock.model.InviteReceiveEntity;
import teleblock.model.InviteSendEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.InviteReceiveInfoApi;
import teleblock.network.api.InviteReceiveListApi;
import teleblock.network.api.InviteSendInfoApi;
import teleblock.network.api.InviteSendListApi;
import teleblock.ui.activity.InviteReceiveDetailAct;
import teleblock.ui.adapter.InviteReceiveAdapter;
import teleblock.ui.adapter.InviteSendAdapter;
import teleblock.widget.divider.CustomItemDecoration;

public class InviteReceiveView extends FrameLayout implements OnRefreshListener, OnLoadMoreListener, OnItemClickListener {

    private LayoutBaseRefreshBinding binding;
    private ViewInviteReceiveHeaderBinding headerBinding;
    private InviteReceiveDetailAct activity;
    private InviteReceiveAdapter inviteReceiveAdapter;
    private int page;

    public InviteReceiveView(InviteReceiveDetailAct activity) {
        super(activity.getParentActivity());
        this.activity = activity;
        initView();
        initData();
    }

    private void initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inviteReceiveAdapter = new InviteReceiveAdapter();
        inviteReceiveAdapter.addHeaderView(createHeaderView());
        inviteReceiveAdapter.getHeaderLayout().setVisibility(GONE);
        inviteReceiveAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        inviteReceiveAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(inviteReceiveAdapter);
    }

    private View createHeaderView() {
        headerBinding = ViewInviteReceiveHeaderBinding.inflate(LayoutInflater.from(getContext()));
        return headerBinding.getRoot();
    }

    private void initData() {
        getInfo();
        binding.refreshLayout.autoRefresh();
    }

    private void getInfo() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new InviteReceiveInfoApi())
                .request(new OnHttpListener<BaseBean<InviteReceiveEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<InviteReceiveEntity> result) {
                        inviteReceiveAdapter.getHeaderLayout().setVisibility(VISIBLE);
                        InviteReceiveEntity inviteReceive = result.getData();
                        headerBinding.tvEarnAmount.setText(LocaleController.getString("invite_friend_detail_tab_receive_amount_text", R.string.invite_friend_detail_tab_receive_amount_text) + inviteReceive.amount + " " + inviteReceive.currency_name);
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
                .api(new InviteReceiveListApi()
                        .setPage(page))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<InviteReceiveEntity.RecordEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<InviteReceiveEntity.RecordEntity>> result) {
                        if (page == 1) {
                            inviteReceiveAdapter.setList(result.getData().getData());
                        } else {
                            inviteReceiveAdapter.addData(result.getData().getData());
                        }
                        binding.refreshLayout.finishRefresh();
                        if (result.getData().whetherRemaining()) {
                            inviteReceiveAdapter.getLoadMoreModule().loadMoreComplete();
                        } else {
                            inviteReceiveAdapter.getLoadMoreModule().loadMoreEnd(true);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        binding.refreshLayout.finishRefresh();
                        inviteReceiveAdapter.getLoadMoreModule().loadMoreFail();
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }

}