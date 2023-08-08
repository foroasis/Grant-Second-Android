package teleblock.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogGameBetConfirmBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.config.GameType;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BetStatusEntity;
import teleblock.model.GameInfoEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.BetStatusApi;
import teleblock.network.api.GameBetApi;
import teleblock.util.WalletTransferUtil;
import teleblock.util.parse.GameParseUtil;
import teleblock.ui.adapter.BetAmountAdapter;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * 游戏详情—确认下注
 */
public class GameBetConfirmDialog extends BaseBottomSheetDialog implements RadioGroup.OnCheckedChangeListener, OnItemClickListener {

    private DialogGameBetConfirmBinding binding;
    private GameInfoEntity gameInfo;
    private long dialogId;
    private BetAmountAdapter betAmountAdapter;
    private int bet_amount; // 押注金额
    private int bet_double = 1; // 押注倍数
    private String address;
    private AlertDialog progressDialog;

    public GameBetConfirmDialog(Context context, GameInfoEntity gameInfo, long dialogId) {
        super(context);
        this.gameInfo = gameInfo;
        this.dialogId = dialogId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameBetConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
        initData();
    }

    private void initView() {
        binding.rgBetType.setOnCheckedChangeListener(this);
        binding.rvBetAmount.setLayoutManager(new GridLayoutManager(getContext(), 4));
        betAmountAdapter = new BetAmountAdapter(gameInfo);
        betAmountAdapter.setOnItemClickListener(this);
        binding.rvBetAmount.setAdapter(betAmountAdapter);
        binding.tvBetDouble.setOnClickListener(v ->
                new GameBetDoubleDialog(getContext(), bet_double, gameInfo.multiple_range, (BaseAlertDialog.OnCloseListener<String>) data -> {
                    bet_double = Integer.parseInt(data);
                    initData();
                }).show());
        binding.tvBetConfirm.setOnClickListener(v -> betAmount());

        binding.tvBetTypeTitle.setText(LocaleController.getString("game_bet_confirm_bet_type_title", R.string.game_bet_confirm_bet_type_title));
        binding.rbBetOdd.setText(LocaleController.getString("game_bet_confirm_bet_type_odd", R.string.game_bet_confirm_bet_type_odd));
        binding.rbBetEven.setText(LocaleController.getString("game_bet_confirm_bet_type_even", R.string.game_bet_confirm_bet_type_even));
        binding.tvBetAmountTitle.setText(LocaleController.getString("game_bet_confirm_bet_amount_title", R.string.game_bet_confirm_bet_amount_title));
        binding.tvBetDoubleTitle.setText(LocaleController.getString("game_bet_confirm_bet_double_title", R.string.game_bet_confirm_bet_double_title));
        binding.tvChainTypeTitle.setText(LocaleController.getString("game_bet_confirm_chain_type_title", R.string.game_bet_confirm_chain_type_title));
        binding.tvOddEvenTitle.setText(LocaleController.getString("game_bet_confirm_odd_even_title", R.string.game_bet_confirm_odd_even_title));
        binding.tvOddEven.setText(LocaleController.getString("game_bet_confirm_odd_text", R.string.game_bet_confirm_odd_text));
        binding.tvTotalAmountTitle.setText(LocaleController.getString("game_bet_confirm_total_amount_title", R.string.game_bet_confirm_total_amount_title));
        binding.tvGameOddsTitle.setText(LocaleController.getString("game_bet_confirm_game_odds_title", R.string.game_bet_confirm_game_odds_title));
        binding.tvPayAmountTitle.setText(LocaleController.getString("game_bet_confirm_pay_amount_title", R.string.game_bet_confirm_pay_amount_title));
        binding.tvBetConfirm.setText(LocaleController.getString("game_bet_confirm_text", R.string.game_bet_confirm_text));
    }

