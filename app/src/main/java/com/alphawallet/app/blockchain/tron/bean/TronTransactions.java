package teleblock.blockchain.tron.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.TransactionInfo;

/**
 * 创建日期：2022/12/28
 * 描述：
 */
public class TronTransactions {

    public int total;
    public int rangeTotal;
    public List<DataEntity> data;
    public long wholeChainTxCount;

    public static class DataEntity {
        public int block;
        public String hash;
        public long timestamp;
        public String ownerAddress;
        public List<String> toAddressList;
        public String toAddress;
        public int contractType;
        public boolean confirmed;
        public boolean revert;
        public ContractDataEntity contractData;
        @JsonProperty("SmartCalls")
        public String smartCalls;
        @JsonProperty("Events")
        public String events;
        public String id;
        public String data;
        public String fee;
        public String contractRet;
        public String result;
        public String amount;
        public CostEntity cost;
        public TokenInfoEntity tokenInfo;
        public String tokenType;
        public TriggerInfoEntity trigger_info;
        public String ownerAddressTag;

        public static class ContractDataEntity {
            public String data;
            public String owner_address;
            public String contract_address;
        }

        public static class CostEntity {
            public int net_fee;
            public int energy_usage;
            public int fee;
            public int energy_fee;
            public int energy_usage_total;
            public int origin_energy_usage;
            public int net_usage;
        }

        public static class TokenInfoEntity {
            public String tokenId;
            public String tokenAbbr;
            public String tokenName;
            public int tokenDecimal;
            public int tokenCanShow;
            public String tokenType;
            public String tokenLogo;
            public String tokenLevel;
            public boolean vip;
        }

        public static class TriggerInfoEntity {
            public String method;
            public String data;
            public ParameterEntity parameter;
            public String methodName;
            public String contract_address;
            public int call_value;

            public static class ParameterEntity {
                public String _value;
                public String _to;
            }
        }
    }

    public static List<TransactionInfo> parse(TronTransactions result) {
        List<TransactionInfo> list = new ArrayList<>();
        try {
            for (TronTransactions.DataEntity info : result.data) {
                TransactionInfo transaction = new TransactionInfo();
                transaction.hash = info.hash;
                transaction.from = info.ownerAddress;
                transaction.to = info.toAddress;
                transaction.blockNumber = info.block + "";;
                transaction.timestamp = info.timestamp / 1000 + "";
                transaction.value = info.trigger_info == null ? info.amount : info.trigger_info.parameter._value;
                transaction.contractAddress = info.trigger_info == null ? "" : info.trigger_info.contract_address;
                transaction.tokenSymbol = info.tokenInfo == null ? "trx" : info.tokenInfo.tokenAbbr;
                transaction.tokenDecimal = info.tokenInfo == null ? 6 : info.tokenInfo.tokenDecimal;
                list.add(transaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}