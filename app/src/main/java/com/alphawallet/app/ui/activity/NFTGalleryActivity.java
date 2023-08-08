package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.ruffian.library.widget.RTextView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ActivityNftGalleryBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.NFTGalleryManager;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.ui.dialog.ChainTypeSelectorDialog;
import teleblock.util.JsonUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.divider.CustomItemDecoration;

/**
 * NFT橱窗
 */
public class NFTGalleryActivity extends BaseFragment implements OnItemClickListener, OnRefreshListener {

    private ActivityNftGalleryBinding binding;
    private NFTAssetsAdapter nftListAdapter;
    private long user_id;
    private long dialog_id;
    private String address;
    private Web3ConfigEntity.WalletNetworkConfigChainType currentChainType;
    private RTextView rightTv;

    public NFTGalleryActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        user_id = getArguments().getLong("user_id");
        dialog_id = getArguments().getLong("dialog_id");
        address = getArguments().getString("address");
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        EventBus.getDefault().unregister(this);
        EasyHttp.cancel(getClass().getSimpleName());
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("nft_gallery_title", R.string.nft_gallery_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        binding = ActivityNftGalleryBinding.inflate(LayoutInflater.from(context));
        addRightView();
        initView();
        initData();
        initStyle();
        return fragmentView = binding.getRoot();
    }

    private void addRightView() {
        int additionalTop = AndroidUtilities.getStatusBarHeight(getParentActivity());
        float marginTop = additionalTop + (ActionBar.getCurrentActionBarHeight() - SizeUtils.dp2px(30)) / 2f;
        rightTv = (RTextView) LayoutInflater.from(getParentActivity()).inflate(R.layout.layout_switch_chain, null);
        Drawable rightDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_ab_new);
        rightDrawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
        rightTv.getHelper().setIconNormalRight(rightDrawable);
        actionBar.addView(rightTv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 30, Gravity.RIGHT, 0f, SizeUtils.px2dp(marginTop), 12f, 0f));

        rightTv.setOnClickListener(view ->
                new ChainTypeSelectorDialog(getContext(), true, data -> {
                    updateChainType(data);
                }).setCurrentChainType(currentChainType).show()
        );
    }

    private void updateChainType(Web3ConfigEntity.WalletNetworkConfigChainType data) {
        currentChainType = data;
        if (currentChainType.getId() == 0) {
            rightTv.setText(LocaleController.getString("dialog_selector_chaintype_all_chain", R.string.dialog_selector_chaintype_all_chain));
            rightTv.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(R.drawable.icon_all_network_dialog_wallet));
        } else {
            rightTv.setText(currentChainType.getName());
            GlideHelper.getDrawableGlide(binding.getRoot().getContext(), currentChainType.getIcon(), drawable -> rightTv.getHelper().setIconNormalLeft(drawable));
        }
        binding.refreshLayout.autoRefresh();
    }

    private void initView() {
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getParentActivity(), 2));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(2, 20f, 20f, true));
        nftListAdapter = new NFTAssetsAdapter(true);
        nftListAdapter.setEmptyView(nftListAdapter.createEmptyView(getParentActivity()));
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(nftListAdapter);
    }

    private void initStyle() {
        actionBar.setBackgroundColor(Color.parseColor("#ffffff"));
        actionBar.setTitleColor(Color.parseColor("#000000"));
        actionBar.getBackButton().setColorFilter(Color.BLACK);
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), true), 200);
    }


    private void initData() {
        updateChainType(new Web3ConfigEntity.WalletNetworkConfigChainType(0));
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadData();
    }

    private void loadData() {
        nftListAdapter.setList(null);
        NFTGalleryManager.getInstance().getAllNFTGallery(currentChainType.getId(), address, new BlockCallback<List<NFTInfo>>() {
            @Override
            public void onProgress(int index, String data) {
                super.onProgress(index, data);
                NFTInfo nftInfo = JsonUtil.parseJsonToBean(data, NFTInfo.class);
                if (nftInfo != null) {
                    for (int i = 0; i < nftListAdapter.getData().size(); i++) {
                        NFTInfo info = nftListAdapter.getData().get(i);
                        if (info.equals(nftInfo)) {
                            nftListAdapter.setData(i, nftInfo);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onSuccess(List<NFTInfo> data) {
                super.onSuccess(data);
                nftListAdapter.setList(data);
                binding.refreshLayout.finishRefresh();
                if (nftListAdapter.getData().isEmpty()) {
                    nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                } else {
                    nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        NFTGalleryManager.getInstance().nftIndexPage(this, nftListAdapter, dialog_id, UserObject.isUserSelf(getMessagesController().getUser(user_id)), position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CREATED:
                break;
        }
    }
}
