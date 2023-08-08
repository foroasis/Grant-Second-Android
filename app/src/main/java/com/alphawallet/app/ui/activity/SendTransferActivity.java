package teleblock.ui.activity;

import static teleblock.widget.TelegramUserAvatar.DEFAUTL;
import static teleblock.widget.TelegramUserAvatar.SPONSOR_US;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.databinding.ActivitySendTransferBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.api.AddTransferRecordApi;
import teleblock.ui.dialog.ChainTypeSelectorDialog;
import teleblock.ui.dialog.GasTipsDialog;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.ui.dialog.TransferErrorDialog;
import teleblock.ui.dialog.TransferLoadDialog;
import teleblock.ui.dialog.TransferSelectorCointypeDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletTransferUtil;
import teleblock.util.parse.TransferParseUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.PriceTextWatcher;
import teleblock.widget.TelegramUserAvatar;

/**
 * Time:2022/9/8
 * Author:Perry
 * Description：发起转账页面
 */
public class SendTransferActivity extends BaseFragment {
    private ActivitySendTransferBinding binding;

    //打赏我们的逻辑
    private boolean sponsorUs = false;
    //外部传入的链ID
    private int selectorChainId = -1;

    private TransferDialogListener mTransferDialogListener;

    private TLRPC.User ownUserInfor;//自己的user信息
    private TLRPC.User userInfor;//对方的user信息

    private String ownWalletAddress;//自己的钱包地址
    private String toAddress;//对方的钱包地址

    //是否在请求中
    private boolean loading = false;

    //币种图标
    private Drawable coinDrawable;

    //钱包余额 单位：coinType
    private BigDecimal walletBalanceBigDecimal;

    //币种单价 单位：美元
    private BigDecimal coinPrice;

    //转账金额 单位：coinType
    private BigDecimal amount;

    //总额 单位：coinType
    private BigDecimal totalMoney;
    //总额 单位：美元
    private BigDecimal totalMoneyDoller;

    //gas金额 单位：coinType
    private BigDecimal gasPrice;
    //gas金额 单位：美元
    private BigDecimal gasPriceDoller;
    //gasfee
    private BigDecimal gasPriceWei;
    //gasLimit
    private BigDecimal gasLimit;

    //选择链对话框
    private ChainTypeSelectorDialog mChainTypeSelectorDialog;
    //选中的链数据
    private Web3ConfigEntity.WalletNetworkConfigChainType currentChainType;

    //链下面的币种数据对话框
    private TransferSelectorCointypeDialog mTransferSelectorCointypeDialog;
    //币种列表数据
    private List<MyCoinListData> mMyCoinListDataList = new ArrayList<>();
    //选中的币种数据
    private MyCoinListData selectorCoinData;
    //主币币种数据
    private MyCoinListData mainCoinData;

    //什么是gas费用
    private GasTipsDialog mGasTipsDialog;

    //加载框
    private LoadingDialog mLoadingDialog;

    //选择gas费用对话框
    //private SelectorGasFeeDialog mSelectorGasFeeDialog;
    //gas费用列表
    //private List<SelectorGasFeeData> gasFeeList = new ArrayList<>();

    public SendTransferActivity(
            TLRPC.User ownUserInfor,
            TLRPC.User user,
            String toAddress,
            int selectorChainId,
            boolean sponsorUs,
            TransferDialogListener mTransferDialogListener
    ) {
        this.ownUserInfor = ownUserInfor;
        this.userInfor = user;
        this.toAddress = toAddress;
        this.selectorChainId = selectorChainId;
        this.sponsorUs = sponsorUs;
        this.mTransferDialogListener = mTransferDialogListener;
    }

