package teleblock.ui.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ViewTabCommunityBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.CameraScanActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.GroupCreateFinalActivity;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.database.KKVideoMessageDB;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.PayerGroupManager;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.CameraScanEntity;
import teleblock.model.CommunityFuntionEntity;
import teleblock.model.CommunityGroupData;
import teleblock.model.MintAssignmentList;
import teleblock.model.NoticeEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.model.wallet.TokenBalance;
import teleblock.network.BaseBean;
import teleblock.network.api.CommunityFuntionApi;
import teleblock.network.api.CommunityGroupApi;
import teleblock.network.api.MintAssignmentListApi;
import teleblock.network.api.NoticeMessagesApi;
import teleblock.ui.activity.MyWalletActivity;
import teleblock.ui.activity.NoticeCenterActivity;
import teleblock.ui.activity.TransferActivity;
import teleblock.ui.adapter.CommunityDappRvAdapter;
import teleblock.ui.adapter.CommunityGroupRvAdapter;
import teleblock.ui.adapter.MintAssignmentListAdapter;
import teleblock.ui.dialog.ReceiptQRCodeDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.ui.dialog.WalletUnbindDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import timber.log.Timber;

/**
 * Time：2023/1/5
 * Creator：LSD
 * Description：社区聚合Tab页
 */
public class CommunityTabView extends FrameLayout {
    private DialogsActivity mBaseFragment;
    private ViewTabCommunityBinding binding;
    //打开次数
    private int openCount = 0;

    //空投任务
    private MintAssignmentListAdapter mMintAssignmentListAdapter;
    //dapp
    private CommunityDappRvAdapter communityDappRvAdapter;
    //群组适配器
    private CommunityGroupRvAdapter communityGroupRvAdapter;
    private Drawable duduIcon = null;

    public CommunityTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        mBaseFragment = dialogsActivity;
        setOnClickListener(v -> {
        });

