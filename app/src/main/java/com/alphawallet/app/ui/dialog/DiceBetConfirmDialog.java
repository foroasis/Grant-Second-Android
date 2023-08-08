package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogDiceBetConfirmBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ChatActivity;

import okhttp3.Call;
import teleblock.blockchain.BlockchainConfig;
import teleblock.model.DiceConfigEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.DiceConfigApi;
import teleblock.network.api.DiceCreateApi;
import teleblock.ui.adapter.DiceAmountAdapter;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;

/**
 * 创建日期：2023/3/9
 * 描述：骰子下注确认
 */
public class DiceBetConfirmDialog extends BaseBottomSheetDialog implements OnItemClickListener {

    private DialogDiceBetConfirmBinding binding;
    private ChatActivity chatActivity;
    private ETHWallet currentWallet;
    private DiceConfigEntity diceConfig;
    private DiceAmountAdapter diceAmountAdapter;
    private DiceConfigEntity.Tokens token;
    private Web3ConfigEntity.WalletNetworkConfigChainType chainType;

    public DiceBetConfirmDialog(ChatActivity chatActivity, ETHWallet currentWallet, DiceConfigEntity diceConfig) {
        super(chatActivity.getContext());
        this.chatActivity = chatActivity;
        this.currentWallet = currentWallet;
        this.diceConfig = diceConfig;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogDiceBetConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
        initData();
    }

    private void initView() {
        binding.rvBetAmount.setLayoutManager(new LinearLayoutManager(getContext()));
        diceAmountAdapter = new DiceAmountAdapter();
        diceAmountAdapter.setOnItemClickListener(this);
        binding.rvBetAmount.setAdapter(diceAmountAdapter);

        binding.tvBetConfirm.setOnClickListener(v -> betAmount());

        binding.tvDiceBetTitle.setText(LocaleController.getString("dice_bet_confirm_title", R.string.dice_bet_confirm_title));
        binding.tvDiceBetRuleTitle.setText(LocaleController.getString("dice_bet_confirm_rule_title", R.string.dice_bet_confirm_rule_title));
        binding.tvDiceBetRule.setText(LocaleController.getString("dice_bet_confirm_rule_text", R.string.dice_bet_confirm_rule_text));
        binding.tvChainTypeTitle.setText(LocaleController.getString("dice_bet_confirm_chain_type_title", R.string.dice_bet_confirm_chain_type_title));
        binding.tvPayAmountTitle.setText(LocaleController.getString("dice_bet_confirm_pay_amount_title", R.string.dice_bet_confirm_pay_amount_title));
        binding.tvTotalAmountTitle.setText(LocaleController.getString("dice_bet_confirm_total_amount_title", R.string.dice_bet_confirm_total_amount_title));
        binding.tvBetConfirm.setText(LocaleController.getString("dice_bet_confirm_confirm", R.string.dice_bet_confirm_confirm));
    }

    private void initData() {
        token = (DiceConfigEntity.Tokens) CollectionUtils.get(diceConfig.tokens, 0);
        diceAmountAdapter.setData(diceConfig.tokens, token);
        updateInfo();
    }

    private void updateInfo() {
        chainType = BlockchainConfig.getChainType(token.chain_id);
        binding.tvChainType.setText(chainType.getName());
        binding.tvPayAmount.setText(token.amount + " " + token.name);
        binding.tvTotalAmount.setText(token.amount + " " + token.name);
    }

    private void betAmount() {
        if (currentWallet.getChainId() == 99999) {
            new WalletListDialog(chatActivity) {
                @Override
                public void onItemClick(ETHWallet wallet) {
                    currentWallet = wallet;
                }
            }.show();
            ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
            return;
        }

        TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading));
        WalletTransferUtil.sendTransaction(token.chain_id, diceConfig.address, null, null, token.contract_address,
                token.amount, token.decimal, token.name, new WalletUtil.SendTransactionListener() {
                    @Override
                    public void paySuccessful(String hash) {
                        TransferLoadDialog.stopLoading();
                        String tg_group_id = "";
                        String tg_group_link = "";
                        if (chatActivity.getCurrentChat() != null) {
                            tg_group_id = chatActivity.getCurrentChat().id + "";
                        }
                        if (ChatObject.getPublicUsername(chatActivity.getCurrentChat()) != null) {
                            tg_group_link = "https://t.me/" + ChatObject.getPublicUsername(chatActivity.getCurrentChat());
                        }
                        EasyHttp.post(new ApplicationLifecycle())
                                .api(new DiceCreateApi()
                                        .setTx_hash(hash)
                                        .setAmount(token.amount)
                                        .setPayment_account(currentWallet.getAddress())
                                        .setChain_id(token.chain_id + "")
                                        .setChain_name(chainType.getName())
                                        .setCurrency_id(token.id + "")
                                        .setCurrency_name(token.name)
                                        .setTg_group_id(tg_group_id)
                                        .setTg_group_link(tg_group_link))
                                .request(null);
                        new DiceBetResultDialog(chatActivity, hash).show();
                        dismiss();
                    }

                    @Override
                    public void payError(String error) {
                        TransferLoadDialog.stopLoading();
                        new TransferErrorDialog(getContext(), error).show();
                    }
                });
    }

    public static void showDialog(ChatActivity fragment, ETHWallet wallet) {
        AlertDialog progressDialog = new AlertDialog(fragment.getContext(), 3);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        EasyHttp.post(new ApplicationLifecycle())
                .api(new DiceConfigApi())
                .request(new OnHttpListener<BaseBean<DiceConfigEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<DiceConfigEntity> result) {
                        new DiceBetConfirmDialog(fragment, wallet, result.getData()).show();
                    }

                    @Override
                    public void onFail(Exception e) {

                    }

                    @Override
                    public void onEnd(Call call) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        token = diceAmountAdapter.getItem(position);
        diceAmountAdapter.setToken(token);
        updateInfo();
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(50));
        getWindow().setGravity(Gravity.BOTTOM);
        resetPeekHeight();
    }
}