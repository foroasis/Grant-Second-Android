package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.CurrencyChildHeadviewBinding;
import org.telegram.messenger.databinding.FragmentCurrencyCircleBinding;
import org.telegram.messenger.databinding.RvCurrencyEmptyViewBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.GroupCreateFinalActivity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import teleblock.blockchain.BlockchainConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.PayerGroupManager;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.PrivateGroupEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.PrivateGroupApi;
import teleblock.ui.adapter.CurrencyOperaBtnAdp;
import teleblock.ui.adapter.PrivateGroupRvAdapter;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.ui.dialog.WalletUnbindDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * Time:2022/10/11
 * Author:Perry
 * Description：币圈社群页面
 */
public class CurrencyCircleActivity extends BaseFragment {

    private FragmentCurrencyCircleBinding binding;

    private Web3ConfigEntity.WalletNetworkConfigChainType chainData;//链数据
    private Web3ConfigEntity.WalletNetworkConfigEntityItem mainCoinData;//主币数据

    //私有群组列表
    private PrivateGroupRvAdapter privateGroupRvAdapter;
    //头部布局
    private CurrencyChildHeadviewBinding headviewBinding;
    //空布局view
    private RvCurrencyEmptyViewBinding emptyViewBinding;

    //工具按钮去
    private CurrencyOperaBtnAdp mCurrencyOperaBtnAdp;

    private LoadingDialog loadingDialog;

    //request
    private int page = 1;

