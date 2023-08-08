package teleblock.ui.view;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.ConvertUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewHomeWalletTopBinding;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.DialogsActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.HomeTopItemEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.activity.MyWalletActivity;
import teleblock.ui.activity.TransferActivity;
import teleblock.ui.adapter.HomeTopWalletRvAdapter;
import teleblock.ui.dialog.SelectorChainTypeDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/4/19
 * 描述：首页钱包顶部
 */
public class HomeTopWalletView extends FrameLayout {

    private DialogsActivity dialogsActivity;
    public ViewHomeWalletTopBinding binding;
    private SelectorChainTypeDialog mSelectorChainTypeDialog;

    private ETHWallet ethWalletData;
    private Web3ConfigEntity.WalletNetworkConfigChainType selectorChainTypeData;
    private HomeTopWalletRvAdapter homeTopWalletRvAdapter;

    //余额加载view
    private LottieAnimationView balanceLoadingIv;

    public HomeTopWalletView(@NonNull DialogsActivity fragment) {
        super(fragment.getParentActivity());
        this.dialogsActivity = fragment;
        initView();
        initRvData();
        updateStyle();
        setOnClickListener(v -> {
        });
    }

    private void initView() {
        EventBus.getDefault().register(this);
        binding = ViewHomeWalletTopBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.tvHomeSearch.setText(LocaleController.getString("ac_home_wallet_search", R.string.ac_home_wallet_search));

        balanceLoadingIv = new LottieAnimationView(getContext());
        balanceLoadingIv.setAnimation(R.raw.animation_spaceholder_loading);
        balanceLoadingIv.setRepeatCount(ValueAnimator.INFINITE);
        balanceLoadingIv.playAnimation();
        binding.flBalanceLoading.addView(balanceLoadingIv, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        binding.llHomeSearch.setOnClickListener(v -> {
            dialogsActivity.searchItem.performClick();//搜索
        });

        //选择钱包
        binding.tvHomeWalletAddress.setOnClickListener(v -> {
            //ClipboardUtils.copyText(ethWalletData.getAddress());
            //BulletinFactory.of(dialogsActivity).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address)).show();

            new WalletListDialog(dialogsActivity) {
                @Override
                public void onItemClick(ETHWallet wallet) {
                    setWallet(wallet);
                }
            }.show();
        });

        //选择钱包
        binding.ivHomeWalletDrop.setOnClickListener(v -> new WalletListDialog(dialogsActivity) {
            @Override
            public void onItemClick(ETHWallet wallet) {
                setWallet(wallet);
            }
        }.show());

        //初始化链数据
        binding.tvHomeChain.setOnClickListener(v -> {
//            if (mSelectorChainTypeDialog == null) {
//                mSelectorChainTypeDialog = new SelectorChainTypeDialog(getContext(), data -> {
//                    setChainUi(data);
//
//                    //获取这条链下面的余额
//                    getAccountBalance(data.getId());
//                });
//            }
//            mSelectorChainTypeDialog.showDialog(selectorChainTypeData);
        });

        //rv
        binding.homeWalletRv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        binding.homeWalletRv.setAdapter(homeTopWalletRvAdapter = new HomeTopWalletRvAdapter());
        binding.homeWalletRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = ConvertUtils.dp2px(10);
            }
        });

        homeTopWalletRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            HomeTopItemEntity item = homeTopWalletRvAdapter.getItem(position);
            if (1 == item.id) {
                dialogsActivity.presentFragment(new MyWalletActivity());
            } else if (2 == item.id) {
                WalletUtil.getWalletInfo(wallet -> {
                    dialogsActivity.presentFragment(new TransferActivity());
                });
            }
        });
    }

    public void initData() {
        //设置链数据
        setChainUi(MMKVUtil.currentChainConfig());

        //获取钱包对象
        WalletUtil.getWalletInfo(this::setWallet);
    }

    /***
     * 设置链数据
     * @param data
     */
    private void setChainUi(Web3ConfigEntity.WalletNetworkConfigChainType data) {
        selectorChainTypeData = data;
        MMKVUtil.currentChainConfig(data);

        //链的名称
        binding.tvHomeChain.setText(data.getName());
        //获取显示链图标
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), data.getIcon(), drawable -> binding.tvHomeChain.getHelper().setIconNormalLeft(drawable));
    }

    /**
     * 设置钱包相关信息
     *
     * @param ethWalletData
     */
    private void setWallet(ETHWallet ethWalletData) {
        this.ethWalletData = ethWalletData;

        //钱包地址
        binding.tvHomeWalletAddress.setText(WalletUtil.formatAddress(ethWalletData.getAddress()));

        //获取这条链下面的余额
        getAccountBalance(selectorChainTypeData.getId());
    }

    /***
     * 获取余额
     * @param chainId
     */
    private void getAccountBalance(long chainId) {
        WalletUtil.requestChainAllBalance(chainId, ethWalletData.getAddress(), new WalletUtil.RequestChainTotalBalance() {
            @Override
            public void requestStart() {
            }

            @Override
            public void requestError(String msg) {
                stopBalanceLoading();
                binding.tvHomeWalletAccount.setText("$" + new BigDecimal("0").toPlainString());
            }

            @Override
            public void requestSuccessful(String balanceStr, BigDecimal balance) {
                stopBalanceLoading();
                binding.tvHomeWalletAccount.setText(balanceStr);
            }
        });
    }

    private void stopBalanceLoading() {
        balanceLoadingIv.pauseAnimation();
        binding.flBalanceLoading.setVisibility(GONE);
    }


    private void initRvData() {
        List<HomeTopItemEntity> list = new ArrayList<>();
        HomeTopItemEntity item = new HomeTopItemEntity();
        item.id = 1;
        item.icon = R.drawable.ic_home_wallet_one;
        item.name = "钱包";
        list.add(item);

        item = new HomeTopItemEntity();
        item.id = 2;
        item.icon = R.drawable.ic_home_wallet_transfer;
        item.name = "转账";
        list.add(item);

        item = new HomeTopItemEntity();
        item.id = 5;
        item.icon = R.drawable.ic_home_wallet_red;
        item.name = "红包";
        list.add(item);

        item = new HomeTopItemEntity();
        item.id = 7;
        item.icon = R.drawable.ic_home_wallet_browser;
        item.name = "浏览器";
        list.add(item);

        homeTopWalletRvAdapter.setList(list);
    }

    public void updateStyle() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                initData();
                break;
        }
    }
}