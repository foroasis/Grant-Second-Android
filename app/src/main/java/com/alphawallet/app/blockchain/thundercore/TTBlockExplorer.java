package teleblock.blockchain.thundercore;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.p2p.solanaj.model.types.RpcResponse;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.JsonRpc;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTResponse;
import teleblock.blockchain.thundercore.bean.TTNft;
import teleblock.blockchain.thundercore.bean.TTToken;
import teleblock.blockchain.thundercore.bean.TTTokensPrice;
import teleblock.blockchain.thundercore.bean.TTTransactions;
import teleblock.model.wallet.TokenBalance;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.thundercore.TTNftsApi;
import teleblock.network.api.blockchain.thundercore.TTTokensApi;
import teleblock.network.api.blockchain.thundercore.TTTransactionsApi;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletUtil;

public class TTBlockExplorer extends BlockExplorer {

    public TTBlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new TTTransactionsApi()
                        .setAddress(address)
                        .setPage(page))
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        callback.onSuccess(TTTransactions.parse(result));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    @Override
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new NFTResponse());
            return;
        }
        EasyHttp.post(new ApplicationLifecycle())
                .api(new TTNftsApi())
                .body(new JsonBody(TTNftsApi.createJson(address)))
                .request(new OnHttpListener<JsonRpc>() {
                    @Override
                    public void onSucceed(JsonRpc result) {
                        if (result != null) {
                            List<TTNft> ttNftList = JsonUtil.parseJsonToList(result.getResult(), TTNft.class);
                            List<NFTInfo> nftInfoList = TTNft.parse(ttNftList);
                            callback.onSuccess(new NFTResponse(nftInfoList));
                            // 请求NFT详情数据
                            CollectionUtils.forAllDo(nftInfoList, new CollectionUtils.Closure<NFTInfo>() {
                                @Override
                                public void execute(int index, NFTInfo item) {
                                    getNftInfo(index, item, callback);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }

                    @Override
                    public void onEnd(Call call) {
                        callback.onEnd();
                    }
                });
    }

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
//        EasyHttp.post(new ApplicationLifecycle())
//                .api(new TTTokensApi())
//                .body(new JsonBody(TTTokensApi.createJson(address)))
//                .request(new OnHttpListener<List<RpcResponse<String>>>() {
//
//                    @Override
//                    public void onEnd(Call call) {
//                        callback.onSuccess(tokenBalances);
//                    }
//
//                    @Override
//                    public void onSucceed(List<RpcResponse<String>> result) {
//                        List<TTToken> tokenList = MMKVUtil.getTTTokens();
//                        CollectionUtils.forAllDo(tokenList, new CollectionUtils.Closure<TTToken>() {
//                            @Override
//                            public void execute(int index, TTToken item) {
//                                String balance;
//                                try {
//                                    balance = Numeric.toBigInt(result.get(index).getResult()).toString();
//                                } catch (Exception e) {
//                                    balance = "0";
//                                }
//                                item.setBalance(balance);
//                                if (tokensPrice.get(item.getSymbol()) != null) {
//                                    item.setPrice(tokensPrice.get(item.getSymbol()));
//                                }
//                                tokenBalances.add(TokenBalance.parse(item));
//                            }
//                        });
//                        // 过滤掉没余额的代币
//                        CollectionUtils.filter(tokenBalances, new CollectionUtils.Predicate<TokenBalance>() {
//                            @Override
//                            public boolean evaluate(TokenBalance item) {
//                                return Double.parseDouble(item.balance) > 0 || TextUtils.isEmpty(item.address);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFail(Exception e) {
//                        callback.onError(e.toString());
//                    }
//                });
//        // 代币单价
//        getTTTokensPrice(callback);
//    }

    private void getTTTokensPrice(BlockCallback<List<TokenBalance>> callback) {
        OkHttpUtils.get().url("https://ttswap.space/api/tokens").build().execute(new StringCallback() {

            @Override
            public void onAfter(int id) {
                callback.onSuccess(tokenBalances);
            }

            @Override
            public void onResponse(String response, int id) {
                TTTokensPrice result = JsonUtil.parseJsonToBean(response, TTTokensPrice.class);
                if (result != null) {
                    for (TTTokensPrice.DataBean.TokenListBean token : result.getData().getTokenList()) {
                        tokensPrice.put(token.getSymbol(), token.getPrice());
                    }
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
