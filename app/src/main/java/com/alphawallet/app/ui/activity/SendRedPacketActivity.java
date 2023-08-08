package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivitySendRedpacketBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BonusConfigEntity;
import teleblock.model.BonusStatusEntity;
import teleblock.model.CreateBonusEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.BonusStatusApi;
import teleblock.network.api.CreateBonusApi;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.ui.dialog.RedGasTipsDialog;
import teleblock.ui.dialog.SendRedPacketSuccessfulDialog;
import teleblock.ui.dialog.TransferErrorDialog;
import teleblock.ui.dialog.TransferLoadDialog;
import teleblock.ui.dialog.TransferSelectorCointypeDialog;
import teleblock.ui.dialog.WalletListDialog;
import teleblock.ui.dialog.sendredpacket.SendRedpacketSelectorChainTypeDialog;
import teleblock.util.MMKVUtil;
import teleblock.util.TGLog;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.util.parse.RedPacketParseUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.PriceTextWatcher;

/**
 * Time:2022/12/13
 * Author:Perry
 * Description：发红包页面
 */
public class SendRedPacketActivity extends BaseFragment {
    private ActivitySendRedpacketBinding binding;

    private SendRedPacketDialogListener listener;

    //红包配置数据
    private BonusConfigEntity bonusConfigEntity;

    //选择链对话框
    private SendRedpacketSelectorChainTypeDialog mSendRedpacketSelectorChainTypeDialog;
    //选中的链数据
    private BonusConfigEntity.Config currentChainType;

    //链下面的币种数据对话框
    private TransferSelectorCointypeDialog mTransferSelectorCointypeDialog;
    //所有币种列表数据
    private List<MyCoinListData> allCoinList = new ArrayList<>();
    //当前的币种列表数据
    private List<MyCoinListData> coinList = new ArrayList<>();
    //选中的币种数据
    private MyCoinListData selectorCoinData;
    //选择的币种ID
    private long coinId = 0L;

    //自己的钱包地址
    private String ownWalletAddress;

    //当前用户信息
    private TLRPC.User currentUser;

    //总金额 单位：coinType
    private BigDecimal totalAmount;
    //输入的红包金额
    private BigDecimal inputBounsAmount;
    //gas费总额
    private BigDecimal totalGasfee;
    //gas费单价
    private BigDecimal gasfee = new BigDecimal("0");
    //红包数量
    private BigDecimal inputBounsNum;
    //钱包余额 单位：coinType
    private BigDecimal walletBalanceBigDecimal;
    //币种单价 单位：美元
    private BigDecimal coinPrice;

    //红包最大数量
    private BigDecimal bigNumBigDecimal;
    //红包最小金额
    private BigDecimal minPrice;

    //是单聊还是群聊
    private boolean isSingleChat;
    //群ID
    private long chatId;
    //群链接
    private String chatLink;

    //加载框
    private LoadingDialog mLoadingDialog;

    //交易成功弹窗
    private SendRedPacketSuccessfulDialog mSendRedPacketSuccessfulDialog;
    // 请求次数
    private int requestCount;

    public SendRedPacketActivity(
            BonusConfigEntity bonusConfigEntity,
            boolean isSingleChat,
            long chatId,
            String chatLink,
            SendRedPacketDialogListener listener
    ) {
        this.bonusConfigEntity = bonusConfigEntity;
        this.isSingleChat = isSingleChat;
        this.chatId = chatId;
        this.chatLink = chatLink;
        currentUser = getUserConfig().getCurrentUser();
        this.listener = listener;
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
        BarUtils.transparentStatusBar(getParentActivity());
        BarUtils.setStatusBarLightMode(getParentActivity(), true);
        binding = ActivitySendRedpacketBinding.inflate(LayoutInflater.from(getContext()));
        initView();

        loadData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);
        binding.etInputNum.setHint("0");
        binding.tvRedPacketTitle.setText(LocaleController.getString("dialog_send_redpackets_packet_num", R.string.dialog_send_redpackets_packet_num));
        binding.etInputRedpacketNum.setHint("0");
        binding.etInputBenediction.setHint(LocaleController.getString("dialog_send_redpackets_input_benediction", R.string.dialog_send_redpackets_input_benediction));
        binding.tvSendRedPacket.setText(LocaleController.getString("dialog_send_redpackets", R.string.dialog_send_redpackets));
        binding.tvSendLoadingTitle.setText(LocaleController.getString("dialog_send_redpackets", R.string.dialog_send_redpackets));
        binding.tvRedpacketTotalBalanceTitle.setText(LocaleController.getString("dialog_send_redpackets_total_balance", R.string.dialog_send_redpackets_total_balance));

