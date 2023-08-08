package teleblock.blockchain.metis;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.JsonUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.ethereum.bean.EthTransactions;
import teleblock.blockchain.metis.bean.MetisToken;
import teleblock.blockchain.oasis.bean.OasisToken;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.TokenBalance;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.metis.MetisTokensApi;
import teleblock.network.api.blockchain.metis.MetisTransactionsApi;
import teleblock.util.JsonUtil;
import teleblock.util.WalletUtil;

public class MetisBlockExplorer extends BlockExplorer {

    public MetisBlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
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
//        EasyHttp.get(new ApplicationLifecycle())
//                .api(new MetisTokensApi()
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
//    }

    @Override
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new NFTResponse());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new MetisTokensApi()
                        .setAddress(address))
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        List<MetisToken> tokenList = JsonUtil.parseJsonToList(JsonUtils.getString(result, "result"), MetisToken.class);
                        List<NFTInfo> nftInfoList = new ArrayList<>();
                        for (MetisToken metisToken : tokenList) {
                            if ("ERC-20".equals(metisToken.getType())) continue;
                            nftInfoList.add(MetisToken.parse(metisToken));
                        }
                        callback.onSuccess(new NFTResponse(nftInfoList));
                        // 请求NFT详情数据
                        CollectionUtils.forAllDo(nftInfoList, new CollectionUtils.Closure<NFTInfo>() {
                            @Override
                            public void execute(int index, NFTInfo item) {
                                getNftInfo(index, item, callback);
                            }
                        });
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new MetisTransactionsApi()
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
}
