package teleblock.blockchain.polygon;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.ethereum.bean.EthTransactions;
import teleblock.model.wallet.OpenSeaAssets;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.polygon.PolygonAssentsApi;
import teleblock.network.api.blockchain.polygon.PolygonTransactionsApi;
import teleblock.util.WalletUtil;

public class PolygonBlockExplorer extends BlockExplorer {

    public PolygonBlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new PolygonTransactionsApi()
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

    @Override
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new NFTResponse());
            callback.onEnd();
            return;
        }
        EasyHttp.get(new ApplicationLifecycle())
                .api(new PolygonAssentsApi()
                        .setOwner_address(address)
                        .setOrder_direction("desc")
                        .setLimit("10")
                        .setCursor(cursor)
                        .setInclude_orders(true))
                .request(new OnHttpListener<String>() {
                    @Override
                    public void onSucceed(String result) {
                        callback.onSuccess(OpenSeaAssets.parse(result, chainType.getId()));
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
//        EasyHttp.get(new ApplicationLifecycle())
//                .api(new TokenBalancesApi()
//                        .setAddresses(address)
//                        .setNetwork(chainType.getMain_currency_name()))
//                .request(new OnHttpListener<String>() {
//
//                    @Override
//                    public void onSucceed(String result) {
//                        EthBalances ethBalances = EthBalances.parse(result, address.toLowerCase());
//                        if (ethBalances != null) {
//                            tokenBalances = TokenBalance.parse(ethBalances);
//                        }
//                    }
//
//                    @Override
//                    public void onFail(Exception e) {
//                    }
//
//                    @Override
//                    public void onEnd(Call call) {
//                        if (tokenBalances.isEmpty()) {
//                            // 主币余额
//                            getMainCoinBalance(address, callback);
//                            // 主币单价
//                            getMainCoinPrice(callback);
//                        } else {
//                            callback.onSuccess(tokenBalances);
//                        }
//                    }
//                });
//    }

}
