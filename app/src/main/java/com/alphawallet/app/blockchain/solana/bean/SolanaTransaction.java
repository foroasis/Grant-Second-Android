package teleblock.blockchain.solana.bean;

import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.ParticleTransaction;
import teleblock.model.wallet.TransactionInfo;
import teleblock.util.TGLog;

/**
 * 创建日期：2023/5/11
 * 描述：
 */
public class SolanaTransaction {
    public String type;
    public int lamportsChange;
    public int lamportsFee;
    public String signature;
    public int blockTime;
    public String status;
    public int preBalance;
    public int postBalance;
    public Data data;

    public static class Data {
        public int lamportsTransfered;
        public String sender;
        public String receiver;
    }


    public static List<TransactionInfo> parse(List<SolanaTransaction> result) {
        List<TransactionInfo> list = new ArrayList<>();
        if (result == null) return list;

        for (SolanaTransaction info : result) {
            TransactionInfo transaction;
            try {
                transaction = new TransactionInfo();
                transaction.hash = info.signature;
                transaction.from = info.data.sender;
                transaction.to = info.data.receiver;
                transaction.timestamp = String.valueOf(info.blockTime);
                transaction.value = String.valueOf(Math.abs(info.lamportsChange));
//                            transaction.gasPrice = info.getGasPrice();
//                            transaction.gas = info.gas;
//                            transaction.contractAddress = info.contractAddress;
//                            transaction.tokenSymbol = info.tokenSymbol;
//                            transaction.tokenDecimal = info.tokenDecimal;
                transaction.isError = !"success".equals(info.status);
                if (transaction.from == null || transaction.to == null) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            list.add(transaction);
        }
        return list;
    }
}