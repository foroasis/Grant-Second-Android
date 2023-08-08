package teleblock.ui.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.particle.base.ParticleNetwork;
import com.particle.base.data.WebOutput;
import com.particle.base.data.WebServiceCallback;
import com.particle.base.data.WebServiceError;
import com.particle.network.ParticleNetworkAuth;
import com.particle.network.service.LoginType;
import com.particle.network.service.SupportAuthType;
import com.particle.network.service.model.LoginOutput;
import com.particle.network.service.model.UserInfo;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewMywalletBinding;
import org.telegram.ui.CameraScanActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.bnb.bean.AddressWallet;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.LoginManager;
import teleblock.model.CameraScanEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.api.blockchain.bnb.CyberConnectApi;
import teleblock.ui.adapter.TgFragmentVp2Adapter;
import teleblock.ui.dialog.ReceiptQRCodeDialog;
import teleblock.ui.dialog.SelectorChainTypeDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.ui.fragment.BaseFragment;
import teleblock.ui.fragment.MyNftsRecordFragment;
import teleblock.ui.fragment.MyTokensFragment;
import teleblock.ui.fragment.MyTransferRecordFragment;
import teleblock.util.MMKVUtil;
import teleblock.util.ViewUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.CommonCallback;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/11/30
 * Author:Perry
 * Description：我的钱包view
 */
public class MyWalletActivity extends org.telegram.ui.ActionBar.BaseFragment {

    public ViewMywalletBinding binding;
    //余额加载view
    private LottieAnimationView balanceLoadingIv;

    //选择链对话框
//    private SelectorChainTypeDialog mSelectorChainTypeDialog;
    //选择的链数据
//    private Web3ConfigEntity.WalletNetworkConfigChainType selectorChainTypeData;

    //收款二维码对话框
    private ReceiptQRCodeDialog mReceiptQRCodeDialog;

    private List<String> tabNameList = new ArrayList<>();
    //存储子页面的集合
    private List<BaseFragment> pageFragmentView = new ArrayList<>();
    //代币列表页面
    private MyTokensFragment myTokensFragment;
    //nfts列表页面
    private MyNftsRecordFragment myNftsRecordFragment;
    //交易记录页面
    private MyTransferRecordFragment myTransferRecordFragment;

    //vp2适配器
    private TgFragmentVp2Adapter mTgFragmentVp2Adapter;

    //当前钱包地址数据
    private ETHWallet ethWalletData;

    private boolean tipSecurity = true;

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        EventBus.getDefault().unregister(this);
        super.onFragmentDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ViewMywalletBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.rootView.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        binding.tvTabTransfer.setText(LocaleController.getString("view_mywallet_tab_transtionfer", R.string.view_mywallet_tab_transtionfer));
        binding.tvTabCollection.setText(LocaleController.getString("view_mywallet_tab_collection", R.string.view_mywallet_tab_collection));
        binding.tvTabSwap.setText(LocaleController.getString("view_mywallet_tab_exchange", R.string.view_mywallet_tab_exchange));
        binding.tvTabScancode.setText(LocaleController.getString("view_mywallet_tab_scancode", R.string.view_mywallet_tab_scancode));
        binding.tvBorderTips.setText(LocaleController.getString("view_mywallet_pintips", R.string.view_mywallet_pintips));

