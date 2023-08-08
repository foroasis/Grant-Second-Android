package teleblock.blockchain.tron;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.tron.bean.TronAccountInfo;
import teleblock.blockchain.tron.bean.TronTransactions;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.TokenBalance;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.tron.TronAccountInfoApi;
import teleblock.network.api.blockchain.tron.TronTransactionsApi;
import teleblock.util.WalletUtil;

public class TronExplorer extends BlockExplorer {

    public TronExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getBalance(String address, BlockCallback<BigInteger> callback) {
        if (!WalletUtil.isTronAddress(address)) {
            callback.onSuccess(BigInteger.valueOf(0));
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new TronAccountInfoApi()
                        .setAddress(address))
                .request(new OnHttpListener<TronAccountInfo>() {
                    @Override
                    public void onSucceed(TronAccountInfo result) {
                        callback.onSuccess(BigInteger.valueOf(result.balance));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isTronAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new TronTransactionsApi()
                        .setAddress(address))
                .request(new OnHttpListener<TronTransactions>() {
                    @Override
                    public void onSucceed(TronTransactions result) {
                        callback.onSuccess(TronTransactions.parse(result));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    @Override
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isTronAddress(address)) {
            callback.onSuccess(new NFTResponse());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new TronAccountInfoApi()
                        .setAddress(address))
                .request(new OnHttpListener<TronAccountInfo>() {
                    @Override
                    public void onSucceed(TronAccountInfo result) {
                        List<NFTInfo> nftInfoList = TronAccountInfo.parse(result);
                        callback.onSuccess(new NFTResponse(nftInfoList));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

//    @Override
//    public void getTokenList(String address, BlockCallback<List<TokenBalance>> callback) {
//        if (address.toLowerCase().startsWith("0x")) {
//            callback.onSuccess(new ArrayList<>());
//            return;
//        }
//        // 主币单价
//        getMainCoinPrice(new BlockCallback<List<TokenBalance>>() {
//            @Override
//            public void onSuccess(List<TokenBalance> data) {
//                super.onSuccess(data);
//                EasyHttp.get(new ApplicationLifecycle())
//                        .api(new TronAccountInfoApi()
//                                .setAddress(address))
//                        .request(new OnHttpListener<TronAccountInfo>() {
//                            @Override
//                            public void onSucceed(TronAccountInfo result) {
//                                List<TokenBalance> tokenBalances = new ArrayList<>();
//                                for (TronAccountInfo.TokensEntity token : result.tokens) {
//                                    if (tokensPrice.get("TRX") != null) { // 根据主币单价换算代币单价
//                                        token.price = BigDecimal.valueOf(tokensPrice.get("TRX")).multiply(BigDecimal.valueOf(token.tokenPriceInTrx)).doubleValue();
//                                    }
//                                    tokenBalances.add(TokenBalance.parse(token));
//                                }
//                                callback.onSuccess(tokenBalances);
//                            }
//
//                            @Override
//                            public void onFail(Exception e) {
//                                callback.onError(e.toString());
//                            }
//                        });
//            }
//        });
//    }

}
