package teleblock.model.wallet;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.R;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.ethereum.bean.EthBalances;
import teleblock.blockchain.oasis.bean.OasisToken;
import teleblock.blockchain.thundercore.bean.TTToken;
import teleblock.blockchain.tron.bean.TronAccountInfo;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;

/**
 * 创建日期：2022/8/23
 * 描述：
 */
public class TokenBalance {

    public String symbol;
    public String address;
    public int decimals;
    public String image;
    public int imageRes;
    public double price;
    public String balance;
    public String balanceRaw;
    public double balanceUSD;
    public long chainId;


    public static List<TokenBalance> parse(EthBalances ethBalances) {
        List<TokenBalance> tokenBalances = new ArrayList<>();
        TokenBalance tokenBalance = new TokenBalance();
        if (!CollectionUtils.isEmpty(ethBalances.products)) {
            EthBalances.ProductsEntity product = ethBalances.products.get(0);
            for (EthBalances.ProductsEntity.AssetsEntity asset : product.assets) {
                tokenBalance = new TokenBalance();
                tokenBalance.symbol = asset.symbol;
                if (Numeric.toBigInt(asset.address).intValue() > 0) {
                    tokenBalance.address = asset.address;
                }
                tokenBalance.decimals = asset.decimals;
                if (!CollectionUtils.isEmpty(asset.displayProps.images)) {
                    tokenBalance.image = asset.displayProps.images.get(0);
                }
                tokenBalance.price = asset.price;
                tokenBalance.balance = asset.balance;
                tokenBalance.balanceRaw = asset.balanceRaw;
                tokenBalance.balanceUSD = asset.balanceUSD;
                tokenBalances.add(tokenBalance);
            }
        }
        return tokenBalances;
    }

    public static TokenBalance parse(TTToken ttToken) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = ttToken.getSymbol();
        tokenBalance.address = ttToken.getContractAddress();
        tokenBalance.decimals = ttToken.getDecimals();
        tokenBalance.image = ttToken.getImage();
        tokenBalance.balance = WalletUtil.fromWei(ttToken.getBalance(), ttToken.getDecimals());
        tokenBalance.price = ttToken.getPrice();
        if (!TextUtils.isEmpty(tokenBalance.balance) && tokenBalance.price > 0) {
            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
        }
        return tokenBalance;
    }

    public static TokenBalance parse(OasisToken oasisToken) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = oasisToken.getSymbol();
        tokenBalance.address = oasisToken.getContractAddress();
        tokenBalance.decimals = oasisToken.getDecimals();
        if (TextUtils.isEmpty(oasisToken.getImage())) {
            tokenBalance.imageRes = R.drawable.token_holder;
        } else {
            tokenBalance.image = oasisToken.getImage();
        }
        tokenBalance.balance = WalletUtil.fromWei(oasisToken.getBalance(), oasisToken.getDecimals());
        tokenBalance.price = oasisToken.getPrice();
        if (!TextUtils.isEmpty(tokenBalance.balance) && tokenBalance.price > 0) {
            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
        }
        return tokenBalance;
    }

    public static TokenBalance parse(TronAccountInfo.TokensEntity token) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = token.tokenAbbr.toUpperCase();
        tokenBalance.address = token.tokenAbbr.equalsIgnoreCase("trx") ? "" : token.tokenId;
        tokenBalance.decimals = token.tokenDecimal;
        tokenBalance.image = token.tokenLogo;
        tokenBalance.balance = WalletUtil.fromWei(token.balance, token.tokenDecimal);
        tokenBalance.price = token.price;
        if (!TextUtils.isEmpty(tokenBalance.balance) && tokenBalance.price > 0) {
            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
        }
        return tokenBalance;
    }

    public static TokenBalance parse(ParticleToken.TokensEntity token, long chainId) {
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = token.symbol;
        tokenBalance.address = token.address;
        tokenBalance.decimals = token.decimals;
        if (TextUtils.isEmpty(token.image)) {
            tokenBalance.imageRes = R.drawable.token_holder;
        } else if ("DUDU".equalsIgnoreCase(token.symbol)) {//DUDU币配置
            try {
                tokenBalance.image = MMKVUtil.getWeb3ConfigData().getSocialTokens().getIcon();
            } catch (Exception e) {
            }
        } else {
            tokenBalance.image = token.image;
        }
        tokenBalance.balance = WalletUtil.fromWei(token.amount, token.decimals);
        tokenBalance.chainId = chainId;
        return tokenBalance;
    }
}