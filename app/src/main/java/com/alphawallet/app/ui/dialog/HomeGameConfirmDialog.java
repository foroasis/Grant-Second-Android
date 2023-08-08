package teleblock.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogHomeGameConfirmBinding;
import org.telegram.ui.LaunchActivity;

import java.util.HashMap;

import okhttp3.Call;
import teleblock.blockchain.BlockchainConfig;
import teleblock.model.HomeGameConfigEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.JumpGamePayApi;
import teleblock.ui.adapter.HomeGameAmountAdapter;
import teleblock.util.EventUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/3/31
 * Author:Perry
 * Description：首页游戏确认支付页面
 */
public class HomeGameConfirmDialog extends BaseBottomSheetDialog {

    private DialogHomeGameConfirmBinding binding;
    private HomeGameAmountAdapter mHomeGameAmountAdapter;

    private String gameNumber;
    private HomeGameConfigEntity mHomeGameConfigEntity;
    private HomeGameConfigEntity.Tokens token;
    private Web3ConfigEntity.WalletNetworkConfigChainType chainType;
    private int times;

    private String currentAddress;

    private Runnable runnable;

    public HomeGameConfirmDialog(
            @NonNull Context context,
            String gameNumber,
            HomeGameConfigEntity mHomeGameConfigEntity,
            Runnable runnable
    ) {
        super(context);
        this.gameNumber = gameNumber;
        this.mHomeGameConfigEntity = mHomeGameConfigEntity;
        this.runnable = runnable;
        times = mHomeGameConfigEntity.getTimes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogHomeGameConfirmBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        currentAddress = WalletDaoUtils.getCurrent().getAddress();
        token = (HomeGameConfigEntity.Tokens) CollectionUtils.get(mHomeGameConfigEntity.getTokens(), 0);

        binding.rvBetAmount.setLayoutManager(new LinearLayoutManager(getContext()));
        mHomeGameAmountAdapter = new HomeGameAmountAdapter();
        binding.rvBetAmount.setAdapter(mHomeGameAmountAdapter);
        mHomeGameAmountAdapter.setData(mHomeGameConfigEntity.getTokens(), token);

        binding.tvBetConfirm.setOnClickListener(v -> {
            if (times > 0) {
                runnable.run();
                dismiss();
            } else {
                if (mHomeGameConfigEntity.getDaily_pay_times() >= mHomeGameConfigEntity.getDaily_max_pay_times()) {
                    ToastUtils.showLong(LocaleController.getString("dialog_home_game_pay_max_count_tips", R.string.dialog_home_game_pay_max_count_tips));
                    return;
                }
                betAmount();
            }
        });

        binding.tvDiceBetTitle.setText(LocaleController.getString("jump_game_title", R.string.jump_game_title));
        binding.tvChainTypeTitle.setText(LocaleController.getString("dice_bet_confirm_chain_type_title", R.string.dice_bet_confirm_chain_type_title));
        binding.tvPayAmountTitle.setText(LocaleController.getString("dice_bet_confirm_pay_amount_title", R.string.dice_bet_confirm_pay_amount_title));
        binding.tvTotalAmountTitle.setText(LocaleController.getString("dice_bet_confirm_total_amount_title", R.string.dice_bet_confirm_total_amount_title));

        if (times > 0) {
            binding.llFree.setVisibility(View.VISIBLE);
            binding.llPayContent.setVisibility(View.GONE);
            binding.tvRulesHeader.setText(mHomeGameConfigEntity.getRules().getHeader());
            GlideHelper.displayImage(getContext(), binding.ivRulesImage, mHomeGameConfigEntity.getRules().getImage());
            binding.tvRulesFooter.setText(mHomeGameConfigEntity.getRules().getFooter());

            String betConfirmTx = String.format(LocaleController.getString("game_free_play", R.string.game_free_play), times, mHomeGameConfigEntity.getFree_times());
            binding.tvBetConfirm.setText(betConfirmTx);

        } else {
            binding.llFree.setVisibility(View.GONE);
            binding.llPayContent.setVisibility(View.VISIBLE);
            updateInfo();
            binding.tvBetConfirm.setText(LocaleController.getString("dice_bet_confirm_confirm", R.string.dice_bet_confirm_confirm));
            EventUtil.track(getContext(), EventUtil.Even.小游戏付款弹窗展示, new HashMap<>());
        }

        mHomeGameAmountAdapter.setOnItemClickListener((adapter, view, position) -> {
            token = mHomeGameAmountAdapter.getItem(position);
            mHomeGameAmountAdapter.setToken(token);
            updateInfo();
        });
    }

    private void updateInfo() {
        chainType = BlockchainConfig.getChainType(token.getChain_id());
        binding.tvChainType.setText(chainType.getName());
        binding.tvPayAmount.setText(token.getAmount() + " " + token.getName());
        binding.tvTotalAmount.setText(token.getAmount() + " " + token.getName());
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
                        currentAddress = wallet.getAddress();
                    }
                }.show();

                ToastUtils.showLong(LocaleController.getString("common_wallet_switch_evm_address", R.string.common_wallet_switch_evm_address));
                return;
            }
        }

        TransferLoadDialog.showLoading(getContext(), LocaleController.getString("transfer_pay_loading", R.string.transfer_pay_loading));
        WalletTransferUtil.sendTransaction(token.getChain_id(), mHomeGameConfigEntity.getAddress(), null, null, token.getContract_address(),
                token.getAmount(), token.getDecimal(), token.getName(), new WalletUtil.SendTransactionListener() {
                    @Override
                    public void paySuccessful(String hash) {
                        EasyHttp.post(new ApplicationLifecycle())
                                .api(new JumpGamePayApi()
                                        .setGame_number(gameNumber)
                                        .setPayment_account(currentAddress)
                                        .setTx_hash(hash)
                                        .setChain_id(String.valueOf(token.getChain_id()))
                                        .setCurrency_id(String.valueOf(token.getId()))
                                        .setAmount(token.getAmount())
                                ).request(new OnHttpListener<BaseBean<Object>>() {
                                    @Override
                                    public void onSucceed(BaseBean<Object> result) {
                                        if (result.getCode() == 12001) {
                                            ToastUtils.showLong(result.getMessage());
                                            TransferLoadDialog.stopLoading();
                                            dismiss();
                                        } else if (result.getCode() == 200) {
                                            TelegramUtil.queryCenterTxStatus(hash, () -> {
                                                runnable.run();
                                                TransferLoadDialog.stopLoading();
                                                dismiss();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFail(Exception e) {
                                        ToastUtils.showLong(e.getMessage());
                                        TransferLoadDialog.stopLoading();
                                        dismiss();
                                    }

                                    @Override
                                    public void onEnd(Call call) {
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

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(50));
        getWindow().setGravity(Gravity.BOTTOM);
        resetPeekHeight();
    }
}