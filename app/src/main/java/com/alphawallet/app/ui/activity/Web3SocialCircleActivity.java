package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActivityWeb3SocialCircleBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.blockchain.bnb.bean.AddressWallet;
import teleblock.blockchain.bnb.bean.FollowingShip;
import teleblock.database.KKVideoMessageDB;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.CCprofileBannerEntity;
import teleblock.model.TransferHistoryEntity;
import teleblock.model.ui.Web3SocialCircleData;
import teleblock.model.ui.Web3SocialCircleNodeData;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.CCprofileBannerApi;
import teleblock.network.api.TransferHistoryApi;
import teleblock.network.api.blockchain.bnb.CyberConnectApi;
import teleblock.ui.adapter.Web3SocialCircleAdapter;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.widget.CommonCallback;
import teleblock.widget.GlideHelper;
import timber.log.Timber;

/**
 * Time:2023/2/27
 * Author:Perry
 * Description：web3社交圈
 */
public class Web3SocialCircleActivity extends BaseFragment {

    private ActivityWeb3SocialCircleBinding activityWeb3SocialCircleBinding;
    private Web3SocialCircleAdapter mWeb3SocialCircleAdapter;

    //telegram用户数据列表
    private List<Web3SocialCircleData> telegramWeb3SocialCircleList = new ArrayList<>();
    //钱包地址用户数据
    private ArrayList<String> walletAddressList = new ArrayList<>();
    //未关注用户列表数据
    private List<Web3SocialCircleData> unFollowList = new ArrayList<>();
    //要关注的人的列表
    private List<Web3SocialCircleData> requestFollowList = new ArrayList<>();
    //当前请求关注的第几个数据
    private int requestFollowPosition = 0;

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
        EasyHttp.cancel(this.getClass().getSimpleName());
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setCastShadows(false);
        actionBar.setTitle(LocaleController.getString("contacts_web3_social_circle_title", R.string.contacts_web3_social_circle_title));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        EventUtil.track(getContext(), EventUtil.Even.web3社交圈页面展示, new HashMap<>());
        activityWeb3SocialCircleBinding = ActivityWeb3SocialCircleBinding.inflate(LayoutInflater.from(context));