        binding.tvTips.setText(LocaleController.getString("dialog_send_redpackets_tips", R.string.dialog_send_redpackets_tips));
        binding.tvCloseDialog.setOnClickListener(v -> finishFragment());

        binding.rlRedpacketNum.setVisibility(isSingleChat ? View.GONE : View.VISIBLE);

        //切换钱包
        binding.tvTitle.setOnClickListener(v -> new WalletListDialog(this) {
            @Override
            public void onItemClick(ETHWallet wallet) {
                loadData();
            }
        }.show());

        //切换链点击事件
        binding.tvChaintype.setOnClickListener(view -> {
            if (mSendRedpacketSelectorChainTypeDialog == null) {
                mSendRedpacketSelectorChainTypeDialog = new SendRedpacketSelectorChainTypeDialog(getContext(), this::setChainTypeUi);
            }
            mSendRedpacketSelectorChainTypeDialog.setCurrentChainType(currentChainType, bonusConfigEntity.getConfig());
            //显示选择链对话框
            mSendRedpacketSelectorChainTypeDialog.show();
        });

        //选择币种 弹出对话框
        binding.tvCoinType.setOnClickListener(view -> {
            if (mTransferSelectorCointypeDialog == null) {
                mTransferSelectorCointypeDialog = new TransferSelectorCointypeDialog(getContext(), this::selectorCoinInfo);
            }

            mTransferSelectorCointypeDialog.setData(coinList);
            mTransferSelectorCointypeDialog.show();
        });

        //输入金额限制判断
        binding.etInputNum.addTextChangedListener(new PriceTextWatcher(binding.etInputNum) {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                inputJudge();
            }
        });

        //输入红包个数判断 最大500个
        binding.etInputRedpacketNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputJudge();
            }
        });

        //发送红包
        binding.tvSendRedPacket.setOnClickListener(v -> {
            TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading));
            //要转到到地址
            String toAddress = bonusConfigEntity.getAddress();
            if (currentChainType.getId() == 99999) {
                toAddress = bonusConfigEntity.getSolana_address();
            } else if (currentChainType.getId() == 999) {
                toAddress = bonusConfigEntity.getTron_address();
            }
            //精度
            int decimal = selectorCoinData.getDecimal();

            //请求gas费和gaslimit
