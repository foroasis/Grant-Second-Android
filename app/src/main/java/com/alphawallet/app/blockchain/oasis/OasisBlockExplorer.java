package teleblock.blockchain.oasis;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.MapUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.ethereum.bean.EthTransactions;
import teleblock.blockchain.oasis.bean.OasisToken;
import teleblock.blockchain.oasis.bean.OasisTokensPrice;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.TokenBalance;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.oasis.OasisTokensApi;
import teleblock.network.api.blockchain.oasis.OasisTransactionsApi;
import teleblock.util.JsonUtil;
import teleblock.util.WalletUtil;

public class OasisBlockExplorer extends BlockExplorer {

    public OasisBlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new OasisTransactionsApi()
                        .setAddress(address)
                        .setPage(page))
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        callback.onSuccess(EthTransactions.parse(result));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

//    @Override
//    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
//        if (!WalletUtil.isEvmAddress(address)) {
//            callback.onSuccess(new NFTResponse());
//            return;
//        }
//        EasyHttp.get(new ApplicationLifecycle())
//                .api(new OasisTokensApi()
//                        .setAddress(address))
//                .request(new OnHttpListener<String>() {
//                    @Override
//                    public void onSucceed(String result) {
//                        List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
//                        List<NFTInfo> nftInfoList = new ArrayList<>();
//                        for (OasisToken oasisToken : tokenList) {
//                            if ("ERC-20".equals(oasisToken.getType())) continue;
//                            nftInfoList.add(OasisToken.parse(oasisToken));
//                        }
//                        callback.onSuccess(new NFTResponse(nftInfoList));
//                        // 请求NFT详情数据
//                        CollectionUtils.forAllDo(nftInfoList, new CollectionUtils.Closure<NFTInfo>() {
//                            @Override
//                            public void execute(int index, NFTInfo item) {
//                                getNftInfo(index, item, callback);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFail(Exception e) {
//                        callback.onError(e.toString());
//                    }
//                });
//    }

//    @Override
//    public void getTokenList(String address, BlockCallback<List<TokenBalance>> callback) {
//        if (!address.toLowerCase().startsWith("0x")) {
//            callback.onSuccess(new ArrayList<>());
//            return;
//        }
//        // 主币余额
//        getMainCoinBalance(address, callback);
//        // 主币单价
//        getMainCoinPrice(callback);
//        // 代币余额
//        EasyHttp.get(new ApplicationLifecycle())
//                .api(new OasisTokensApi()
//                        .setAddress(address))
//                .request(new OnHttpListener<String>() {
//
//                    @Override
//                    public void onEnd(Call call) {
//                        callback.onSuccess(tokenBalances);
//                    }
//
//                    @Override
//                    public void onSucceed(String result) {
//                        List<OasisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), OasisToken.class);
//                        for (OasisToken oasisToken : tokenList) {
//                            if (!"ERC-20".equals(oasisToken.getType())) continue;
//                            if (tokensPrice.get(oasisToken.getSymbol()) != null) {
//                                oasisToken.setPrice(tokensPrice.get(oasisToken.getSymbol()));
//                            }
//                            String drawableName = "ic_os_" + oasisToken.getSymbol().toLowerCase();
//                            if (ResourceUtils.getDrawableIdByName(drawableName) > 0) {
//                                oasisToken.setImageRes(ResourceUtils.getDrawableIdByName(drawableName));
//                            }
//                            tokenBalances.add(TokenBalance.parse(oasisToken));
//                        }
//                    }
//
//                    @Override
//                    public void onFail(Exception e) {
//                        callback.onError(e.toString());
//                    }
//                });
//        // 代币单价
//        getOasisTokensPrice(callback);
//    }

    private void getOasisTokensPrice(BlockCallback<List<TokenBalance>> callback) {
        OkHttpUtils.get().url("https://app.yuzu-swap.com/api/prices").build().execute(new StringCallback() {

            @Override
            public void onAfter(int id) {
                callback.onSuccess(tokenBalances);
            }

            @Override
            public void onResponse(String response, int id) {
                OasisTokensPrice result = JsonUtil.parseJsonToBean(response, OasisTokensPrice.class);
                if (result != null) {
                    MapUtils.forAllDo(result.getData(), new MapUtils.Closure<String, Double>() {
                        @Override
                        public void execute(String key, Double value) {
                            tokensPrice.put(key, value);
                        }
                    });
                    for (TokenBalance tokenBalance : tokenBalances) {
                        if (tokensPrice.get(tokenBalance.symbol) != null) {
                            tokenBalance.price = tokensPrice.get(tokenBalance.symbol);
                            tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                        }
                    }
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }
        });
    }
}
