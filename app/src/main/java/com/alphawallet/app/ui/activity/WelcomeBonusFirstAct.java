package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActWelcomeBonusFirstBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import teleblock.model.BonusConfigEntity;
import teleblock.model.PromotionBonusConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.network.BaseBean;
import teleblock.network.api.PromotionBonusConfigApi;
import teleblock.ui.dialog.RedGasTipsDialog;
import teleblock.ui.dialog.TransferSelectorCointypeDialog;
import teleblock.ui.dialog.sendredpacket.SendRedpacketSelectorChainTypeDialog;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;
import teleblock.widget.PriceTextWatcher;

/**
 * 欢迎红包-第一步
 */
public class WelcomeBonusFirstAct extends BaseFragment {

    private ActWelcomeBonusFirstBinding binding;

    //红包配置数据
    private PromotionBonusConfigEntity bonusConfigEntity;
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

    private BigDecimal zeroBigDecimal = new BigDecimal("0");
    //总金额 单位：coinType
    private BigDecimal totalAmount = zeroBigDecimal;
    //输入的红包金额
    private BigDecimal inputBounsAmount = zeroBigDecimal;
    //gas费总额
    private BigDecimal totalGasfee = zeroBigDecimal;
    //gas费单价
    private BigDecimal gasfee = zeroBigDecimal;
    //红包数量
    private BigDecimal inputBounsNum = zeroBigDecimal;
    //钱包余额 单位：coinType
    private BigDecimal walletBalanceBigDecimal = zeroBigDecimal;
    //币种单价 单位：美元
    private BigDecimal coinPrice = zeroBigDecimal;
    //红包最小金额
    private BigDecimal minPrice = zeroBigDecimal;

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActWelcomeBonusFirstBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvInviteLevelTitle.setText(LocaleController.getString("act_welcome_bouns_frist_title", R.string.act_welcome_bouns_frist_title));
        binding.tvRedPacketTitle.setText(LocaleController.getString("dialog_send_redpackets_packet_num", R.string.dialog_send_redpackets_packet_num));
        binding.tvWelcomeNext.setText(LocaleController.getString("act_welcome_bouns_frist_sendbonus", R.string.act_welcome_bouns_frist_sendbonus));
        binding.etInputNum.setHint(LocaleController.getString("act_welcome_bouns_frist_et1", R.string.act_welcome_bouns_frist_et1));
        binding.etInputRedpacketNum.setHint(LocaleController.getString("act_welcome_bouns_frist_et2", R.string.act_welcome_bouns_frist_et2));

        String tipOriginalStr = LocaleController.getString("act_welcome_bonus_first_error_default_tips", R.string.act_welcome_bonus_first_error_default_tips);
        try {
            List<String> tipsList = List.of(tipOriginalStr.split("\\|"));
            SpanUtils.with(binding.tvTips)
                    .append(tipsList.get(0))
                    .append(tipsList.get(1)).setBold()
                    .append(tipsList.get(2))
                    .create();
        } catch (Exception e) {
            binding.tvTips.setText(tipOriginalStr);
        }