    private void initData() {
        address = WalletDaoUtils.getCurrent().getAddress();
        binding.tvBetDouble.setText(String.format(LocaleController.getString("game_bet_confirm_bet_double_number", R.string.game_bet_confirm_bet_double_number), bet_double));
        bet_amount = betAmountAdapter.getAmount();
        binding.tvChainType.setText(gameInfo.chain_name);
        binding.tvBetAmount.setText(bet_amount + "");
        binding.tvTotalAmount.setText(bet_amount * bet_double + " " + gameInfo.currency_name);
        binding.tvGameOdds.setText(gameInfo.game_odds + "");
        binding.tvPayAmount.setText(bet_amount * bet_double + " " + gameInfo.currency_name);
    }

    private void betAmount() {
        ETHWallet ethWallet = WalletDaoUtils.getCurrent();
        if (ethWallet != null && ethWallet.getChainId() == 99999) {
            Activity activity = ActivityUtils.getTopActivity();
            if (activity instanceof LaunchActivity) {
                LaunchActivity launchActivity = (LaunchActivity) activity;
                new WalletListDialog(launchActivity.getActionBarLayout().getLastFragment()) {
                    @Override
                    public void onItemClick(ETHWallet wallet) {
                        address = wallet.getAddress();
                    }
                }.show();

                ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
                return;
            }
        }

        TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading));
        WalletTransferUtil.sendTransaction(gameInfo.chain_id, gameInfo.receipt_account, null, null, "",
                bet_amount * bet_double + "", 18, gameInfo.currency_name, new WalletUtil.SendTransactionListener() {
                    @Override
                    public void paySuccessful(String hash) {
                        TransferLoadDialog.updateLoading(LocaleController.getString("transfer_pay_successful", R.string.transfer_pay_successful));
                        EasyHttp.post(new ApplicationLifecycle())
                                .api(new GameBetApi()
                                        .setTx_hash(hash)
                                        .setGame_number(gameInfo.game_number)
                                        .setAmount(bet_amount * bet_double)
                                        .setPayment_account(address)
                                        .setBet_type(binding.rbBetOdd.isChecked() ? "odd" : "even"))
                                .request(new OnHttpListener<BaseBean<String>>() {
                                    @Override
                                    public void onSucceed(BaseBean<String> result) {
                                        progressDialog = new AlertDialog(getContext(), 3);
                                        progressDialog.setCanCancel(false);
                                        progressDialog.show();
                                        checkStatus(hash);
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

                    @Override
                    public void payError(String error) {
                        TransferLoadDialog.stopLoading();
                        new TransferErrorDialog(getContext(), error).show();
                    }
                });
    }

    private void checkStatus(String hash) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new BetStatusApi()
                        .setTx_hash(hash))
                .request(new OnHttpListener<BaseBean<BetStatusEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<BetStatusEntity> result) {
                        BetStatusEntity betStatusEntity = result.getData();
                        if (betStatusEntity == null || betStatusEntity.block_status == 3 || betStatusEntity.status == 4) {
                            onFail(new Exception("数据异常，等待退款"));
                            return;
                        }
                        if (betStatusEntity.status != 1) { // 已开奖
                            progressDialog.dismiss();
                            //加密数据
                            String str = GameParseUtil.setParseStr(
                                    GameType.HASH_SINGLE_DOUBLE,
                                    dialogId,
                                    UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId(),
                                    gameInfo.game_number,
                                    binding.rbBetOdd.isChecked() ? "odd" : "even",
                                    String.valueOf(bet_amount * bet_double),
                                    gameInfo.currency_name,
                                    betStatusEntity.pay_amount,
                                    betStatusEntity.status,
                                    betStatusEntity.block_hash
                            );
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.GAME_WIN, (Object) str));
                            dismiss();
                            return;
                        }
                        ThreadUtils.getMainHandler().postDelayed(() -> checkStatus(hash), 3000);
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_bet_odd:
                binding.tvOddEven.setText(LocaleController.getString("game_bet_confirm_odd_text", R.string.game_bet_confirm_odd_text));
                break;
            case R.id.rb_bet_even:
                binding.tvOddEven.setText(LocaleController.getString("game_bet_confirm_even_text", R.string.game_bet_confirm_even_text));
                break;
        }
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        bet_amount = betAmountAdapter.getItem(position);
        betAmountAdapter.setAmount(bet_amount);
        initData();
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(50));
        getWindow().setGravity(Gravity.BOTTOM);
        resetPeekHeight();
    }
}