        balanceLoadingIv = new LottieAnimationView(getContext());
        balanceLoadingIv.setAnimation(R.raw.animation_spaceholder_loading);
        balanceLoadingIv.setRepeatCount(ValueAnimator.INFINITE);
        balanceLoadingIv.playAnimation();
        binding.flBalanceLoading.addView(balanceLoadingIv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        Drawable chainRightIcon = ResourceUtils.getDrawable(R.drawable.arrow_white_up_icon);
        chainRightIcon.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
        binding.tvChaintype.getHelper().setIconNormalRight(chainRightIcon);

        binding.ivBack.setOnClickListener(v -> finishFragment());

        //头像
        binding.avHeader.setUserInfo(getUserConfig().getCurrentUser()).loadView();

        //复制钱包地址
        binding.tvWalletAddress.setOnClickListener(v -> {
            ClipboardUtils.copyText(ethWalletData.getAddress());
            BulletinFactory.of(binding.getRoot(), getResourceProvider()).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address)).show();
        });

        binding.ivWalletPwd.setOnClickListener(v -> {
            checkAccountAndSecurity();
        });

        //解决下拉刷新和coodring冲突
        binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            binding.refreshLayout.setEnableRefresh(verticalOffset >= 0);
        });

        //下拉刷新
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshData(MMKVUtil.currentChainConfig(binding.vpPage.getCurrentItem() == 0));
        });

        //钱包列表
        binding.tvTitle.setOnClickListener(v -> {
            new WalletListDialog(this) {
                @Override
                public void onItemClick(ETHWallet wallet) {
                    ethWalletData = wallet;
                    setWallet(ethWalletData);
                }
            }.show();
        });

        //跳转转账页面
        binding.tvTabTransfer.setOnClickListener(v -> {
            WalletUtil.getWalletInfo(wallet -> {
                presentFragment(new TransferActivity());
            });
        });

        //扫码二维码
        binding.tvTabScancode.setOnClickListener(v -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.SHOW_CAMERA_FUNCTION, CameraScanActivity.MY_WALLET_SCAN));
        });

        //选择链
        binding.tvChaintype.setOnClickListener(v -> {
            new SelectorChainTypeDialog(binding.tvChaintype.getContext(), binding.vpPage.getCurrentItem(), this::refreshData).show();
        });

        //弹出收款二维码
        binding.tvTabCollection.setOnClickListener(v -> {
            mReceiptQRCodeDialog = new ReceiptQRCodeDialog(this, ethWalletData.getAddress());
            mReceiptQRCodeDialog.show();
        });
    }

    private void addFragmentToVp() {
        FragmentTransaction ft = getParentActivity().getSupportFragmentManager().beginTransaction();
        if (pageFragmentView.size() > 0) {
            for (Fragment fragment : pageFragmentView) {
                ft.remove(fragment);
            }

            ft.commitNow();
            pageFragmentView.clear();
            mTgFragmentVp2Adapter.notifyDataSetChanged();
        }

        //我的代币列表页面
        myTokensFragment = MyTokensFragment.instance();
        myTokensFragment.setParentFragment(this);
        pageFragmentView.add(myTokensFragment);

        //我的nft记录页面
        myNftsRecordFragment = MyNftsRecordFragment.instance();
        myNftsRecordFragment.setParentFragment(this);
        pageFragmentView.add(myNftsRecordFragment);

        //交易记录
        myTransferRecordFragment = MyTransferRecordFragment.instance();
        myTransferRecordFragment.setParentFragment(this);
        pageFragmentView.add(myTransferRecordFragment);

        //初始化适配器
        mTgFragmentVp2Adapter = new TgFragmentVp2Adapter(getParentActivity(), pageFragmentView);
        binding.vpPage.setAdapter(mTgFragmentVp2Adapter);
        binding.vpPage.setUserInputEnabled(false);

        //tab适配器
        binding.mib.setNavigator(ViewUtil.mibSetNavigat(getParentActivity(), textMibAdapter(tabNameList, binding.vpPage)));
        ViewUtil.vbBindMiTabListener(binding.mib, binding.vpPage, position -> {
            setChainTypeUi(MMKVUtil.currentChainConfig(binding.vpPage.getCurrentItem() == 0));
        });
    }

    public void initData() {
        //添加tab拦数据
        tabNameList.clear();
        tabNameList.add(LocaleController.getString("view_mywallet_tab_tokens", R.string.view_mywallet_tab_tokens));
        tabNameList.add(LocaleController.getString("view_mywallet_tab_nft", R.string.view_mywallet_tab_nft));
        tabNameList.add(LocaleController.getString("view_mywallet_tab_transactions", R.string.view_mywallet_tab_transactions));
        //设置链数据
        setChainTypeUi(MMKVUtil.currentChainConfig(binding.vpPage.getCurrentItem() == 0));
        //获取钱包对象
        WalletUtil.getWalletInfo(wallet -> {
            ethWalletData = wallet;
            setWallet(wallet);
        });
    }

    /**
     * 数据刷新
     *
     * @param data
     */
    private void refreshData(Web3ConfigEntity.WalletNetworkConfigChainType data) {
        setChainTypeUi(data);
        //设置chainid
        myTransferRecordFragment.setChainData(data);
        myNftsRecordFragment.setChainData(data);
        myTokensFragment.setChainData(MMKVUtil.currentChainConfig(true));
    }

    /**
     * 设置钱包相关信息
     *
     * @param ethWalletData
     */
    private void setWallet(ETHWallet ethWalletData) {
        this.ethWalletData = ethWalletData;

        setWalletBaseInfo(ethWalletData);

        addFragmentToVp();

        List<String> walletAddressList = new ArrayList<>();
        walletAddressList.add(ethWalletData.getAddress());
        CyberConnectApi.getAddresses(walletAddressList, new CommonCallback<List<AddressWallet>>() {
            @Override
            public void onSuccess(List<AddressWallet> data) {
                for (int i = 0; i < data.size(); i++) {
                    AddressWallet.Node node = CollectionUtils.isEmpty(data.get(i).wallet.profiles.edges) ? null : data.get(i).wallet.profiles.edges.get(0).node;
                    MMKVUtil.ccprofileHandler(node == null ? "" : node.handle.replace(".cc", ""));
                }
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_CCPROFILE_ID));
            }

            @Override
            public void onError(String msg) {
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_CCPROFILE_ID));
            }
        });
    }

    /**
     * 显示钱包地址和名称
     *
     * @param ethWallet
     */
    private void setWalletBaseInfo(ETHWallet ethWallet) {
        //钱包地址
        binding.tvWalletAddress.setText(WalletUtil.formatAddress(ethWallet.getAddress()));
        binding.tvTitle.setText(String.format(LocaleController.getString("wallet_type_title", R.string.wallet_type_title),
                ethWallet.getAddress().toLowerCase().startsWith("0x") ? "ETH" : "Solana"));

        //品牌标志
        binding.tvFlag.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(BlockchainConfig.getWalletIconByPkg(ethWallet.getConnectedWalletPkg())));
        String showText = "";
        if (ArrayUtils.contains(ETHWallet.TYPE_CONNECT, ethWallet.getWalletType())) {//链接的钱包
            showText = BlockchainConfig.getWalletNameByPkg(ethWallet.getConnectedWalletPkg());
        } else if (ethWallet.getWalletType() == ETHWallet.TYPE_TSS) {
            showText = "Particle Network";
            binding.tvFlag.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(R.drawable.icon_partical_wallet));
        }
        binding.tvFlag.setText(showText);
    }

    /**
     * 设置选择的链数据
     *
     * @param data
     */
    private void setChainTypeUi(Web3ConfigEntity.WalletNetworkConfigChainType data) {
        if (data == null) {
            binding.tvChaintype.setText(LocaleController.getString("dialog_selector_chaintype_all_chain", R.string.dialog_selector_chaintype_all_chain));
            binding.tvChaintype.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(R.drawable.icon_all_network_dialog_wallet));
        } else {
            //链的名称
            binding.tvChaintype.setText(data.getName());
            //获取显示链图标
            GlideHelper.getDrawableGlide(binding.getRoot().getContext(), data.getIcon(), drawable -> binding.tvChaintype.getHelper().setIconNormalLeft(drawable));
        }
    }

    public void stopBalanceLoading() {
        binding.flBalanceLoading.setVisibility(View.GONE);
        binding.refreshLayout.finishRefresh();
    }

    /**
     * 普通文字样式的adapter
     *
     * @param tabs
     * @param viewPager2
     * @return
     */
    public CommonNavigatorAdapter textMibAdapter(List<String> tabs, ViewPager2 viewPager2) {
        return new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ColorTransitionPagerTitleView titleView = new ColorTransitionPagerTitleView(context);
                titleView.setNormalColor(Color.parseColor("#56565C"));
                titleView.setSelectedColor(Color.parseColor("#3954D5"));
                titleView.setText(tabs.get(index));
                titleView.setTextSize(15f);
                titleView.setTypeface(Typeface.DEFAULT_BOLD);
                titleView.setOnClickListener(v -> viewPager2.setCurrentItem(index, false));
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setColors(Color.parseColor("#3954D5"));
                linePagerIndicator.setRoundRadius(5);
                return linePagerIndicator;
            }
        };
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
                    presentFragment(new TransferActivity(toAddress));
                }
                break;
            case EventBusTags.WALLET_CREATED:
                if (WalletDaoUtils.getCurrent() != null) {
                    setWallet(WalletDaoUtils.getCurrent());
                }
                break;
        }
    }

    //检查账号是否正常
    private void checkAccountAndSecurity() {
        if (!ethWalletData.getAddress().equalsIgnoreCase(ParticleNetworkAuth.getAddress(ParticleNetwork.INSTANCE))) {
            ParticleNetworkAuth.login(ParticleNetwork.INSTANCE, LoginType.JWT, LoginManager.getUserToken(), SupportAuthType.ALL.getValue(), false, null, new WebServiceCallback<LoginOutput>() {
                @Override
                public void success(@NonNull LoginOutput loginOutput) {
                    AndroidUtilities.runOnUIThread(() -> openAccountAndSecurity(), 300);
                }

                @Override
                public void failure(@NonNull WebServiceError webServiceError) {
                    ToastUtils.showLong(webServiceError.getMessage());
                }
            },null);
            return;
        }
        openAccountAndSecurity();
    }


    /**
     * 跳转到particle页面
     */
    private void openAccountAndSecurity() {
        ParticleNetworkAuth.openAccountAndSecurity(ParticleNetwork.INSTANCE, new WebServiceCallback<>() {
            @Override
            public void success(@NonNull WebOutput webOutput) {
            }

            @Override
            public void failure(@NonNull WebServiceError webServiceError) {
            }
        });
    }

}