    @Override
    public boolean onFragmentCreate() {
        EventBus.getDefault().register(this);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivitySendTransferBinding.inflate(LayoutInflater.from(getContext()));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        mLoadingDialog = new LoadingDialog(getContext(), LocaleController.getString("Loading", R.string.Loading));
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        if (selectorChainId == -1) {
            currentChainType = MMKVUtil.currentChainConfig();
        } else {
            //根据id来匹配选择的链
            for (Web3ConfigEntity.WalletNetworkConfigChainType chainData : MMKVUtil.getWeb3ConfigData().getChainType()) {
                if (chainData.getId() == selectorChainId) {
                    currentChainType = chainData;
                }
            }
        }

        //切换钱包
        binding.tvTitle.setOnClickListener(v -> new WalletListDialog(this) {
            @Override
            public void onItemClick(ETHWallet wallet) {
                setWalletInfo();
            }
        }.show());

        //关闭页面
        binding.tvCloseDialog.setOnClickListener(view -> finishFragment());
        //显示对方头像
        if (userInfor != null) {
            binding.flAvatar.setUserInfo(userInfor)
                    .setModel(sponsorUs ? SPONSOR_US : DEFAUTL)
                    .loadView();
        } else {
            binding.flAvatar.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
        }

        //像谁转账
        String nickName;
        if (sponsorUs) {
            nickName = LocaleController.getString("sponsorus_tips", R.string.sponsorus_tips);
        } else if (userInfor != null) {
            nickName = String.format(LocaleController.getString("chat_transfer_towhotransfer", R.string.chat_transfer_towhotransfer), UserObject.getUserName(userInfor));
        } else {
            nickName = String.format(LocaleController.getString("chat_transfer_towhotransfer", R.string.chat_transfer_towhotransfer), WalletUtil.formatAddress(toAddress));
        }

        binding.tvNickname.setText(nickName);
        binding.tvNickname2.setText(nickName);

        setWalletInfo();

        setSetpOne();
    }

    /**
     * 设置钱包信息
     */
    private void setWalletInfo() {
        //获取钱包地址
        ownWalletAddress = WalletDaoUtils.getCurrent().getAddress();
        //显示钱包名称
        binding.tvTitle.setText(String.format(LocaleController.getString("wallet_type_title", R.string.wallet_type_title),
                ownWalletAddress.toLowerCase().startsWith("0x") ? "ETH" : "Solana"));
        binding.tvAddress.setText(WalletUtil.format6Address(ownWalletAddress));

        //设置链数据
        setChainTypeUi(currentChainType);
    }

    /**
     * 设置步骤1样式和逻辑
     */
    private void setSetpOne() {
        binding.tvNextstep.setText(LocaleController.getString("chat_transfer_nextstep", R.string.chat_transfer_nextstep));
        binding.tvTips.setText(LocaleController.getString("chat_transfer_tips", R.string.chat_transfer_tips));
        binding.tvStepTwoBack.setVisibility(View.GONE);
        binding.llStepOne.setVisibility(View.VISIBLE);
        binding.clSetpTwo.setVisibility(View.GONE);

        //输入的金额转化的美元，默认值
        binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips", R.string.chat_transfer_input_price_tips));
        binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));

        //选择链类型
        binding.tvChaintype.setOnClickListener(view -> {
            if (mChainTypeSelectorDialog == null) {
                mChainTypeSelectorDialog = new ChainTypeSelectorDialog(getContext(), data -> {
                    currentChainType = data;
                    setChainTypeUi(data);
                });
            }
            mChainTypeSelectorDialog.setCurrentChainType(currentChainType);

            if (!loading) {
                //显示选择链对话框
                mChainTypeSelectorDialog.show();
            }
        });

        //选择币种 弹出对话框
        binding.tvCoinType.setOnClickListener(view -> {
            if (mTransferSelectorCointypeDialog == null) {
                mTransferSelectorCointypeDialog = new TransferSelectorCointypeDialog(getContext(), coinData -> {
                    selectorCoinData = coinData;

                    selectorCoinInfo(coinData);
                });
            }

            mTransferSelectorCointypeDialog.setData(mMyCoinListDataList);
            mTransferSelectorCointypeDialog.show();
        });

        //键盘点击事件监听
