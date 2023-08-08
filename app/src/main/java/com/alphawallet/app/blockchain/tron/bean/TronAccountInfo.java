package teleblock.blockchain.tron.bean;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.NFTInfo;

/**
 * 创建日期：2022/12/28
 * 描述：
 */
public class TronAccountInfo {

    public List<Trc20tokenBalancesEntity> trc20token_balances;
    public int transactions_out;
    public int acquiredDelegateFrozenForBandWidth;
    public long rewardNum;
    public List<TokenBalancesEntity> tokenBalances;
    public int delegateFrozenForEnergy;
    public List<BalancesEntity> balances;
    public List<Trc721tokenBalancesEntity> trc721token_balances;
    public long balance;
    public long voteTotal;
    public long totalFrozen;
    public List<TokensEntity> tokens;
    public int transactions_in;
    public int totalTransactionCount;
    public RepresentativeEntity representative;
    public long frozenForBandWidth;
    public int reward;
    public String addressTagLogo;
    public String address;
    public BandwidthEntity bandwidth;
    public long date_created;
    public int accountType;
    public String addressTag;
    public FrozenEntity frozen;
    public int transactions;
    public int witness;
    public int delegateFrozenForBandWidth;
    public String name;
    public int frozenForEnergy;
    public int acquiredDelegateFrozenForEnergy;

    public static class RepresentativeEntity {
        public long lastWithDrawTime;
        public int allowance;
        public boolean enabled;
        public String url;
    }

    public static class BandwidthEntity {
        public int energyRemaining;
        public long totalEnergyLimit;
        public long totalEnergyWeight;
        public int netUsed;
        public int storageLimit;
        public double storagePercentage;
        public double netPercentage;
        public int storageUsed;
        public int storageRemaining;
        public int freeNetLimit;
        public int energyUsed;
        public int freeNetRemaining;
        public long netLimit;
        public long netRemaining;
        public int energyLimit;
        public int freeNetUsed;
        public long totalNetWeight;
        public double freeNetPercentage;
        public double energyPercentage;
        public long totalNetLimit;

    }

    public static class FrozenEntity {
        public long total;
        public List<BalancesEntity> balances;

        public static class BalancesEntity {
            public long expires;
            public long amount;
        }
    }


    public static class Trc20tokenBalancesEntity {
        public String tokenId;
        public String balance;
        public String tokenName;
        public String tokenAbbr;
        public int tokenDecimal;
        public int tokenCanShow;
        public String tokenType;
        public String tokenLogo;
        public boolean vip;
        public double tokenPriceInTrx;
        public double amount;
        public int nrOfTokenHolders;
        public int transferCount;
    }

    public static class TokenBalancesEntity {
        public String amount;
        public int tokenPriceInTrx;
        public String tokenId;
        public String balance;
        public String tokenName;
        public int tokenDecimal;
        public String tokenAbbr;
        public int tokenCanShow;
        public String tokenType;
        public boolean vip;
        public String tokenLogo;
        public String owner_address;
        public int transferCount;
        public int nrOfTokenHolders;
    }

    public static class BalancesEntity {
        public String amount;
        public int tokenPriceInTrx;
        public String tokenId;
        public String balance;
        public String tokenName;
        public int tokenDecimal;
        public String tokenAbbr;
        public int tokenCanShow;
        public String tokenType;
        public boolean vip;
        public String tokenLogo;
        public String owner_address;
        public int transferCount;
        public int nrOfTokenHolders;
    }

    public static class Trc721tokenBalancesEntity {
        public long tokenId;
        public String balance;
        public String tokenName;
        public String tokenAbbr;
        public int tokenDecimal;
        public int tokenCanShow;
        public String tokenType;
        public String tokenLogo;
        public boolean vip;
        public int nrOfTokenHolders;
        public int transferCount;
    }

    public static class TokensEntity {
        public String amount;
        public double tokenPriceInTrx;
        public String tokenId;
        public String balance;
        public String tokenName;
        public int tokenDecimal;
        public String tokenAbbr;
        public int tokenCanShow;
        public String tokenType;
        public boolean vip;
        public String tokenLogo;
        public int nrOfTokenHolders;
        public int transferCount;
        public String owner_address;
        public double price;
    }


    public static List<NFTInfo> parse(TronAccountInfo result) {
        List<NFTInfo> nftInfoList = new ArrayList<>();
        try {
            for (TronAccountInfo.Trc721tokenBalancesEntity entity : result.trc721token_balances) {
                NFTInfo nftInfo = new NFTInfo();
                nftInfo.name = entity.tokenName;
                nftInfo.thumb_url = entity.tokenLogo;
                nftInfo.setOriginal_url(nftInfo.thumb_url);
                nftInfo.token_id = entity.tokenId;
                nftInfo.token_standard = entity.tokenType;
                nftInfo.symbol = entity.tokenAbbr;
                nftInfoList.add(nftInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nftInfoList;
    }
}