        //切换链点击事件
        binding.tvChaintype.setOnClickListener(view -> {
            if (mSendRedpacketSelectorChainTypeDialog == null) {
                mSendRedpacketSelectorChainTypeDialog = new SendRedpacketSelectorChainTypeDialog(getContext(), this::setChainTypeUi);
            }
            mSendRedpacketSelectorChainTypeDialog.setCurrentChainType(currentChainType, bonusConfigEntity.getTokens());
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

        binding.ivWelcomeBack.setOnClickListener(view -> finishFragment());
        binding.tvWelcomeNext.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bonusConfig", bonusConfigEntity);
            bundle.putString("toAddress", bonusConfigEntity.getAddress());
            bundle.putString("totalAmount", totalAmount.toPlainString());
            bundle.putSerializable("selectorCoinData", selectorCoinData);
            bundle.putString("amount", inputBounsAmount.toPlainString());
            bundle.putString("num", inputBounsNum.toPlainString());
            bundle.putString("chainName", currentChainType.getName());
            bundle.putString("gasFee", totalGasfee.toPlainString());
            bundle.putLong("coinId", coinId);
            presentFragment(new WelcomeBonusSecondAct(bundle));
        });
        binding.tvWelcomeNext.setClickable(false);
    }

    private void initData() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.llWelcomeTop.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;

        //获取钱包地址
        ownWalletAddress = WalletDaoUtils.getCurrent().getAddress();

        binding.llBonusData.setVisibility(View.INVISIBLE);
        getPromotionBonusConfig();
    }

    private void getPromotionBonusConfig() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new PromotionBonusConfigApi())
                .request(new OnHttpListener<BaseBean<PromotionBonusConfigEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<PromotionBonusConfigEntity> result) {
                        bonusConfigEntity = result.getData();
                        //设置链数据
                        setChainTypeUi(bonusConfigEntity.getTokens().get(0));
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }

                });
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
        //每次切换链的时候都要获取这个链下面的主币账户信息，以及钱包下所有的代币信息
        WalletUtil.requestWalletCoinBalance(ownWalletAddress, currentChainType.getId(), allCoinList, () -> {
            //清空一些数据
            walletBalanceBigDecimal = zeroBigDecimal;
            coinPrice = zeroBigDecimal;
            totalGasfee = zeroBigDecimal;
            totalAmount = zeroBigDecimal;
            inputBounsNum = zeroBigDecimal;
            minPrice = zeroBigDecimal;

            if (!CollectionUtils.isEmpty(allCoinList)) {
                MyCoinListData checkData = allCoinList.get(0);
                if (checkData.getChainId() == currentChainType.getId()) {
                    coinList.clear();
                    for (BonusConfigEntity.Currency currency : currentChainType.getCurrency()) {
                        //添加所有服务端配置的币
                        boolean has = false;
                        if (!allCoinList.isEmpty()) {
                            for (MyCoinListData myCoinListData : allCoinList) {
                                if (myCoinListData.getSymbol().equals(currency.getName())) {
                                    coinList.add(myCoinListData);
                                    has = true;
                                    break;
                                }
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

                    binding.llBonusData.setVisibility(View.VISIBLE);
                    binding.animationView.cancelAnimation();
                    binding.animationView.setVisibility(View.GONE);
                }
            } else {
                coinList.clear();
                selectorCoinInfo(new MyCoinListData());
                binding.llBonusData.setVisibility(View.VISIBLE);
                binding.animationView.cancelAnimation();
                binding.animationView.setVisibility(View.GONE);
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
        binding.tvTotal.setText(priceCoinType(zeroBigDecimal));
        binding.tvTotalDoller.setText(WalletUtil.toCoinPriceUSD(zeroBigDecimal, coinPrice, 6));

        //筛选gas费
        for (BonusConfigEntity.Currency currency : currentChainType.getCurrency()) {
            if (mMyCoinListData.getSymbol().equals(currency.getName())) {
                //币种ID
                coinId = currency.getId();
                //获取gas费
                gasfee = new BigDecimal(String.valueOf(currency.getGas_price()));
                //红包最小金额
                minPrice = new BigDecimal(StringUtils.isEmpty(currency.getMin_price()) ? "0" : currency.getMin_price());
            }
        }

        //当前币种账户余额
        binding.tvTopWalletBalance.setText(priceCoinType(walletBalanceBigDecimal));

        //币种 文字
        binding.tvCoinType.setText(mMyCoinListData.getSymbol());
        //币种 图标
        if (!TextUtils.isEmpty(mMyCoinListData.getIcon())) {
            GlideHelper.getDrawableGlide(binding.getRoot().getContext(), mMyCoinListData.getIcon(), drawable -> {
                binding.tvCoinType.getHelper().setIconNormalLeft(drawable);
            });
        } else {
            if (mMyCoinListData.getIconRes() != 0) {
                binding.tvCoinType.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(mMyCoinListData.getIconRes()));
            } else {
                binding.tvCoinType.getHelper().setIconNormalLeft(null);
            }
        }
    }

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

        //数量
        String numStr = binding.etInputRedpacketNum.getText().toString().trim();
        try {
            inputBounsNum = new BigDecimal(numStr);
        } catch (Exception e) {
            inputBounsNum = zeroBigDecimal;
        }

        //gas费总额
        totalGasfee = inputBounsNum.multiply(gasfee);
        //输入的金额+gas总费用=红包总金额
        totalAmount = inputBounsAmount.add(totalGasfee);
        //消费总额
        binding.tvTotal.setText(priceCoinType(totalAmount));
        binding.tvTotalDoller.setText(WalletUtil.toCoinPriceUSD(totalAmount, coinPrice, 6));

        if (WalletUtil.decimalCompareTo(inputBounsAmount, zeroBigDecimal) && WalletUtil.decimalCompareTo(inputBounsNum, zeroBigDecimal)) {
            changeNextBtnStatus(
                    WalletUtil.decimalCompareTo(walletBalanceBigDecimal, totalAmount)
                            && WalletUtil.decimalCompareTo(inputBounsAmount.divide(inputBounsNum, 6, RoundingMode.UP), minPrice)
            );

            if (WalletUtil.decimalCompareTo(walletBalanceBigDecimal, totalAmount)) {
                if (WalletUtil.decimalCompareTo(inputBounsAmount.divide(inputBounsNum, 6, RoundingMode.UP), minPrice)) {
                    showErrorTipsView("");
                } else {
                    showErrorTipsView(String.format(LocaleController.getString("dialog_send_redpackets_error_tips2", R.string.dialog_send_redpackets_error_tips2), priceCoinType(minPrice)));
                }
            } else {
                showErrorTipsView(LocaleController.getString("chat_transfer_input_price_tips1", R.string.chat_transfer_input_price_tips1));
            }
        } else {
            changeNextBtnStatus(false);
        }
    }


    /**
     * 修改下一步按钮状态，是否可以点击
     *
     * @param isClickable
     */
    private void changeNextBtnStatus(boolean isClickable) {
        binding.tvWelcomeNext.setClickable(isClickable);
        if (isClickable) {
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
        }
    }

    /**
     * 显示错误提示
     *
     * @param content
     */
    private void showErrorTipsView(String content) {
        if (content.isEmpty()) {
            binding.tvErrorTip.setVisibility(View.GONE);
            binding.ivArrow.setVisibility(View.GONE);
        } else {
            binding.ivArrow.setVisibility(View.VISIBLE);
            binding.tvErrorTip.setVisibility(View.VISIBLE);
        }

        binding.tvErrorTip.setText(content);
    }

    /**
     * BigDecimal转String 增加币种单位
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
}