//        binding.etInputNum.setOnEditorActionListener((textView, i, keyEvent) -> {
//            AndroidUtilities.hideKeyboard(getWindow().getDecorView());
//            if (binding.llNextstep.isClickable()) {
//                binding.llNextstep.performClick();
//            }
//            return false;
//        });

        //监听焦点是否获取
        binding.etInputNum.setOnFocusChangeListener((v, hasFocus) -> {
            EditText _e = (EditText) v;
            if (!hasFocus) {
                //没有焦点
                _e.setHint(_e.getTag().toString());
            } else { //获取到焦点
                _e.setTag(_e.getHint().toString());
                _e.setHint("");
            }
        });

        //输入限制判断
        binding.etInputNum.addTextChangedListener(new PriceTextWatcher(binding.etInputNum) {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                if (walletBalanceBigDecimal == null) {
                    return;
                }
                String transFerMoneyStr = binding.etInputNum.getText().toString().trim();
                try {
                    amount = new BigDecimal(transFerMoneyStr);
                } catch (Exception e) {
                    amount = new BigDecimal(String.valueOf(0f));
                }

                if (WalletUtil.decimalCompareTo(amount, new BigDecimal(String.valueOf(0f)))) {
                    if (WalletUtil.decimalCompareTo(amount, walletBalanceBigDecimal)) {
                        changeNextBtnStatus(false);
                        binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips1", R.string.chat_transfer_input_price_tips1));
                        binding.tvInputPrice.setTextColor(Color.parseColor("#FF4550"));
                    } else {
                        changeNextBtnStatus(true);
                        binding.tvInputPrice.setText(WalletUtil.toCoinPriceUSD(amount, coinPrice, 6));
                        binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));
                    }
                } else {
                    binding.tvInputPrice.setText(LocaleController.getString("chat_transfer_input_price_tips", R.string.chat_transfer_input_price_tips));
                    binding.tvInputPrice.setTextColor(Color.parseColor("#56565c"));
                    changeNextBtnStatus(false);
                }
            }
        });

        //下一步按钮点击事件
        binding.tvNextstep.setOnClickListener(view -> {
            //显示加载中布局
            mLoadingDialog.show();
            if (WalletUtil.isSolanaAddress(WalletDaoUtils.getCurrent().getAddress())) {
                mLoadingDialog.dismiss();
                setGasmoneyUi(gasPrice);
                setSetpTwo();
            } else {
                //请求gas费用
                WalletUtil.requestGasFee(currentChainType.getId(), selectorCoinData.getDecimal(), ownWalletAddress, toAddress, amount, selectorCoinData.getContractAddress(), (gasFee, gasLimit) -> {
                    this.gasPrice = new BigDecimal(WalletUtil.fromWei((Convert.fromWei(gasFee.toPlainString(), Convert.Unit.GWEI).multiply(gasLimit)).toPlainString(), 9));
                    this.gasPriceWei = gasFee;
                    this.gasLimit = gasLimit;
                    setGasmoneyUi(gasPrice);

                    mLoadingDialog.dismiss();
                    setSetpTwo();
                });
            }
        });

        changeNextBtnStatus(false);
    }

    /**
     * 设置步骤2的样式和逻辑
     */
    private void setSetpTwo() {
        binding.tvChaintype.setVisibility(View.GONE);
        binding.llWalletInfo.setVisibility(View.GONE);
        binding.tvWalletBalance.setVisibility(View.GONE);
        binding.tvStepTwoBack.setVisibility(View.VISIBLE);
        binding.llStepOne.setVisibility(View.GONE);
        binding.clSetpTwo.setVisibility(View.VISIBLE);//显示步骤2view

        binding.tvStepTwoBack.setText(LocaleController.getString("dg_transfer_step_tow_title", R.string.dg_transfer_step_tow_title));
        binding.tvBtnTransferConfirm.setText(LocaleController.getString("chat_transfer_confirm", R.string.chat_transfer_confirm));
        binding.tvBtnBack.setText(LocaleController.getString("chat_transfer_back", R.string.chat_transfer_back));

        binding.tvGas.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        binding.tvGas.getPaint().setAntiAlias(true);

        binding.tvStepTwoBack.setOnClickListener(view -> finishFragment());

        //gas说明
        binding.tvGasTitle.setOnClickListener(view -> {
            if (mGasTipsDialog == null) {
                mGasTipsDialog = new GasTipsDialog(getContext());
            }
            mGasTipsDialog.show();
        });

        //确认转账
        binding.tvBtnTransferConfirm.setOnClickListener(view -> {
            TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading), true);

            //发起支付
            WalletTransferUtil.sendTransaction(
                    currentChainType.getId(),
                    toAddress,
                    gasPriceWei.toBigInteger(),
                    gasLimit.toBigInteger(),
                    selectorCoinData.getContractAddress(),
                    amount.toPlainString(),
                    selectorCoinData.getDecimal(),
                    selectorCoinData.getSymbol(),
                    new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            transferSuccessful(hash);
                        }

                        @Override
                        public void payError(String error) {
                            TransferLoadDialog.stopLoading();
                            if (getContext() != null) {
                                new TransferErrorDialog(getContext(), error).show();
                            }
                        }
                    }
            );
        });

        //设置自己的钱包地址
        binding.tvFromAccount.setText(WalletUtil.formatAddress(ownWalletAddress));
        //自己的头像
        binding.flFromAvatar.setUserInfo(ownUserInfor).loadView();

        //设置对方的钱包地址
        binding.tvToAccount.setText(WalletUtil.formatAddress(toAddress));
        //显示对方头像
        if (userInfor != null) {
            binding.flToAvatar.setUserInfo(userInfor).setModel(sponsorUs ? SPONSOR_US : DEFAUTL).loadView();
            binding.flAvatar2.setUserInfo(userInfor).setModel(sponsorUs ? SPONSOR_US : DEFAUTL).loadView();
        } else {
            binding.flToAvatar.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
            binding.flAvatar2.setModel(TelegramUserAvatar.ADDRESS_TRANSFER).loadView();
        }

        //钱包余额
        String balanceStr = String.format(LocaleController.getString("chat_transfer_balance", R.string.chat_transfer_balance),
                WalletUtil.priceCoinType(walletBalanceBigDecimal, selectorCoinData.getSymbol(), false));
        binding.tvFromBalance.setText(balanceStr);

        //转账金额
        binding.tvTransferfee.setText(WalletUtil.priceCoinType(amount, selectorCoinData.getSymbol(), false));
        //转账金额 美元
        binding.tvTransferfeeDollar.setText(WalletUtil.toCoinPriceUSD(amount, coinPrice, 6));
    }

    /**
     * 转账成功 上报结果
     *
     * @param hash
     */
    private void transferSuccessful(String hash) {
        if (sponsorUs) {
            TransferLoadDialog.updateLoading(LocaleController.getString("toast_tips_dscg", R.string.toast_tips_dscg));
        } else {
            TransferLoadDialog.updateLoading(LocaleController.getString("transfer_pay_successful", R.string.transfer_pay_successful));
        }

        long coinId = 0L;
        for (Web3ConfigEntity.WalletNetworkConfigEntityItem coinItemData : currentChainType.getCurrency()) {
            if (coinItemData.getName().equals(selectorCoinData.getSymbol())) {
                coinId = coinItemData.getId();
            }
        }

        //上报转账结果
        AddTransferRecordApi addTransferRecordApi = new AddTransferRecordApi();
        addTransferRecordApi.setPayment_tg_user_id(String.valueOf(ownUserInfor.id))
                .setPayment_account(ownWalletAddress)
                .setReceipt_tg_user_id(String.valueOf(userInfor == null ? 0 : userInfor.id))
                .setReceipt_account(toAddress)
                .setChain_id(currentChainType.getId())
                .setChain_name(currentChainType.getName())
                .setCurrency_id(coinId)
                .setCurrency_name(selectorCoinData.getSymbol())
                .setAmount(amount.toPlainString())
                .setTx_hash(hash);
        EasyHttp.post(new ApplicationLifecycle()).api(addTransferRecordApi).request(null);
        if (userInfor != null) {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.TRANSFER_SUCCESSFUL, userInfor.id));
        }

        ThreadUtils.getMainHandler().postDelayed(() -> {
            TransferLoadDialog.stopLoading();
            if (mTransferDialogListener != null) {
                String parserStr = TransferParseUtil.setParseStr(
                        ownUserInfor.id,
                        userInfor == null ? 0 : userInfor.id,
                        selectorCoinData.getSymbol(),
                        hash,
                        ownWalletAddress,
                        toAddress,
                        amount,
                        gasPrice,
                        totalMoney,
                        totalMoneyDoller,
                        currentChainType.getId(),
                        currentChainType.getName(),
                        currentChainType.getExplorer_url()
                );
                mTransferDialogListener.transferSuccessful(parserStr);
            }
            finishFragment();
        }, 1500);
    }

    /**
     * 修改下一步按钮状态，是否可以点击
     *
     * @param isClickable
     */
    private void changeNextBtnStatus(boolean isClickable) {
        if (isClickable) {
            binding.tvNextstep.setClickable(true);
            binding.tvNextstep.setAlpha(1f);
        } else {
            binding.tvNextstep.setClickable(false);
            binding.tvNextstep.setAlpha(0.5f);
        }
    }


    /**
     * 设置gas费用 并计算总额
     *
     * @param gasPrice 单位Gwei
     */
    private void setGasmoneyUi(BigDecimal gasPrice) {
        //默认gas费用
        binding.tvGas.setText(WalletUtil.priceCoinType(gasPrice, mainCoinData.getSymbol(), false));
        //gas费用美元
        gasPriceDoller = WalletUtil.bigDecimalScale(gasPrice.multiply(mainCoinData.getPrice()), 6);
        binding.tvGasDollar.setText("$" + gasPriceDoller);

        setTotalMoneyUi();
    }

    /**
     * 设置总额
     */
    private void setTotalMoneyUi() {
        //总额计算
        if (selectorCoinData.isIs_main_currency()) {
            totalMoney = amount.add(gasPrice);
            binding.tvTotal.setText(WalletUtil.priceCoinType(totalMoney, selectorCoinData.getSymbol(), false));
        } else {
            totalMoney = amount;
            binding.tvTotal.setText(WalletUtil.priceCoinType(totalMoney, selectorCoinData.getSymbol(), true)
                    + "+" + WalletUtil.priceCoinType(gasPrice, mainCoinData.getSymbol(), true));
        }

        binding.tvTotal.getHelper().setIconNormalLeft(coinDrawable);

        //总额美元
        totalMoneyDoller = totalMoney.multiply(coinPrice).add(gasPriceDoller);
        StringBuffer totalDoller = new StringBuffer("$");
        totalDoller.append(WalletUtil.bigDecimalScale(totalMoneyDoller, 10).toPlainString());
        binding.tvTotalDoller.setText(totalDoller.toString());
    }

    /**
     * 设置链的ui数据 并赋值
     *
     * @param data
     */
    private void setChainTypeUi(Web3ConfigEntity.WalletNetworkConfigChainType data) {
        requestWalletInforData();

        //链的名称
        binding.tvChaintype.setText(data.getName());
        //获取显示链图标
        GlideHelper.getDrawableGlide(binding.getRoot().getContext(), data.getIcon(), drawable -> binding.tvChaintype.getHelper().setIconNormalLeft(drawable));
    }

    /**
     * 请求链和钱包下面的数据
     */
    private void requestWalletInforData() {
        mLoadingDialog.show();
        //清空一些数据
        binding.etInputNum.setText("");
        walletBalanceBigDecimal = BigDecimal.ZERO;
        coinPrice = BigDecimal.ZERO;
        gasPrice = BigDecimal.ZERO;
        gasPriceWei = BigDecimal.ZERO;
        gasLimit = BigDecimal.ZERO;

        //每次切换链的时候都要获取这个链下面的主币账户信息，以及钱包下所有的代币信息
        WalletUtil.requestWalletCoinBalance(ownWalletAddress, currentChainType.getId(), mMyCoinListDataList, () -> {
            loading = false;
            if (!mMyCoinListDataList.isEmpty()) {
                //默认选中第一条的币种数据
                selectorCoinData = mMyCoinListDataList.get(0);
                if (selectorCoinData.getChainId() == currentChainType.getId()) {
                    //隐藏加载状态
                    mLoadingDialog.dismiss();

                    //获取主币价格
                    for (MyCoinListData coinListData : mMyCoinListDataList) {
                        if (coinListData.isIs_main_currency()) {
                            mainCoinData = coinListData;
                        }
                    }

                    //设置选中的币种数据
                    selectorCoinInfo(selectorCoinData);
                }
            } else {
                selectorCoinData = new MyCoinListData();
                mainCoinData = new MyCoinListData();
                selectorCoinInfo(new MyCoinListData());
                mLoadingDialog.dismiss();
            }
        });
    }

    /**
     * 计算选择的币种信息数据并设置显示图标icon和名称
     *
     * @param mMyCoinListData
     */
    private void selectorCoinInfo(MyCoinListData mMyCoinListData) {
        walletBalanceBigDecimal = mMyCoinListData.getBalance();
        coinPrice = mMyCoinListData.getPrice();

        //当前币种账户余额
        binding.tvWalletBalance.setText(WalletUtil.priceCoinType(walletBalanceBigDecimal, selectorCoinData.getSymbol(), false));

        //币种 文字
        binding.tvCoinType.setText(mMyCoinListData.getSymbol());
        //币种 图标
        if (!TextUtils.isEmpty(mMyCoinListData.getIcon())) {
            GlideHelper.getDrawableGlide(binding.getRoot().getContext(), mMyCoinListData.getIcon(), drawable -> {
                coinDrawable = drawable;
                binding.tvCoinType.getHelper().setIconNormalLeft(drawable);
            });
        } else {
            if (mMyCoinListData.getIconRes() != 0) {
                coinDrawable = ResourceUtils.getDrawable(mMyCoinListData.getIconRes());
                binding.tvCoinType.getHelper().setIconNormalLeft(coinDrawable);
            } else {
                binding.tvCoinType.getHelper().setIconNormalLeft(null);
            }
        }
    }

    public interface TransferDialogListener {
        void transferSuccessful(String parseStr);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CHANGED:
            case EventBusTags.WALLET_CREATED:
                setWalletInfo();
                break;
        }
    }
}