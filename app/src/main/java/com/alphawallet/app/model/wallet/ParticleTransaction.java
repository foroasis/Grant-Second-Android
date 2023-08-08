package teleblock.model.wallet;

import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

import teleblock.util.TGLog;

/**
 * Time:2022/11/28
 * Author:Perry
 * Description：转账记录
 */
public class ParticleTransaction {
    private String chainId;
    private String hash;
    private String from;
    private String to;
    private String value;
    private String gasLimit;
    private String gasSpent;
    private String gasPrice;
    private String fees;
    private int status;
    private long timestamp;
    private String nonce;
    private String data;

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasSpent() {
        return gasSpent;
    }

    public void setGasSpent(String gasSpent) {
        this.gasSpent = gasSpent;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static List<TransactionInfo> parse(List<ParticleTransaction> result) {
        List<TransactionInfo> list = new ArrayList<>();
        try {
            if (result == null) return list;
            for (ParticleTransaction info : result) {
                TransactionInfo transaction = new TransactionInfo();
                transaction.hash = info.getHash();
//                            transaction.blockHash = null == info.blockHash ? "" : info.blockHash;
                transaction.from = info.getFrom();
                transaction.to = info.getTo();
//                            transaction.blockNumber = info.blockNumber;
                transaction.timestamp = String.valueOf(info.getTimestamp());
                transaction.value = Numeric.toBigInt(info.getValue()).toString();
//                            transaction.gasPrice = info.getGasPrice();
//                            transaction.gas = info.gas;
//                            transaction.contractAddress = info.contractAddress;
//                            transaction.tokenSymbol = info.tokenSymbol;
//                            transaction.tokenDecimal = info.tokenDecimal;
//                            transaction.isError = !"0".equals(info.isError);

                TGLog.erro("value：" + info.getValue() + "====" + "data：" + info.getData());
                list.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