    public CurrencyCircleActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context) {
        chainData = (Web3ConfigEntity.WalletNetworkConfigChainType) getArguments().getSerializable("chainData");
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(chainData.getName());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = FragmentCurrencyCircleBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        //初始化加载框
        loadingDialog = new LoadingDialog(getContext(), LocaleController.getString("Loading", R.string.Loading));

        for (Web3ConfigEntity.WalletNetworkConfigEntityItem coinItemData : chainData.getCurrency()) {
            if (coinItemData.isIs_main_currency()) {
                mainCoinData = coinItemData;
            }
        }

        binding.rv.setLayoutManager(new LinearLayoutManager(getParentActivity()));

        //初始化适配器
        privateGroupRvAdapter = new PrivateGroupRvAdapter();
        binding.rv.setAdapter(privateGroupRvAdapter);

        //空状态view
        emptyViewBinding = RvCurrencyEmptyViewBinding.inflate(LayoutInflater.from(getParentActivity()));
        emptyViewBinding.tvEmptyTitle.setText(LocaleController.getString("currency_fragment_empty_data_title", R.string.currency_fragment_empty_data_title));
        emptyViewBinding.tvEmptyTips.setText(LocaleController.getString("currency_fragment_empty_data_tips", R.string.currency_fragment_empty_data_tips));

        //添加头部view
        headviewBinding = CurrencyChildHeadviewBinding.inflate(LayoutInflater.from(getParentActivity()));
        privateGroupRvAdapter.addHeaderView(headviewBinding.getRoot());
        initHeadView();

        //下拉刷新
        binding.refreshLayout.autoRefresh();
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadData(true);
        });

        //上拉加载更多
        privateGroupRvAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            loadData(false);
        });

        //加入群组
        privateGroupRvAdapter.setOnItemClickListener((adapter, view, position) -> {
            WalletUtil.getWalletInfo(wallet -> {
                PrivateGroupEntity privateGroup = (PrivateGroupEntity) adapter.getItem(position);
                Map map = new HashMap();
                map.put("groupId", privateGroup.getId() + "");
                EventUtil.track(getContext(), EventUtil.Even.币圈页_群加入点击, map);
                PayerGroupManager.getInstance(getCurrentAccount()).handleShopInfo(this, privateGroup);
            });
        });
    }

    private void initHeadView() {
        headviewBinding.tvWalletUnbind.setText(LocaleController.getString("currency_fragment_wallet_unbind", R.string.currency_fragment_wallet_unbind));
        headviewBinding.tvChainName.setText(chainData.getName());//链名
        headviewBinding.tvCoinName.setText(chainData.getMain_currency_name());//主币名称

        setWalletInfo();

        //工具区按钮
        mCurrencyOperaBtnAdp = new CurrencyOperaBtnAdp();
        headviewBinding.rvOpera.setLayoutManager(new GridLayoutManager(getParentActivity(), 4));
        headviewBinding.rvOpera.setAdapter(mCurrencyOperaBtnAdp);
        mCurrencyOperaBtnAdp.setOnItemClickListener((adapter, view, position) -> {
            Web3ConfigEntity.WalletNetworkConfigChainTypeBtn btn = mCurrencyOperaBtnAdp.getItem(position);
            if (TextUtils.isEmpty(btn.getLink())) {
                ToastUtils.showShort(LocaleController.getString("btn_null_function_tips", R.string.btn_null_function_tips));
                return;
            }
            Browser.openUrl(getContext(), btn.getLink());
        });
        if (!CollectionUtils.isEmpty(chainData.getButton())) {
            mCurrencyOperaBtnAdp.setList(chainData.getButton());
        }

        headviewBinding.tvWalletUnbind.setOnClickListener(v -> new WalletUnbindDialog(getParentActivity()).show());

        //复制钱包地址
        headviewBinding.llWalletAddress.setOnClickListener(view -> {
            ClipboardUtils.copyText(WalletDaoUtils.getCurrent().getAddress());
            BulletinFactory.of(this).createCopyBulletin(LocaleController.getString("wallet_home_copy_address", R.string.wallet_home_copy_address), getResourceProvider()).show();
        });

        //跳转到新建群组页面
        headviewBinding.tvNewgroup.setOnClickListener(view -> {
            EventUtil.track(getContext(), EventUtil.Even.币圈页_群创建点击, new HashMap<>());
            loadingDialog.show();//显示加载框
            TelegramUtil.getBotInfo(MMKVUtil.getSystemMsg(), () -> {
                loadingDialog.dismiss();

                Bundle args = new Bundle();
                long[] array = new long[]{MMKVUtil.getSystemMsg().bot_id};
                args.putLongArray("result", array);
                args.putInt("chatType", ChatObject.CHAT_TYPE_CHAT);
                args.putBoolean("forImport", false);
                args.putBoolean("group_if_upload", true);
                args.putLong("chain_id", chainData.getId());
                args.putString("chain_name", chainData.getName());
                presentFragment(new GroupCreateFinalActivity(args));
            });
        });
    }

    /**
     * 设置钱包信息
     */
    private void setWalletInfo() {
        //钱包地址
        ETHWallet wallet = WalletDaoUtils.getCurrent();
        if (wallet == null) {//未绑定钱包
            headviewBinding.llWalletAddress.setVisibility(View.GONE);
            headviewBinding.tvWalletUnbind.setVisibility(View.VISIBLE);
        } else {
            headviewBinding.llWalletAddress.setVisibility(View.VISIBLE);
            headviewBinding.tvWalletUnbind.setVisibility(View.GONE);
            //显示钱包图标 和 钱包地址
            headviewBinding.ivWalletLogo.setImageDrawable(ContextCompat.getDrawable(getParentActivity(), BlockchainConfig.getWalletIconByPkg(wallet.getConnectedWalletPkg())));
            headviewBinding.tvWalletAddress.setText(WalletUtil.formatAddress(wallet.getAddress()));
        }
    }

    /**
     * 获取数据
     *
     * @param ifRefresh
     */
    private void loadData(boolean ifRefresh) {
        if (privateGroupRvAdapter == null) {
            return;
        }
        privateGroupRvAdapter.removeHeaderView(emptyViewBinding.getRoot());

        if (ifRefresh) {
            page = 1;
            getCoinPrice();
        } else {
            page++;
        }

        PrivateGroupApi privateGroupApi = new PrivateGroupApi(page, 20).setChain_id(chainData.getId());
        EasyHttp.post(new ApplicationLifecycle()).api(privateGroupApi)
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<PrivateGroupEntity>>>() {
                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<PrivateGroupEntity>> result) {
                        List<PrivateGroupEntity> resultData = result.getData().getData();
                        if (CollectionUtils.isEmpty(resultData) && ifRefresh) {
                            privateGroupRvAdapter.addHeaderView(emptyViewBinding.getRoot());
                        }

                        if (CollectionUtils.isEmpty(resultData) || resultData.size() < 20) {
                            privateGroupRvAdapter.getLoadMoreModule().loadMoreEnd(true);
                        }

                        if (ifRefresh) {
                            privateGroupRvAdapter.setList(resultData);
                        } else {
                            privateGroupRvAdapter.addData(resultData);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (ifRefresh) {
                            privateGroupRvAdapter.addHeaderView(emptyViewBinding.getRoot());
                        }
                    }

                    @Override
                    public void onEnd(Call call) {
                        binding.refreshLayout.finishRefresh();
                        privateGroupRvAdapter.getLoadMoreModule().loadMoreComplete();
                    }
                });
    }

    /**
     * 获取币种单价
     */
    private void getCoinPrice() {
        if (mainCoinData != null) {//获取币种单价
            WalletUtil.requestMainCoinPrice(mainCoinData.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
                @Override
                public void requestEnd() {
                }

                @Override
                public void requestError(String msg) {
                    headviewBinding.tvPrice.setText("$0");
                    headviewBinding.tvAmplitude.setText("0%");
                }

                @Override
                public void requestSuccessful(CurrencyPriceEntity resultData) {
                    //单价
                    headviewBinding.tvPrice.setText("$" + resultData.getUsd());

                    //涨跌幅度
                    String change = WalletUtil.bigDecimalScale(new BigDecimal(String.valueOf(resultData.getUsd_24h_change())), 2).toPlainString();
                    headviewBinding.tvAmplitude.setText(change + "%");
                    if (change.startsWith("-")) {
                        headviewBinding.tvAmplitude.setTextColor(Color.parseColor("#FFF06464"));
                        headviewBinding.ivAmplitude.setImageResource(R.mipmap.coin_change_decrease);
                    } else {
                        headviewBinding.tvAmplitude.setTextColor(Color.parseColor("#FF32C481"));
                        headviewBinding.ivAmplitude.setImageResource(R.mipmap.coin_change_increase);
                    }
                }

            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.CRATE_NEW_GROUP:
            case EventBusTags.EDIT_GC_DETAILS_SUCCESSFUL:
                long chainId = (long) event.getData();
                if (chainId != 0L && chainId == chainData.getId()) {
                    loadData(true);
                }
                break;

            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                setWalletInfo();
                break;
        }
    }

}
