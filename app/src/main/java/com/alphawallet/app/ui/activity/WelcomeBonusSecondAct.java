package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ActivityWelcomeBonusSecondBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.math.BigDecimal;
import java.util.HashMap;

import okhttp3.Call;
import teleblock.model.CreateBonusEntity;
import teleblock.model.PromotionBonusConfigEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.ui.MyCoinListData;
import teleblock.network.BaseBean;
import teleblock.network.api.CreatePromotionBonusApi;
import teleblock.ui.dialog.SendRedPacketSuccessfulDialog;
import teleblock.ui.dialog.TransferErrorDialog;
import teleblock.ui.dialog.TransferLoadDialog;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.util.parse.PromotionBounsParseUtil;
import teleblock.widget.GlideHelper;

/**
 * 欢迎红包-第二步
 */
public class WelcomeBonusSecondAct extends BaseFragment {

    private ActivityWelcomeBonusSecondBinding binding;

    //传递过来的数据
    private String toAddress;
    private String totalAmount;
    private String amount;
    private String num;
    private String chainName;
    private String gasFee;
    private long coinId;
    private MyCoinListData selectorCoinData;
    private PromotionBonusConfigEntity bonusConfigEntity;
    //群相关
    private long groupId;
    private String groupLink;

    //交易成功弹窗
    private SendRedPacketSuccessfulDialog mSendRedPacketSuccessfulDialog;

    public WelcomeBonusSecondAct(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivityWelcomeBonusSecondBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.ivInviteBack.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;

        binding.tvInviteLevelTitle.setText(LocaleController.getString("act_welcome_bouns_second_title", R.string.act_welcome_bouns_second_title));
        binding.tvGroupTitle.setText(LocaleController.getString("act_welcome_bouns_second_group", R.string.act_welcome_bouns_second_group));
        binding.tvInviteNext.setText(LocaleController.getString("act_welcome_bonus_send", R.string.act_welcome_bonus_send));
        binding.ivInviteBack.setOnClickListener(view -> finishFragment());

        binding.tvInviteNext.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.欢迎红包发送按钮点击, new HashMap<>());
            TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading));
            //先进行支付 然后调用接口创建红包 然后查询上链状态 然后在发消息
            WalletTransferUtil.sendTransaction(selectorCoinData.getChainId(),
                    toAddress, null, null,
                    selectorCoinData.getContractAddress(),
                    totalAmount,
                    selectorCoinData.getDecimal(),
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
        });
    }

    private void initData() {
        bonusConfigEntity = (PromotionBonusConfigEntity) getArguments().getSerializable("bonusConfig");
        toAddress = getArguments().getString("toAddress");
        totalAmount = getArguments().getString("totalAmount");
        selectorCoinData = (MyCoinListData) getArguments().getSerializable("selectorCoinData");
        amount = getArguments().getString("amount");
        num = getArguments().getString("num");
        chainName = getArguments().getString("chainName");
        gasFee = getArguments().getString("gasFee");
        coinId = getArguments().getLong("coinId");

        PromotionBonusConfigEntity.ChatInfo chat_info = bonusConfigEntity.getChat_info();
        if (chat_info != null) {
            groupId = chat_info.getId();
            groupLink = chat_info.getLink();
            GlideHelper.displayImage(getContext(), binding.ivAvatar, chat_info.getImage());
            binding.tvGroupName.setText(chat_info.getTitle());
        }
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
            if (chainType.getId() == selectorCoinData.getChainId()) {
                explorerUrl = chainType.getExplorer_url();
            }
        }

        //初始化交易成功弹窗
        String url = explorerUrl + (selectorCoinData.getChainId() == 999 ? "/transaction/" : "/tx/") + hash;
        mSendRedPacketSuccessfulDialog = new SendRedPacketSuccessfulDialog(getContext(), selectorCoinData, new BigDecimal(totalAmount), url, () -> finishFragment());

        //上报服务器
        CreatePromotionBonusApi createBonusApi = new CreatePromotionBonusApi();
        createBonusApi.setTx_hash(hash);
        createBonusApi.setAmount(amount);
        createBonusApi.setNum(num);
        createBonusApi.setChain_id(selectorCoinData.getChainId());
        createBonusApi.setChain_name(chainName);
        createBonusApi.setCurrency_id(coinId);
        createBonusApi.setCurrency_name(selectorCoinData.getSymbol());
        createBonusApi.setPayment_account(WalletDaoUtils.getCurrent().getAddress());
        createBonusApi.setGas_amount(gasFee);
        createBonusApi.setSource(1);
        createBonusApi.setTg_group_id(String.valueOf(groupId));
        createBonusApi.setTg_group_link(groupLink);
        EasyHttp.post(new ApplicationLifecycle())
                .api(createBonusApi)
                .request(new OnHttpListener<BaseBean<CreateBonusEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<CreateBonusEntity> data) {
                        mSendRedPacketSuccessfulDialog.show();
                        TelegramUtil.queryCenterTxStatus(hash, () -> {
                            String str = PromotionBounsParseUtil.setParseStr(
                                    -groupId,
                                    UserConfig.getInstance(getCurrentAccount()).getClientUserId(),
                                    data.getData().getSecret_num(),
                                    selectorCoinData.getChainId(),
                                    chainName,
                                    amount, num,
                                    selectorCoinData.getSymbol()
                            );

                            TLRPC.Chat groupChat = getMessagesController().getChat(groupId);
                            if (ChatObject.isNotInChat(groupChat)) {
                                TelegramUtil.addJoinToChat(groupLink, () -> {
                                    sendMsgOrFinish(str);
                                });
                            } else {
                                sendMsgOrFinish(str);
                            }
                        });
                    }

                    @Override
                    public void onFail(Exception e) {
                        new TransferErrorDialog(getContext(), e.getMessage()).show();
                    }

                    @Override
                    public void onEnd(Call call) {
                        TransferLoadDialog.stopLoading();
                    }
                });
    }


    private void sendMsgOrFinish(String str) {
        mSendRedPacketSuccessfulDialog.dismiss();
        getSendMessagesHelper().sendMessage(str, -groupId, null, null, null, true, null, null, null, true, 0, null,false);
        if (getParentLayout() != null && getParentLayout().getFragmentStack() != null) {
            getParentLayout().removeFragmentFromStack(getParentLayout().getFragmentStack().size() - 2);
        }
        finishFragment();
    }
}