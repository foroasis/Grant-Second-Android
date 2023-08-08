package teleblock.ui.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.LayoutBaseRefreshBinding;
import org.telegram.messenger.databinding.ViewNoticeEmptyBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

import okhttp3.Call;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.NoticeEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.NoticeMessagesApi;
import teleblock.ui.adapter.NoticeCenterAdapter;
import teleblock.util.MMKVUtil;


public class NoticeCenterActivity extends BaseFragment implements OnItemClickListener, OnRefreshListener, OnLoadMoreListener {

    private LayoutBaseRefreshBinding binding;
    private NoticeCenterAdapter noticeCenterAdapter;
    private ActionBarMenuItem deleteItem;
    private boolean deleteModel;
    private int page;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("notice_center_title", R.string.notice_center_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 100) {
                    deleteModel = !deleteModel;
                    noticeCenterAdapter.deleteModel(deleteModel);
                    if (deleteModel) {
                        deleteItem.setIcon(R.drawable.notice_center_top_done);
                    } else {
                        deleteItem.setIcon(R.drawable.notice_center_top_delete);
                    }
                }
            }
        });
        View view = initView();
        loadData();
        return view;
    }

    private View initView() {
        binding = LayoutBaseRefreshBinding.inflate(LayoutInflater.from(getContext()));
        ActionBarMenu menu = actionBar.createMenu();
        deleteItem = menu.addItem(100, R.drawable.notice_center_top_delete);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noticeCenterAdapter = new NoticeCenterAdapter();
        noticeCenterAdapter.setEmptyView(createEmptyView());
        noticeCenterAdapter.getEmptyLayout().setVisibility(GONE);
        noticeCenterAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        noticeCenterAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(noticeCenterAdapter);
        return fragmentView = binding.getRoot();
    }

    private View createEmptyView() {
        ViewNoticeEmptyBinding binding = ViewNoticeEmptyBinding.inflate(LayoutInflater.from(getParentActivity()));
        binding.tvEmpty.setText(LocaleController.getString("notice_empty_text", R.string.notice_empty_text));
        return binding.getRoot();
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
                .api(new NoticeMessagesApi()
                        .setPage(page))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<NoticeEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<NoticeEntity>> result) {
                        List<NoticeEntity> list = result.getData().getData();
                        if (CollectionUtils.isNotEmpty(list)) {
                            MMKVUtil.readNoticeId(list.get(0).id);
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.NOTICE_HAS_READ));
                        }
                        if (page == 1) {
                            noticeCenterAdapter.setList(list);
                        } else {
                            noticeCenterAdapter.addData(list);
                        }
                        binding.refreshLayout.finishRefresh();
                        if (result.getData().whetherRemaining()) {
                            noticeCenterAdapter.getLoadMoreModule().loadMoreComplete();
                        } else {
                            noticeCenterAdapter.getLoadMoreModule().loadMoreEnd(true);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        binding.refreshLayout.finishRefresh();
                        noticeCenterAdapter.getLoadMoreModule().loadMoreFail();
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (noticeCenterAdapter.getData().isEmpty()) {
                            noticeCenterAdapter.getEmptyLayout().setVisibility(VISIBLE);
                            deleteItem.setVisibility(GONE);
                        } else {
                            noticeCenterAdapter.getEmptyLayout().setVisibility(GONE);
                        }
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        NoticeEntity noticeEntity = noticeCenterAdapter.getItem(position);
        if (!deleteModel) {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.NOTICE_HAS_READ));
            presentFragment(new NoticeDetailsActivity(noticeEntity));
        } else {
            MMKVUtil.deleteNoticeIds(noticeEntity.id + "");//删除
            noticeCenterAdapter.removeAt(position);
        }
    }
}