        initView();
        initData();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        binding = ViewTabCommunityBinding.inflate(LayoutInflater.from(mBaseFragment.getParentActivity()), this, true);
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight + SizeUtils.dp2px(4), 0, 0);
        binding.rlActionBar.getLayoutParams().height = ActionBar.getCurrentActionBarHeight() + SizeUtils.dp2px(8);

        binding.tvTabWallet.setText(LocaleController.getString("view_mywallet_tab_wallet", R.string.view_mywallet_tab_wallet));
        binding.tvTabTransfer.setText(LocaleController.getString("view_mywallet_tab_transtionfer", R.string.view_mywallet_tab_transtionfer));
        binding.tvTabCollection.setText(LocaleController.getString("view_mywallet_tab_collection", R.string.view_mywallet_tab_collection));
        binding.tvMintTitle.setText(LocaleController.getString("view_tab_community_mint_title", R.string.view_tab_community_mint_title));
        binding.tvHotTitle.setText(LocaleController.getString("view_tab_community_hot_title", R.string.view_tab_community_hot_title));
        binding.tvGroupTitle.setText(LocaleController.getString("view_tab_community_group_title", R.string.view_tab_community_group_title));
        binding.tvCreateWallet.setText(LocaleController.getString("view_community_create_wallet", R.string.view_community_create_wallet));
        binding.tvCreateWalletTips.setText(LocaleController.getString("view_community_create_wallet_tips", R.string.view_community_create_wallet_tips));

        binding.refreshLayout.setEnableRefresh(true).setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            requestData();
        });

        //空投列表数据
        binding.rvMintAssignment.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMintAssignment.setAdapter(mMintAssignmentListAdapter = new MintAssignmentListAdapter());

        //热门数据列表
        binding.rvHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvHot.setAdapter(communityDappRvAdapter = new CommunityDappRvAdapter());

        //群数据列表
        binding.rvGroup.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGroup.setAdapter(communityGroupRvAdapter = new CommunityGroupRvAdapter());

        setLoadingData();

        binding.flAvatar.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.头像点击, new HashMap<>());
            EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_DRAWER));
        });

        //切换钱包
        binding.tvWalletName.setOnClickListener(view -> {
            WalletUtil.getWalletInfo(wallet -> {
                EventUtil.track(getContext(), EventUtil.Even.切换钱包按钮点击, new HashMap<>());
                new WalletListDialog(mBaseFragment).show();
            });
        });

        binding.flWallet.setOnClickListener(view -> {
            if (WalletDaoUtils.getCurrent() == null) {
                EventUtil.track(getContext(), EventUtil.Even.新用户链接钱包按钮点击, new HashMap<>());
                //显示未绑定钱包弹窗
                new WalletUnbindDialog(ActivityUtils.getTopActivity(), false).show();
            }
        });

        binding.ivQrcode.setOnClickListener(view -> EventBus.getDefault().post(new MessageEvent(EventBusTags.SHOW_CAMERA_FUNCTION, CameraScanActivity.MY_WALLET_SCAN)));

        binding.tvTabWallet.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.钱包转账收款点击, new HashMap<>());
            WalletUtil.getWalletInfo(wallet -> {
                mBaseFragment.presentFragment(new MyWalletActivity());
            });
        });

        binding.tvTabTransfer.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.钱包转账收款点击, new HashMap<>());
            WalletUtil.getWalletInfo(wallet -> mBaseFragment.presentFragment(new TransferActivity()));
        });

        binding.tvTabCollection.setOnClickListener(view -> {
            WalletUtil.getWalletInfo(wallet -> {
                EventUtil.track(getContext(), EventUtil.Even.钱包转账收款点击, new HashMap<>());
                new ReceiptQRCodeDialog(mBaseFragment, wallet.getAddress()).show();
            });
        });

        //空投点击
        mMintAssignmentListAdapter.setOnItemClickListener((adapter, view, position) -> {
            MintAssignmentList clickData = mMintAssignmentListAdapter.getItem(position);
            if (!clickData.isIfLoadingData()) {
                TelegramUtil.alphaRoteIndex(mBaseFragment, clickData);
            }
        });

        //hot数据点击
        communityDappRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommunityFuntionEntity clickData = communityDappRvAdapter.getItem(position);
            if (!clickData.isIfLoadingData()) {
                MintAssignmentList itemData = new MintAssignmentList();//转换下通用
                itemData.setHome_link(clickData.getLink());
                TelegramUtil.alphaRoteIndex(mBaseFragment, itemData);
            }
        });

        binding.tvGroupTitle.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.币圈页_群创建点击, new HashMap<>());
            TelegramUtil.getBotInfo(MMKVUtil.getSystemMsg(), () -> {
                Bundle args = new Bundle();
                long[] array = new long[]{MMKVUtil.getSystemMsg().bot_id};
                args.putLongArray("result", array);
                args.putInt("chatType", ChatObject.CHAT_TYPE_CHAT);
                args.putBoolean("forImport", false);
                args.putBoolean("group_if_upload", true);
                mBaseFragment.presentFragment(new GroupCreateFinalActivity(args));
            });
        });

        //群组点击
        communityGroupRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            CommunityGroupData clickData = communityGroupRvAdapter.getItem(position);
            if (!clickData.isIfLoadingData()) {
                EventUtil.track(getContext(), EventUtil.Even.社群加入按钮点击, new HashMap<>());
                Map map = new HashMap();
                if ("private_group".equals(clickData.getType())) {
                    WalletUtil.getWalletInfo(wallet -> {
                        map.put("groupId", clickData.getPrivate_group().getId() + "");
                        PayerGroupManager.getInstance(UserConfig.selectedAccount).handleShopInfo(mBaseFragment, clickData.getPrivate_group());
                    });
                } else {
                    map.put("groupId", String.valueOf(clickData.getTg_group_id()));
                    String link = clickData.getTg_group_link();
                    if (!link.startsWith("https://t.me/")) {
                        link = "https://t.me/" + link;
                    }
                    Browser.openUrl(mBaseFragment.getContext(), link);
                }

                EventUtil.track(getContext(), EventUtil.Even.币圈页_群加入点击, map);
            }
        });

        // 通知中心
        binding.ivNotice.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.通知中心入口点击, new HashMap<>());
            mBaseFragment.presentFragment(new NoticeCenterActivity());
        });
    }

    public void initData() {
        if (mBaseFragment == null) {
            return;
        }

        if (openCount == 0) {
            requestData();
            openCount++;
        }

        setTgUserInfo();
    }

    private void requestData() {
        setLoadingData();
        EasyHttp.cancel(this.getClass().getSimpleName());
        setWalletInfo(WalletDaoUtils.getCurrent());
        requestMintList();
        requestHotData();
        loadGroupData();
        loadNoticeData();
    }

    private void loadNoticeData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new NoticeMessagesApi()
                        .setPage(1))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<NoticeEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<NoticeEntity>> result) {
                        List<NoticeEntity> list = result.getData().getData();
                        if (CollectionUtils.isNotEmpty(list)) {
                            MMKVUtil.lastNoticeId(list.get(0).id);
                        }
                        if (MMKVUtil.lastNoticeId() > MMKVUtil.readNoticeId()) {
                            binding.ivNotice.setImageResource(R.drawable.icon_notification_on_wallet);
                        } else {
                            binding.ivNotice.setImageResource(R.drawable.icon_notification_off_wallet);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                    }
                });
    }


    /**
     * 设置loading数据
     */
    private void setLoadingData() {
        List<MintAssignmentList> mintLoadingListData = new ArrayList<>();
        List<CommunityFuntionEntity> communityFuntionEntityList = new ArrayList<>();
        List<CommunityGroupData> communityGroupDataList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MintAssignmentList mintLoadingData = new MintAssignmentList();
            mintLoadingData.setIfLoadingData(true);
            mintLoadingListData.add(mintLoadingData);

            CommunityFuntionEntity mCommunityFuntionEntity = new CommunityFuntionEntity();
            mCommunityFuntionEntity.setIfLoadingData(true);
            communityFuntionEntityList.add(mCommunityFuntionEntity);

            CommunityGroupData mCommunityGroupData = new CommunityGroupData();
            mCommunityGroupData.setIfLoadingData(true);
            communityGroupDataList.add(mCommunityGroupData);
        }
        mMintAssignmentListAdapter.setList(mintLoadingListData);
        communityDappRvAdapter.setList(communityFuntionEntityList);
        communityGroupRvAdapter.setList(communityGroupDataList);
    }

    /**
     * 设置钱包信息
     */
    private void setWalletInfo(ETHWallet ethWalletData) {
        if (ethWalletData == null) { //未绑定钱包
            binding.tvFlag.setText("alpha Wallet");
            binding.tvFlag.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(R.drawable.icon_alpha_wallet_15));
            binding.ltAccountLoading1.setVisibility(GONE);
            binding.ltAccountLoading2.setVisibility(GONE);
            binding.tvCreateWallet.setVisibility(VISIBLE);
            binding.tvCreateWalletTips.setVisibility(VISIBLE);

            binding.tvWalletName.setVisibility(GONE);
            binding.tvWalletAccount.setVisibility(GONE);
            binding.tvDollerCoin.setVisibility(GONE);
            binding.ivAlphaIcon.setVisibility(GONE);
            binding.tvAlphaCoinNum.setVisibility(GONE);
            binding.tvAlphaCoin.setVisibility(GONE);
            return;
        }

        binding.tvWalletName.setVisibility(VISIBLE);
        binding.tvWalletAccount.setVisibility(VISIBLE);
        binding.tvDollerCoin.setVisibility(VISIBLE);
        binding.ivAlphaIcon.setVisibility(VISIBLE);
        binding.tvAlphaCoinNum.setVisibility(VISIBLE);
        binding.tvAlphaCoin.setVisibility(VISIBLE);
        binding.tvCreateWallet.setVisibility(GONE);
        binding.tvCreateWalletTips.setVisibility(GONE);

        //钱包名称
        BlockchainConfig.WalletIconType walletIconType = BlockchainConfig.getWalletTypeByTypeId(String.valueOf(ethWalletData.getWalletType()));
        if (walletIconType != null) {
            binding.tvWalletName.setText(String.format(LocaleController.getString("wallet_type_title", R.string.wallet_type_title), walletIconType.walletName));
            //品牌标志
            if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, ethWalletData.getWalletType())) {//链接的钱包
                binding.tvFlag.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(walletIconType.icon));
            } else if (ethWalletData.getWalletType() == ETHWallet.TYPE_TSS) {
                binding.tvFlag.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(R.drawable.icon_partical_wallet));
            }
            binding.tvFlag.setText(walletIconType.walletName);
        }

        //获取余额
        double[] totalAmount = {0.00};
        binding.tvWalletAccount.setTag(0);
        showAccountUstdLoading(true);
        List<Web3ConfigEntity.WalletNetworkConfigChainType> chainTypes = MMKVUtil.getWeb3ConfigData().getChainType();
        CollectionUtils.forAllDo(chainTypes, (index, chainType) -> BlockFactory.get(chainType.getId()).getTokenList(ethWalletData.getAddress(), new BlockCallback<List<TokenBalance>>() {
            @Override
            public void onSuccess(List<TokenBalance> data) {
                for (TokenBalance tokenBalance : data) {
                    totalAmount[0] += tokenBalance.balanceUSD;
                }
                binding.tvWalletAccount.setTag((int) binding.tvWalletAccount.getTag() + 1);
                Timber.i("onSuccess-->" + binding.tvWalletAccount.getTag());
            }

            @Override
            public void onError(String msg) {
            }

            @Override
            public void onEnd() {
                Timber.i("onEnd-->" + binding.tvWalletAccount.getTag());
                if ((int) binding.tvWalletAccount.getTag() == chainTypes.size()) {
                    binding.tvWalletAccount.setText("$" + WalletUtil.bigDecimalScale(new BigDecimal(String.valueOf(totalAmount[0])), 6));
                    showAccountUstdLoading(false);
                }
            }
        }));

        getDUDUCoinBalance(ethWalletData);
    }

    /**
     * 获取dudu余额
     *
     * @param ethWalletData
     */
    private void getDUDUCoinBalance(ETHWallet ethWalletData) {
        if (ethWalletData == null) {
            return;
        }

        binding.llAnim.setVisibility(GONE);
        //获取余额-alpha
        showAccountLoading(true);
        Web3ConfigEntity.SocialTokens socialTokens = MMKVUtil.getWeb3ConfigData().getSocialTokens();
        if (duduIcon == null) {
            GlideHelper.getDrawableGlide(getContext(), socialTokens.getIcon(), drawable -> {
                duduIcon = drawable;
            });
        }
        binding.tvAlphaCoin.setText(socialTokens.getSymbol());
        binding.tvAnimCoinType.setText(socialTokens.getSymbol());

        if (WalletUtil.isEvmAddress(ethWalletData.getAddress())) {
            String data = Web3AbiDataUtils.encodeBalanceOfData(ethWalletData.getAddress(), null);
            BlockFactory.get(socialTokens.getChain_id()).ethCall(socialTokens.getContract_address(), data, new BlockCallback<>() {
                @Override
                public void onSuccess(String data) {
                    super.onSuccess(data);
                    showAccountLoading(false);
                    String balance = Numeric.toBigInt(data).toString();
                    //上次存储的
                    BigDecimal lastDuduBalance = MMKVUtil.lastDuduBalance();
                    //存储dudu余额
                    BigDecimal duduBalance = WalletUtil.bigDecimalScale(new BigDecimal(WalletUtil.fromWei(balance, socialTokens.getDecimal())), 6);
                    binding.tvAlphaCoinNum.setText(duduBalance.toPlainString());

                    if (WalletUtil.decimalCompareTo(duduBalance, lastDuduBalance) && WalletUtil.decimalCompareTo(lastDuduBalance, BigDecimal.ZERO)) {
                        //展示增加dudu币种的特效动画，先往下移动，在改变透明度
                        BigDecimal balanceDifference = duduBalance.subtract(lastDuduBalance);
                        binding.tvAnimCoinNum.setText("+" + balanceDifference.toPlainString());
                        playAnimation();
                    }

                    //存储
                    MMKVUtil.lastDuduBalance(duduBalance.toPlainString());
                }

                @Override
                public void onError(String msg) {
                    super.onError(msg);
                    MMKVUtil.lastDuduBalance("");
                    binding.tvAlphaCoinNum.setText(BigDecimal.ZERO.toPlainString());
                    showAccountLoading(false);
                }
            });
        } else {
            MMKVUtil.lastDuduBalance("");
            binding.tvAlphaCoinNum.setText(BigDecimal.ZERO.toPlainString());
            showAccountLoading(false);
        }
    }

    private TranslateAnimation translateAnimation = null;
    private AlphaAnimation alphaAnimation = null;

    private void playAnimation() {
        binding.llAnim.setVisibility(VISIBLE);
        if (translateAnimation != null) {
            translateAnimation.cancel();
        }

        if (alphaAnimation != null) {
            alphaAnimation.cancel();
        }

        translateAnimation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, TranslateAnimation.RELATIVE_TO_SELF, TranslateAnimation.RELATIVE_TO_SELF, SizeUtils.dp2px(15));
        translateAnimation.setDuration(800);
        translateAnimation.setFillBefore(true);
        translateAnimation.setInterpolator(new AccelerateInterpolator());

        alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(1000);
        translateAnimation.setFillBefore(true);

        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.llAnim.setAnimation(alphaAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.llAnim.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        binding.llAnim.setAnimation(translateAnimation);
    }

    private void showAccountUstdLoading(boolean ifShow) {
        if (WalletDaoUtils.getCurrent() == null) {
            return;
        }

        if (ifShow) {
            binding.ltAccountLoading1.playAnimation();
        } else {
            binding.ltAccountLoading1.cancelAnimation();
        }

        binding.ltAccountLoading1.setVisibility(ifShow ? VISIBLE : GONE);
        binding.tvWalletAccount.setVisibility(ifShow ? INVISIBLE : VISIBLE);
        binding.tvDollerCoin.setVisibility(ifShow ? INVISIBLE : VISIBLE);
    }

    private void showAccountLoading(boolean ifShow) {
        if (WalletDaoUtils.getCurrent() == null) {
            return;
        }
        if (ifShow) {
            binding.ltAccountLoading2.playAnimation();
        } else {
            binding.ltAccountLoading2.cancelAnimation();
            if (duduIcon == null) {
                binding.ivAlphaIcon.setImageResource(R.drawable.icon_soical_token_15);
            } else {
                binding.ivAlphaIcon.setImageDrawable(duduIcon);
            }
        }
        binding.ltAccountLoading2.setVisibility(ifShow ? VISIBLE : GONE);
        binding.ivAlphaIcon.setVisibility(ifShow ? INVISIBLE : VISIBLE);
        binding.tvAlphaCoin.setVisibility(ifShow ? INVISIBLE : VISIBLE);
        binding.tvAlphaCoinNum.setVisibility(ifShow ? INVISIBLE : VISIBLE);
    }

    /**
     * 请求热门数据
     */
    private void requestHotData() {
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new CommunityFuntionApi())
                .request(new OnHttpListener<BaseBean<List<CommunityFuntionEntity>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<CommunityFuntionEntity>> result) {
                        List<CommunityFuntionEntity> data = result.getData();
                        if (!CollectionUtils.isEmpty(data)) {
                            binding.tvHotTitle.setVisibility(VISIBLE);
                            binding.hotContent.setVisibility(VISIBLE);
                            communityDappRvAdapter.setList(data);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                    }
                });
    }

    /**
     * 请求群组数据
     */
    private void loadGroupData() {
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new CommunityGroupApi())
                .request(new OnHttpListener<BaseBean<List<CommunityGroupData>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<CommunityGroupData>> result) {
                        List<CommunityGroupData> resultData = result.getData();

                        if (!CollectionUtils.isEmpty(resultData)) {
                            communityGroupRvAdapter.setList(resultData);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                    }

                    @Override
                    public void onEnd(Call call) {
                        binding.refreshLayout.finishRefresh();
                    }
                });
    }

    /**
     * 请求空投列表数据
     */
    private void requestMintList() {
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new MintAssignmentListApi())
                .request(new OnHttpListener<BaseBean<List<MintAssignmentList>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<MintAssignmentList>> result) {
                        if (CollectionUtils.isEmpty(result.getData())) {
                            return;
                        }

                        mMintAssignmentListAdapter.setList(result.getData());
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    /**
     * 设置tg用户信息
     */
    private void setTgUserInfo() {
        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
        binding.flAvatar.setUserInfo(user).loadView();
        binding.tvNickname.setText(UserObject.getUserName(user));
        binding.tvUserId.setText("@" + user.username);
        //获取激活状态
        boolean activationStatus = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).getUserProfileActivationStatus(user.id);
        binding.ivHaveCcprofile.setVisibility(activationStatus ? VISIBLE : GONE);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if (visibility == VISIBLE) {
            EventUtil.track(getContext(), EventUtil.Even.首页tab展示, new HashMap<>());
        }

        if (visibility == VISIBLE && openCount > 0) {
            ThreadUtils.runOnUiThreadDelayed(() -> getDUDUCoinBalance(WalletDaoUtils.getCurrent()), 1000);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.CAMERA_SCAN_RESULT:
                CameraScanEntity scanEntity = (CameraScanEntity) messageEvent.getData();
                if (scanEntity.type == CameraScanActivity.MY_WALLET_SCAN) {
                    String address = scanEntity.data;
                    String toAddress;
                    //判断是不是小狐狸钱包的二维码，如果是则按照这个进行截取
                    if (!address.isEmpty() && address.length() >= 52 && address.contains("0x") && address.toLowerCase().startsWith("ethereum:")) {
                        toAddress = address.substring(address.indexOf(":") + 1, address.indexOf(":") + 43);
                    } else {
                        toAddress = address;
                    }
                    mBaseFragment.presentFragment(new TransferActivity(toAddress));
                }
                break;
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                setWalletInfo(WalletDaoUtils.getCurrent());
                break;
            case EventBusTags.NOTICE_HAS_READ:
                if (MMKVUtil.lastNoticeId() > MMKVUtil.readNoticeId()) {
                    binding.ivNotice.setImageResource(R.drawable.icon_notification_on_wallet);
                } else {
                    binding.ivNotice.setImageResource(R.drawable.icon_notification_off_wallet);
                }
                break;
        }
    }
}