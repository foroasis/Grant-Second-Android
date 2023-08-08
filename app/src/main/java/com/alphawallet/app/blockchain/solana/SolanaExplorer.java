package teleblock.blockchain.solana;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.p2p.solanaj.model.types.RpcResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.blockchain.solana.bean.SolanaTokensAndNFTs;
import teleblock.blockchain.solana.bean.SolanaTransaction;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTMetadata;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.solana.SolanaTokensAndNFTsApi;
import teleblock.network.api.blockchain.solana.SolanaTransactionsApi;
import teleblock.util.JsonUtil;
import teleblock.util.WalletUtil;

public class SolanaExplorer extends BlockExplorer {

    public SolanaExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        if (!WalletUtil.isSolanaAddress(address)) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        EasyHttp.cancel("getTransactionsByAddress");
        EasyHttp.post(new ApplicationLifecycle())
                .tag("getTransactionsByAddress")
                .api(new SolanaTransactionsApi())
                .body(new JsonBody(SolanaTransactionsApi.createJson(address)))
                .request(new OnHttpListener<RpcResponse<List<SolanaTransaction>>>() {
                    @Override
                    public void onSucceed(RpcResponse<List<SolanaTransaction>> result) {
                        if (result.getError() != null) {
                            callback.onError(result.getError().getMessage());
                            return;
                        }
                        callback.onSuccess(SolanaTransaction.parse(result.getResult()));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isSolanaAddress(address)) {
            callback.onSuccess(new NFTResponse());
            return;
        }
        EasyHttp.post(new ApplicationLifecycle())
                .api(new SolanaTokensAndNFTsApi())
                .body(new JsonBody(SolanaTokensAndNFTsApi.createJson(address)))
                .request(new OnHttpListener<RpcResponse<SolanaTokensAndNFTs>>() {
                    @Override
                    public void onSucceed(RpcResponse<SolanaTokensAndNFTs> result) {
                        SolanaTokensAndNFTs solanaTokensAndNFTs = result.getResult();
                        if (solanaTokensAndNFTs != null) {
                            NFTResponse nftResponse = new NFTResponse();
                            List<SolanaTokensAndNFTs.Nfts> nfts = solanaTokensAndNFTs.nfts;
                            List<NFTInfo> assets = new ArrayList<>();
                            CollectionUtils.forAllDo(nfts, (index, item) -> {
                                NFTInfo nftInfo = new NFTInfo();
                                nftInfo.name = item.name;
                                getNftInfo(item.metadata.data.uri, nftInfo, index, callback);
                                nftInfo.contract_address = item.mint;
                                nftInfo.symbol = item.symbol;
                                nftInfo.token_standard = "SPL";
                                nftInfo.blockchain = "Solana";
                                assets.add(nftInfo);
                            });
                            nftResponse.assets = assets;
                            callback.onSuccess(nftResponse);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    private void getNftInfo(String url, NFTInfo nftInfo, int index, BlockCallback<NFTResponse> callback) {
        if (TextUtils.isEmpty(url)) return;
        OkHttpUtils.get().url(url).build().readTimeOut(20000).execute(new StringCallback() {
            @Override
            public void onResponse(String response, int id) {
                NFTMetadata nftMetadata = JsonUtil.parseJsonToBean(response, NFTMetadata.class);
                if (nftMetadata != null) {
                    nftInfo.name = nftMetadata.getName();
                    String image = nftMetadata.getImage();
                    nftInfo.thumb_url = image;
                    nftInfo.setOriginal_url(nftInfo.thumb_url);
                    callback.onProgress(index, JsonUtil.parseObjToJson(nftInfo));
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {
            }
        });
    }

}
