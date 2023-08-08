package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogTransferDetailsBinding;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.WCSessionManager;
import teleblock.model.Web3ConfigEntity;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.StringUtil;
import teleblock.util.TGLog;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：转账详情
 */
public class TransferDetatilsDialog extends Dialog {
    private DialogTransferDetailsBinding binding;

    public TransferDetatilsDialog(@NonNull Context context, long messageTime, List<String> valus) {
        super(context, R.style.dialog2);
        setCancelable(true);
        binding = DialogTransferDetailsBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        binding.tvTitle.setOnClickListener(view -> dismiss());

        binding.tvTitle.setText(LocaleController.getString("transfer_detatils_dialog_title", R.string.transfer_detatils_dialog_title));
        binding.tvStatusTitle.setText(LocaleController.getString("transfer_detatils_dialog_status", R.string.transfer_detatils_dialog_status));
        binding.tvStatus.setText(LocaleController.getString("transfer_detatils_dialog_status1", R.string.transfer_detatils_dialog_status1));
        binding.tvNumTitle.setText(LocaleController.getString("transfer_detatils_dialog_num", R.string.transfer_detatils_dialog_num));
        binding.tvGasFeeTitle.setText(LocaleController.getString("transfer_detatils_dialog_gasfee", R.string.transfer_detatils_dialog_gasfee));
        binding.tvTotalNumTitle.setText(LocaleController.getString("transfer_detatils_dialog_totalnum", R.string.transfer_detatils_dialog_totalnum));

        SimpleDateFormat formatter = new SimpleDateFormat(LocaleController.getString("dateformat3", R.string.dateformat3));
        String formatDate = formatter.format(messageTime * 1000);
        binding.tvTime.setText(formatDate);

        List<Web3ConfigEntity.WalletNetworkConfigChainType> mWalletNetworkConfigChainTypeList = MMKVUtil.getWeb3ConfigData().getChainType();
        Web3ConfigEntity.WalletNetworkConfigChainType selectorChainData = null;
        for (Web3ConfigEntity.WalletNetworkConfigChainType chainTypeData : mWalletNetworkConfigChainTypeList) {
            if (valus.get(11).equals(String.valueOf(chainTypeData.getId()))) {
                selectorChainData = chainTypeData;
            }
        }

        binding.tvTransferForm.setText(WalletUtil.formatAddress(valus.get(5)));
        binding.tvTransferTo.setText(WalletUtil.formatAddress(valus.get(6)));
        binding.tvNum.setText(WalletUtil.bigDecimalScale(new BigDecimal(valus.get(7)), 6).toPlainString() + " " + valus.get(3));
        binding.tvTotalNum.setText(WalletUtil.bigDecimalScale(new BigDecimal(valus.get(9)), 6).toPlainString() + " " + valus.get(3));
        binding.tvNumDollar.setText( "$" + WalletUtil.bigDecimalScale(new BigDecimal(valus.get(10)), 6).toPlainString());

        if (selectorChainData != null) {
            binding.tvGasFee.setText(WalletUtil.bigDecimalScale(new BigDecimal(valus.get(8)), 6).toPlainString() + " " + selectorChainData.getMain_currency_name());
            BlockFactory.get(selectorChainData.getId()).getTransactionByHash(valus.get(4), new BlockCallback<EthTransaction>() {
                @Override
                public void onSuccess(EthTransaction data) {
                    Transaction transaction = data.getResult();
                    if (transaction != null) {
                        binding.tvNonce.setText("#" + transaction.getNonce());
                    }
                }

                @Override
                public void onError(String msg) {
                }
            });

            binding.tvGotoEtherscan.setText(String.format(
                            LocaleController.getString("transfer_detatils_dialog_goto_etherscan", R.string.transfer_detatils_dialog_goto_etherscan)
                            , selectorChainData.getName()
                    )
            );

            String url = selectorChainData.getExplorer_url();
            binding.tvGotoEtherscan.setOnClickListener(view -> {
                dismiss();
                String hash = valus.get(4);
                if (WalletUtil.isSolanaAddress(valus.get(5))) {
                    Browser.openUrl(getContext(), "https://solscan.io/tx/" + hash);
                } else {
                    Browser.openUrl(getContext(), url + "/tx/" +  hash);
                }
            });

        }
    }
}