//            WalletUtil.requestGasFee(currentChainType.getId(), decimal, ownWalletAddress, toAddress, totalAmount, selectorCoinData.getContractAddress(), (gasFee, gasLimit) -> {
            //发起支付
            WalletTransferUtil.sendTransaction(
                    currentChainType.getId(),
                    toAddress,
                    null,
                    null,
                    selectorCoinData.getContractAddress(),
                    totalAmount.toPlainString(),
                    decimal,
                    selectorCoinData.getSymbol(),
                    new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            payRedPacketSuccessful(hash);
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
//            });
        });
    }

    private void loadData() {
        mLoadingDialog = new LoadingDialog(getContext(), LocaleController.getString("Loading", R.string.Loading));

        //获取钱包地址
        ownWalletAddress = WalletDaoUtils.getCurrent().getAddress();
        //显示钱包名称
        binding.tvTitle.setText(String.format(LocaleController.getString("wallet_type_title", R.string.wallet_type_title),
                ownWalletAddress.toLowerCase().startsWith("0x") ? "ETH" : "Solana"));
        binding.tvAddress.setText(WalletUtil.format6Address(ownWalletAddress));

        //设置链数据
        setChainTypeUi(bonusConfigEntity.getConfig().get(0));
    }

    /**
     * 支付成功 上报结果
     *
     * @param hash
     */
    private void payRedPacketSuccessful(String hash) {
        TransferLoadDialog.updateLoading(LocaleController.getString("transfer_pay_successful", R.string.transfer_pay_successful));

        String explorerUrl = "";
        for (Web3ConfigEntity.WalletNetworkConfigChainType chainType : MMKVUtil.getWeb3ConfigData().getChainType()) {
            if (chainType.getId() == currentChainType.getId()) {
                explorerUrl = chainType.getExplorer_url();
            }
        }

        //初始化交易成功弹窗
        String url = explorerUrl + (currentChainType.getId() == 999 ? "/transaction/" : "/tx/") + hash;
        mSendRedPacketSuccessfulDialog = new SendRedPacketSuccessfulDialog(
                getContext(),
                selectorCoinData,
                totalAmount,
                url,
                () -> finishFragment()
        );


        //上报服务器
        CreateBonusApi createBonusApi = new CreateBonusApi();
        createBonusApi.setTx_hash(hash);
        createBonusApi.setAmount(inputBounsAmount.toPlainString());
        createBonusApi.setNum(inputBounsNum.toPlainString());
        createBonusApi.setChain_id(currentChainType.getId());
        createBonusApi.setChain_name(currentChainType.getName());
        createBonusApi.setCurrency_id(coinId);
        createBonusApi.setSource(isSingleChat ? 2 : 1);
        createBonusApi.setMessage(binding.etInputBenediction.getText().toString());
        createBonusApi.setCurrency_name(selectorCoinData.getSymbol());
        createBonusApi.setPayment_account(ownWalletAddress);
        createBonusApi.setGas_amount(totalGasfee.toPlainString());
        if (!isSingleChat) {
            createBonusApi.setTg_group_id(String.valueOf(chatId));
            createBonusApi.setTg_group_link(chatLink);
        }
        EasyHttp.post(new ApplicationLifecycle())
                .api(createBonusApi)
                .request(new OnHttpListener<BaseBean<CreateBonusEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<CreateBonusEntity> data) {
                        mSendRedPacketSuccessfulDialog.show();
                        queryRedpacketStatus(data);
                    }

                    @Override
                    public void onFail(Exception e) {
                        new TransferErrorDialog(getContext(), e.getMessage()).show();
                        TGLog.erro("创建红包失败：" + e.getMessage());
                    }

                    @Override
                    public void onEnd(Call call) {
                        TransferLoadDialog.stopLoading();
                    }
                });
    }

    /**
     * 查询红包状态，是否上链，3s查询一次
     *
     * @param data
     */
    private void queryRedpacketStatus(BaseBean<CreateBonusEntity> data) {
        requestCount++;
        if (requestCount > 20) {
            requestCount = 0;
            return;
        }
        ThreadUtils.getMainHandler().postDelayed(() -> EasyHttp.post(new ApplicationLifecycle())
                .api(new BonusStatusApi().setSecret_num(data.getData().getSecret_num()))
                .request(new OnHttpListener<BaseBean<BonusStatusEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<BonusStatusEntity> result) {
                        if (result.getData().status == 2) {
                            mSendRedPacketSuccessfulDialog.showUpChainSuccessfulView();

                            //加密
                            String parseStr = RedPacketParseUtil.setParseStr(
                                    currentUser.id,
                                    result.getData().secret_num,
                                    currentChainType.getId(),
                                    selectorCoinData.getSymbol(),
                                    currentChainType.getName(),
                                    inputBounsAmount,
                                    selectorCoinData.getSymbol(),
                                    inputBounsNum.toPlainString(),
                                    data.getData().getExpire_at()
                            );
                            listener.redPacketSendSuccessful(parseStr);
                        } else {
                            queryRedpacketStatus(data);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        queryRedpacketStatus(data);
                    }
                }), 3000);
    }

    /**
     * 输入判断
     */
    private BigDecimal zeroBigDecimal = new BigDecimal("0");

    private void inputJudge() {
        if (walletBalanceBigDecimal == null) {
            changeNextBtnStatus(false);
            return;
        }

        //金额
        String moneyStr = binding.etInputNum.getText().toString().trim();
        try {
            inputBounsAmount = new BigDecimal(moneyStr);
        } catch (Exception e) {
            inputBounsAmount = zeroBigDecimal;
        }

        if (isSingleChat) {
            //红包个数设置为1
            inputBounsNum = new BigDecimal(String.valueOf(1));
        } else {
            //数量
            String numStr = binding.etInputRedpacketNum.getText().toString().trim();
            try {
                inputBounsNum = new BigDecimal(numStr);
            } catch (Exception e) {
                inputBounsNum = zeroBigDecimal;
            }
        }

        //gas费总额
        totalGasfee = inputBounsNum.multiply(gasfee);
        //输入的金额+gas总费用=红包总金额
        totalAmount = inputBounsAmount.add(totalGasfee);

        //总额
        binding.tvRedpacketTotalBalanceDoller.setText(WalletUtil.toCoinPriceUSD(totalAmount, coinPrice, 6));
        //剩余钱
        binding.tvSurplusBalance.setText(
                String.format(LocaleController.getString("dialog_send_redpackets_surplus_balance", R.string.dialog_send_redpackets_surplus_balance),
                        walletBalanceBigDecimal.subtract(totalAmount))
        );

        if (WalletUtil.decimalCompareTo(inputBounsAmount, zeroBigDecimal) && WalletUtil.decimalCompareTo(inputBounsNum, zeroBigDecimal)) {
            changeNextBtnStatus(
                    WalletUtil.decimalCompareTo(walletBalanceBigDecimal, totalAmount)
                            && !WalletUtil.decimalCompareTo(inputBounsNum, bigNumBigDecimal)
                            && WalletUtil.decimalCompareTo(inputBounsAmount.divide(inputBounsNum, 6, RoundingMode.UP), minPrice)
            );

            if (WalletUtil.decimalCompareTo(walletBalanceBigDecimal, totalAmount)) {
                if (WalletUtil.decimalCompareTo(inputBounsNum, bigNumBigDecimal)) {
                    showErrorTipsView(String.format(LocaleController.getString("dialog_send_redpackets_error_tips1", R.string.dialog_send_redpackets_error_tips1), bigNumBigDecimal));
                } else {
                    if (WalletUtil.decimalCompareTo(inputBounsAmount.divide(inputBounsNum, 6, RoundingMode.UP), minPrice)) {
                        showErrorTipsView("");
                    } else {
                        showErrorTipsView(String.format(LocaleController.getString("dialog_send_redpackets_error_tips2", R.string.dialog_send_redpackets_error_tips2), priceCoinType(minPrice)));
                    }
                }
            } else {
                showErrorTipsView(LocaleController.getString("chat_transfer_input_price_tips1", R.string.chat_transfer_input_price_tips1));
            }
        } else {
            changeNextBtnStatus(false);
        }
    }

    /**
     * 显示错误提示
     *
     * @param content
     */
    private void showErrorTipsView(String content) {
        binding.tvErrorTips.setVisibility(content.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvErrorTips.setText(content);
    }

    /**
     * 设置链的ui数据 并赋值
     *
     * @param data
     */
    private void setChainTypeUi(BonusConfigEntity.Config data) {
        this.currentChainType = data;

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
        walletBalanceBigDecimal = null;
        coinPrice = null;
        totalGasfee = null;
        totalAmount = null;
        inputBounsNum = null;
        minPrice = new BigDecimal("0");

        //每次切换链的时候都要获取这个链下面的主币账户信息，以及钱包下所有的代币信息
        WalletUtil.requestWalletCoinBalance(ownWalletAddress, currentChainType.getId(), allCoinList, () -> {
            if (!CollectionUtils.isEmpty(allCoinList)) {
                MyCoinListData checkData = allCoinList.get(0);
                if (checkData.getChainId() == currentChainType.getId()) {
                    coinList.clear();
                    for (BonusConfigEntity.Currency currency : currentChainType.getCurrency()) {
                        //添加所有服务端配置的币
                        boolean has = false;
                        for (MyCoinListData myCoinListData : allCoinList) {
                            if (myCoinListData.getSymbol().equals(currency.getName())) {
                                coinList.add(myCoinListData);
                                has = true;
                                break;
                            }
                        }
                        if (has) {
                            continue;
                        }

                        MyCoinListData myCoinListData = new MyCoinListData();
                        myCoinListData.setChainId(currentChainType.getId());
                        myCoinListData.setSymbol(currency.getName());
                        myCoinListData.setIcon(currency.getIcon());
                        myCoinListData.setPrice(new BigDecimal("0"));
                        myCoinListData.setBalance(new BigDecimal("0"));
                        myCoinListData.setDecimal(currency.getDecimal());
                        myCoinListData.setIs_main_currency(currency.isIs_main_currency());
                        myCoinListData.setContractAddress("");
                        coinList.add(myCoinListData);
                    }

                    //设置选中的币种数据 默认选中第一条的币种数据
                    selectorCoinInfo(coinList.get(0));
                    mLoadingDialog.dismiss();
                }
            } else {
                coinList.clear();
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
        this.selectorCoinData = mMyCoinListData;
        walletBalanceBigDecimal = mMyCoinListData.getBalance();
        coinPrice = mMyCoinListData.getPrice();

        //默认ui状态
        showErrorTipsView("");
        binding.etInputRedpacketNum.setText("");
        binding.etInputNum.setText("");

        //总额
        binding.tvRedpacketTotalBalanceDoller.setText(WalletUtil.toCoinPriceUSD(zeroBigDecimal, coinPrice, 6));

        //筛选gas费
        for (BonusConfigEntity.Currency currency : currentChainType.getCurrency()) {
            if (mMyCoinListData.getSymbol().equals(currency.getName())) {
                //币种ID
                coinId = currency.getId();
                //获取gas费
                gasfee = new BigDecimal(String.valueOf(currency.getGas_price()));
                //红包最大个数
                bigNumBigDecimal = new BigDecimal(currency.getMax_num());
                //红包最小金额
                minPrice = new BigDecimal(StringUtils.isEmpty(currency.getMin_price()) ? "0" : currency.getMin_price());
            }
        }

        //当前币种账户余额
        String walletBalance = String.format(
                LocaleController.getString("chat_transfer_wallet_balance", R.string.chat_transfer_wallet_balance)
                , priceCoinType(walletBalanceBigDecimal)
        ) + "≈" + WalletUtil.toCoinPriceUSD(walletBalanceBigDecimal, coinPrice, 6);
        binding.tvBalance.setText(walletBalance);
        binding.tvTopWalletBalance.setText(priceCoinType(walletBalanceBigDecimal));

        //币种 文字
        binding.tvCoinType.setText(mMyCoinListData.getSymbol());
        //币种 图标
        if (!TextUtils.isEmpty(mMyCoinListData.getIcon())) {
            GlideHelper.getDrawableGlide(binding.getRoot().getContext(), mMyCoinListData.getIcon(), drawable -> {
                binding.tvCoinType.getHelper().setIconNormalLeft(drawable);
            });
        } else if (mMyCoinListData.getIconRes() > 0) {
            binding.tvCoinType.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(mMyCoinListData.getIconRes()));
        } else {
            binding.tvCoinType.getHelper().setIconNormalLeft(null);
        }
    }

    /**
     * bigdecimal转String 增加币种单位
     *
     * @param bigDecimal
     */
    private String priceCoinType(BigDecimal bigDecimal) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(WalletUtil.bigDecimalScale(bigDecimal, 10).toPlainString());
        stringBuffer.append(" ");
        stringBuffer.append(selectorCoinData.getSymbol());
        return stringBuffer.toString();
    }

    /**
     * 修改下一步按钮状态，是否可以点击
     *
     * @param isClickable
     */
    private void changeNextBtnStatus(boolean isClickable) {
        if (isClickable) {
            binding.tvSendRedPacket.setClickable(true);
            binding.tvSendRedPacket.setAlpha(1f);

            //红包个数
            String gasFeeTitleStr = LocaleController.getString("dialog_send_redpackets_gastitle", R.string.dialog_send_redpackets_gastitle);
            binding.tvGasFeeTitle.setText(String.format(gasFeeTitleStr, inputBounsNum.toPlainString()));

            //gas费
            String gasfeeStr = gasfee.toPlainString() + "*" + inputBounsNum.toPlainString() + "=" + priceCoinType(totalGasfee);
            binding.tvGasFee.setText(gasfeeStr);
            binding.tvGasFee.getPaint().setFlags(0);
            binding.tvGasFee.setOnClickListener(v -> {
            });
        } else {
            binding.tvGasFeeTitle.setText("Gas fee");
            binding.tvGasFee.setText(LocaleController.getString("dialog_send_redpackets_gas", R.string.dialog_send_redpackets_gas));
            binding.tvGasFee.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            binding.tvGasFee.setOnClickListener(v -> new RedGasTipsDialog(getContext()).show());
            binding.tvSendRedPacket.setClickable(false);
            binding.tvSendRedPacket.setAlpha(0.3f);
        }
    }

    public interface SendRedPacketDialogListener {
        void redPacketSendSuccessful(String str);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.WALLET_CREATED:
                loadData();
                break;
        }
    }
}