        initView();
        initData();
        return fragmentView = activityWeb3SocialCircleBinding.getRoot();
    }

    private void initView() {
        activityWeb3SocialCircleBinding.rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        activityWeb3SocialCircleBinding.rvContacts.setAdapter(mWeb3SocialCircleAdapter = new Web3SocialCircleAdapter());

        //点击事件
        mWeb3SocialCircleAdapter.addChildClickViewIds(R.id.tv_btn);
        mWeb3SocialCircleAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            Web3SocialCircleNodeData socialCircleData = mWeb3SocialCircleAdapter.getData().get(position);
            if (socialCircleData.getItemType() == Web3SocialCircleNodeData.TYPE_TITLE) {
                return;
            }

            if (!socialCircleData.getData().isIfActivate()) {
                EventUtil.track(getContext(), EventUtil.Even.邀请按钮点击, new HashMap<>());
                Bundle args = new Bundle();
                args.putLong("user_id", socialCircleData.getData().getTgUserData().id);
                args.putInt("operationType", ChatActivity.opera_invite_mint_bnb_nft);
                ChatActivity chatActivity = new ChatActivity(args);
                presentFragment(chatActivity);
                return;
            }

            //获取关注状态
            boolean ifFollow = socialCircleData.getData().isIfPayAttentionTo();
            if (ifFollow) {
                EventUtil.track(getContext(), EventUtil.Even.取消关注按钮点击, new HashMap<>());
                new UniversalDialog(getContext(), () -> {
                    unFollowUser(position);
                }).setTitle(LocaleController.getString("contacts_web3_social_circle_unpayattention_tips", R.string.contacts_web3_social_circle_unpayattention_tips)).show();
            } else {
                EventUtil.track(getContext(), EventUtil.Even.关注按钮点击, new HashMap<>());
                followUser(position);
            }
        });
        //跳转到个人主页
        mWeb3SocialCircleAdapter.setOnItemClickListener((adapter, view, position) -> {
            Web3SocialCircleNodeData socialCircleData = mWeb3SocialCircleAdapter.getData().get(position);
            if (socialCircleData.getItemType() == Web3SocialCircleNodeData.TYPE_DATA && socialCircleData.getData().isIfContacts()) {
                Bundle args = new Bundle();
                TLRPC.User user = socialCircleData.getData().getTgUserData();
                args.putLong("user_id", user.id);
                ProfileActivity profileActivity = new ProfileActivity(args);
                profileActivity.setPlayProfileAnimation(0);
                presentFragment(profileActivity);
            }
        });
    }

    /**
     * 关注对方
     * @param position
     */
    private void followUser(int position) {
        Web3SocialCircleNodeData socialCircleData = mWeb3SocialCircleAdapter.getData().get(position);
        TelegramUtil.followOrUnFollowBnbUser(socialCircleData.getData().getHandle(), false, new TelegramUtil.FollowSignatureResultListener() {
            @Override
            public void onStart() {
                AlertDialog progressDialog = new AlertDialog(getContext(), 3);
                progressDialog.setCanCancel(false);
                showDialog(progressDialog);
            }

            @Override
            public void requestSuccessful() {
                socialCircleData.getData().setIfPayAttentionTo(true);
                mWeb3SocialCircleAdapter.setData(position, socialCircleData);
            }

            @Override
            public void requestError(String error) {
                ToastUtils.showLong(error);
            }

            @Override
            public void onEnd() {
                if (getVisibleDialog() != null) {
                    getVisibleDialog().dismiss();
                }
            }
        });
    }

    /**
     * 取消关注对方
     * @param position
     */
    private void unFollowUser(int position) {
        Web3SocialCircleNodeData socialCircleData = mWeb3SocialCircleAdapter.getItem(position);
        TelegramUtil.followOrUnFollowBnbUser(socialCircleData.getData().getHandle(), true, new TelegramUtil.FollowSignatureResultListener() {
            @Override
            public void onStart() {
                AlertDialog progressDialog = new AlertDialog(getContext(), 3);
                progressDialog.setCanCancel(false);
                showDialog(progressDialog);
            }

            @Override
            public void requestSuccessful() {
                socialCircleData.getData().setIfPayAttentionTo(false);
                mWeb3SocialCircleAdapter.setData(position, socialCircleData);
            }

            @Override
            public void requestError(String error) {
                ToastUtils.showLong(error);
            }

            @Override
            public void onEnd() {
                if (getVisibleDialog() != null) {
                    getVisibleDialog().dismiss();
                }
            }
        });
    }

    private void initData() {
        //请求banner
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new CCprofileBannerApi())
                .request(new OnHttpListener<BaseBean<CCprofileBannerEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<CCprofileBannerEntity> result) {
                        CCprofileBannerEntity cCprofileBannerEntity = result.getData();
                        if (cCprofileBannerEntity != null) {
                            activityWeb3SocialCircleBinding.ivBanner.setVisibility(View.VISIBLE);
                            GlideHelper.displayImage(getContext(), activityWeb3SocialCircleBinding.ivBanner, cCprofileBannerEntity.getImage(), Color.TRANSPARENT, Color.TRANSPARENT);
                            activityWeb3SocialCircleBinding.ivBanner.setOnClickListener(view -> {
                                Browser.openUrl(getContext(), cCprofileBannerEntity.getUrl());
                            });
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });

        telegramWeb3SocialCircleList.clear();
        walletAddressList.clear();

        //所有的联系人数据
        ArrayList<Long> contactsIds = new ArrayList<>();
        ArrayList<TLRPC.TL_contact> contacts = ContactsController.getInstance(UserConfig.selectedAccount).contacts;
        for (TLRPC.TL_contact contact : contacts) {
            contactsIds.add(contact.user_id);
        }

        //批量请求用户钱包数据
        TelegramUtil.getUserCCProfileWalletData(contactsIds, new TelegramUtil.UserNftDataListener() {
            @Override
            public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                for (WalletInfo walletInfo : walletInfoList) {
                    //绑定了钱包的用户
                    if (walletInfo.is_bind_wallet == 1 && !CollectionUtils.isEmpty(walletInfo.getWallet_info())) {
                        String address = walletInfo.getWallet_info().get(0).getWallet_address();
                        walletAddressList.add(address);

                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(Long.parseLong(walletInfo.tg_user_id));
                        //自己规定的数据格式
                        Web3SocialCircleData web3SocialCircleData = new Web3SocialCircleData();
                        web3SocialCircleData.setIfContacts(true);
                        web3SocialCircleData.setTgUserData(user);
                        web3SocialCircleData.setAddress(address);
                        telegramWeb3SocialCircleList.add(web3SocialCircleData);
                    }
                }

                //请求信息
                getCyberConnectInfo();
            }

            @Override
            public void nftDataRequestError(String errorMsg) {
                Timber.e("根据tg用户ID获取钱包相关数据失败");
            }
        });
    }

    /**
     * 获取用户是否激活
     */
    private void getCyberConnectInfo() {
        if (telegramWeb3SocialCircleList.isEmpty()) {
            return;
        }
        unFollowList.clear();
        CyberConnectApi.getAddresses(walletAddressList, new CommonCallback<List<AddressWallet>>() {
            @Override
            public void onSuccess(List<AddressWallet> data) {
                if (telegramWeb3SocialCircleList.size() != data.size()) {
                    return;
                }

                //头部数据
                Web3SocialCircleNodeData titleData = new Web3SocialCircleNodeData();
                titleData.setItemType(Web3SocialCircleNodeData.TYPE_TITLE);
                titleData.setTitle(LocaleController.getString("contacts_web3_social_circle_list_title1", R.string.contacts_web3_social_circle_list_title1));
                mWeb3SocialCircleAdapter.addData(titleData);

                for (int i = 0; i < data.size(); i++) {
                    Web3SocialCircleData web3SocialCircleData = telegramWeb3SocialCircleList.get(i);
                    if (web3SocialCircleData.getAddress().equalsIgnoreCase(data.get(i).wallet.address)) {
                        AddressWallet.Node node = CollectionUtils.isEmpty(data.get(i).wallet.profiles.edges) ? null : data.get(i).wallet.profiles.edges.get(0).node;
                        web3SocialCircleData.setHandle(node == null ? "" : node.handle.replace(".cc", ""));
                        //是否激活
                        boolean activate = !CollectionUtils.isEmpty(data.get(i).wallet.profiles.edges);
                        if (activate) {
                            KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertUserActivation(web3SocialCircleData.getTgUserData().id);
                        }
                        web3SocialCircleData.setIfActivate(activate);

                        //是否关注
                        boolean follow = node != null && node.isFollowedByMe;
                        web3SocialCircleData.setIfPayAttentionTo(follow);
                        if (!follow) {//未关注数据
                            unFollowList.add(web3SocialCircleData);
                        }

                        //添加内容数据
                        Web3SocialCircleNodeData contentData = new Web3SocialCircleNodeData();
                        contentData.setItemType(Web3SocialCircleNodeData.TYPE_DATA);
                        contentData.setData(web3SocialCircleData);
                        mWeb3SocialCircleAdapter.addData(contentData);
                    }
                }

                requestChainFollowData();

                if (MMKVUtil.autoFollowStatus()) {
                    filterUser();
                }
            }

            @Override
            public void onError(String msg) {
                Timber.e("getCyberConnectInfo失败：" + msg);
            }
        });
    }

    /**
     * 请求链上的关注数据
     */
    private void requestChainFollowData() {
        List<String> addressList = new ArrayList<>();//可能会关注了同一个地址下的多个handel
        List<Web3SocialCircleData> requestWeb3List = new ArrayList<>();
        CyberConnectApi.getFollowingsByAddress(WalletDaoUtils.getCurrent().getAddress(), new CommonCallback<List<FollowingShip>>() {
            @Override
            public void onSuccess(List<FollowingShip> data) {
                super.onSuccess(data);
                if (CollectionUtils.isEmpty(data)) {
                    return;
                }

                for (FollowingShip followingShip : data) {
                    FollowingShip.Node node = followingShip.getNode();
                    if (node == null) {
                        continue;
                    }

                    FollowingShip.Node.Profile profile = node.getProfile();
                    if (profile == null) {
                        continue;
                    }

                    FollowingShip.Node.Profile.Owner owner = profile.getOwner();
                    if (owner == null) {
                        continue;
                    }

                    Web3SocialCircleData web3SocialCircleData = new Web3SocialCircleData();
                    web3SocialCircleData.setAddress(owner.getAddress());
                    web3SocialCircleData.setHandle(profile.getHandle());
                    web3SocialCircleData.setIfContacts(false);
                    web3SocialCircleData.setIfActivate(true);
                    web3SocialCircleData.setIfPayAttentionTo(true);
                    if (!addressList.contains(owner.getAddress())) {
                        addressList.add(owner.getAddress());
                        requestWeb3List.add(web3SocialCircleData);
                    }
                }

                List<Web3SocialCircleData> cacheWeb3Data = new ArrayList<>();
                for (Web3SocialCircleData requestWeb3Data : requestWeb3List) {
                    boolean has = false;
                    for (Web3SocialCircleData adapterItemData : telegramWeb3SocialCircleList) {
                        if (adapterItemData.getAddress().equalsIgnoreCase(requestWeb3Data.getAddress())) {
                            has = true;
                            break;
                        }
                    }

                    if (!has) {
                        cacheWeb3Data.add(requestWeb3Data);
                    }
                }

                if (!cacheWeb3Data.isEmpty()) {
                    //头部数据
                    Web3SocialCircleNodeData titleData = new Web3SocialCircleNodeData();
                    titleData.setItemType(Web3SocialCircleNodeData.TYPE_TITLE);
                    titleData.setTitle(LocaleController.getString("contacts_web3_social_circle_list_title2", R.string.contacts_web3_social_circle_list_title2));
                    mWeb3SocialCircleAdapter.addData(titleData);

                    for (Web3SocialCircleData circleData : cacheWeb3Data) {
                        //内容数据
                        Web3SocialCircleNodeData contentData = new Web3SocialCircleNodeData();
                        contentData.setItemType(Web3SocialCircleNodeData.TYPE_DATA);
                        contentData.setData(circleData);
                        mWeb3SocialCircleAdapter.addData(contentData);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
            }
        });
    }

    /**
     * 过滤出符合条件的用户
     */
    private void filterUser() {
        requestFollowList.clear();;
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new TransferHistoryApi())
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<TransferHistoryEntity>>>() {
                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<TransferHistoryEntity>> result) {
                        List<TransferHistoryEntity> historyEntityList = result.getData().getData();
                        if (CollectionUtils.isEmpty(historyEntityList)) {
                            return;
                        }

                        for (Web3SocialCircleData web3SocialCircleData : unFollowList) {
                            for (TransferHistoryEntity historyEntity : historyEntityList) {
                                long myId = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getClientUserId();
                                if (historyEntity.receipt_tg_user_id.equals(String.valueOf(myId))
                                        || historyEntity.payment_tg_user_id.equals(String.valueOf(web3SocialCircleData.getTgUserData().id))) {
                                    requestFollowList.add(web3SocialCircleData);
                                    break;
                                }
                            }
                        }

                        autoFollow();
                    }

                    @Override
                    public void onFail(Exception e) {}
                });
    }

    /**
     * 自动关注用户方法
     */
    private void autoFollow() {
        if (requestFollowPosition < requestFollowList.size()) {
            //未关注的用户数据
            Web3SocialCircleData web3SocialCircleData = requestFollowList.get(requestFollowPosition);
            TelegramUtil.followOrUnFollowBnbUser(web3SocialCircleData.getHandle(), false, new TelegramUtil.FollowSignatureResultListener() {
                @Override
                public void onStart() {}

                @Override
                public void requestSuccessful() {
                    //获取列表数据
                    List<Web3SocialCircleNodeData> adapterData = mWeb3SocialCircleAdapter.getData();

                    for (int i = 0; i < adapterData.size(); i++) {
                        if (adapterData.get(i).getItemType() == Web3SocialCircleNodeData.TYPE_DATA) {
                            if (web3SocialCircleData.getAddress().equals(adapterData.get(i).getData().getAddress())) {
                                //修改适配器这条数据样式
                                mWeb3SocialCircleAdapter.getData().get(i).getData().setIfPayAttentionTo(true);
                                mWeb3SocialCircleAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }

                    requestFollowPosition++;
                    autoFollow();
                }

                @Override
                public void requestError(String error) {}

                @Override
                public void onEnd() {}
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.TRANSFER_SUCCESSFUL:
                long id = (long) messageEvent.getData();
                List<Web3SocialCircleNodeData> adapterData = mWeb3SocialCircleAdapter.getData();
                for (int i = 0; i < adapterData.size(); i++) {
                    Web3SocialCircleNodeData data = adapterData.get(i);
                    if (data.getItemType() == Web3SocialCircleNodeData.TYPE_DATA) {
                        if (id == data.getData().getTgUserData().id
                                && data.getData().isIfActivate()
                                && data.getData().isIfContacts()) {
                            int finalI = i;
                            TelegramUtil.followOrUnFollowBnbUser(data.getData().getHandle(), false, new TelegramUtil.FollowSignatureResultListener() {
                                @Override
                                public void onStart() {}

                                @Override
                                public void requestSuccessful() {
                                    //修改适配器这条数据样式
                                    mWeb3SocialCircleAdapter.getData().get(finalI).getData().setIfPayAttentionTo(true);
                                    mWeb3SocialCircleAdapter.notifyItemChanged(finalI);
                                }

                                @Override
                                public void requestError(String error) {}

                                @Override
                                public void onEnd() {}
                            });
                            break;
                        }
                    }
                }
                break;
        }
    }
}