package teleblock.blockchain;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.body.JsonBody;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.p2p.solanaj.model.types.RpcResponse;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import teleblock.model.ParticleUserInfo;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.CurrencyPriceEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTMetadata;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.ParticleNFT;
import teleblock.model.wallet.ParticlePrice;
import teleblock.model.wallet.ParticleToken;
import teleblock.model.wallet.ParticleTransaction;
import teleblock.model.wallet.TokenBalance;
import teleblock.model.wallet.TransactionInfo;
import teleblock.network.api.blockchain.ParticleNFTsApi;
import teleblock.network.api.blockchain.ParticlePriceApi;
import teleblock.network.api.blockchain.ParticlePriceServerApi;
import teleblock.network.api.blockchain.ParticleTokensApi;
import teleblock.network.api.blockchain.ParticleTransactionsApi;
import teleblock.network.api.blockchain.solana.SolanaPriceApi;
import teleblock.network.api.blockchain.solana.SolanaTokensAndNFTsApi;
import teleblock.util.JsonUtil;
import teleblock.util.WalletUtil;
import timber.log.Timber;

/**
 * 创建日期：2022/12/1
 * 描述：区块链接口基类
 */
public class BlockExplorer {

    public static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    public Web3ConfigEntity.WalletNetworkConfigChainType chainType;
    public Web3ConfigEntity.WalletNetworkConfigEntityItem mainCurrency;
    public List<TokenBalance> tokenBalances = new ArrayList<>();
    public Map<String, Double> tokensPrice = new HashMap<>();

    public BlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        this.chainType = chainType;
        mainCurrency = BlockchainConfig.getMainCurrency(chainType);
    }

    /**
     * 指定地址余额
     *
     * @param address
     * @param callback
     */
    public void getBalance(String address, BlockCallback<BigInteger> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<BigInteger>() {
            @Override
            public BigInteger doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
                return ethGetBalance.getBalance();
            }

            @Override
            public void onSuccess(BigInteger result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * 根据交易哈希值获取交易数据
     *
     * @param hash
     * @param callback
     */
    public void getTransactionByHash(String hash, BlockCallback<EthTransaction> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<EthTransaction>() {
            @Override
            public EthTransaction doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthTransaction ethTransaction = web3j.ethGetTransactionByHash(hash).send();
                return ethTransaction;
            }

            @Override
            public void onSuccess(EthTransaction result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * @param hash
     * @param callback
     */
    public void getBlockByHash(String hash, BlockCallback<EthBlock> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<EthBlock>() {
            @Override
            public EthBlock doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthBlock ethBlock = web3j.ethGetBlockByHash(hash, true).send();
                return ethBlock;
            }

            @Override
            public void onSuccess(EthBlock result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * 根据交易哈希值获取交易数据
     *
     * @param hash
     * @param callback
     */
    public void getTransactionReceipt(String hash, BlockCallback<EthGetTransactionReceipt> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<EthGetTransactionReceipt>() {
            @Override
            public EthGetTransactionReceipt doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthGetTransactionReceipt ethTransaction = web3j.ethGetTransactionReceipt(hash).send();
                return ethTransaction;
            }

            @Override
            public void onSuccess(EthGetTransactionReceipt result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }


    /**
     * 根据账户地址 获取该地址下的所有交易记录
     *
     * @param address
     * @param callback
     * @param page
     */
    public void getTransactionsByAddress(String address, int page, BlockCallback<List<TransactionInfo>> callback) {
        EasyHttp.cancel("getTransactionsByAddress");
        EasyHttp.post(new ApplicationLifecycle())
                .tag("getTransactionsByAddress")
                .api(new ParticleTransactionsApi())
                .body(new JsonBody(ParticleTransactionsApi.createJson(chainType.getId(), address)))
                .request(new OnHttpListener<RpcResponse<List<ParticleTransaction>>>() {
                    @Override
                    public void onSucceed(RpcResponse<List<ParticleTransaction>> result) {
                        if (result.getError() != null) {
                            callback.onError(result.getError().getMessage());
                            return;
                        }
                        callback.onSuccess(ParticleTransaction.parse(result.getResult()));
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onError(e.toString());
                    }
                });
    }

    /**
     * 获取gasPrice
     */
    public void getGasPrice(BlockCallback<BigInteger> callback) {
        if (chainType.getId() == 999) {
            callback.onSuccess(BigInteger.valueOf(0));
            return;
        }
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<BigInteger>() {
            @Override
            public BigInteger doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthGasPrice price = web3j.ethGasPrice().send();
                return price.getGasPrice();
            }

            @Override
            public void onSuccess(BigInteger result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * 获取gasLimit
     */
    public void getGasLimit(String fromAddress, String to, BigInteger value, String data, BlockCallback<BigInteger> callback) {
        if (chainType.getId() == 999) {
            callback.onSuccess(BigInteger.valueOf(0));
            return;
        }
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<BigInteger>() {
            @Override
            public BigInteger doInBackground() throws Throwable {
//                String data = null;
//                String to = toAddress;
//                if (!TextUtils.isEmpty(contractAddress)) {
//                    to = contractAddress;
//                    data = Web3AbiDataUtils.encodeTransferData(toAddress, value);
//                }
                Transaction transaction = new Transaction(fromAddress, null, null, null,
                        to, TextUtils.isEmpty(data) ? value : null, data);

//                BigInteger roundingFactor = BigInteger.valueOf(10000);
//                BigInteger txMin = BigInteger.valueOf(21000L);
//                BigInteger bytePrice = BigInteger.valueOf(300);
//                byte[] bytes = Numeric.hexStringToByteArray(GsonUtils.toJson(transaction));
//                BigInteger dataLength = BigInteger.valueOf(bytes.length);
//                BigInteger estimate = bytePrice.multiply(dataLength).add(txMin);
//                return estimate.divide(roundingFactor).add(BigInteger.ONE).multiply(roundingFactor);

                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
                return ethEstimateGas.getAmountUsed();
            }

            @Override
            public void onSuccess(BigInteger result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * 发起交易
     */
    public void sendTransaction(ETHWallet ethWallet, String toAddress, BigInteger gasPrice, BigInteger gasLimit, String data, BigInteger value, String password, BlockCallback<String> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<EthSendTransaction>() {
            @Override
            public EthSendTransaction doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                Credentials credentials = WalletUtils.loadCredentials(password, ethWallet.getKeystorePath());
                RawTransactionManager rawTransactionManager = new RawTransactionManager(web3j, credentials, chainType.getId());
                return rawTransactionManager.sendTransaction(gasPrice, gasLimit, toAddress, data, value);
            }

            @Override
            public void onSuccess(EthSendTransaction result) {
                if (result.hasError()) {
                    callback.onError(result.getError().getMessage());
                } else {
                    callback.onSuccess(result.getTransactionHash());
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }

    /**
     * 获取NFT列表
     */
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (WalletUtil.isEvmAddress(address) && chainType.getId() == 99999 ||
                WalletUtil.isSolanaAddress(address) && chainType.getId() != 99999) {
            callback.onSuccess(new NFTResponse());
            callback.onEnd();
            return;
        }
        EasyHttp.cancel("getNftList");
        EasyHttp.post(new ApplicationLifecycle())
                .api(new ParticleNFTsApi())
                .tag("getNftList")
                .body(new JsonBody(ParticleNFTsApi.createJson(chainType.getId(), address)))
                .request(new OnHttpListener<RpcResponse<List<ParticleNFT>>>() {
                    @Override
                    public void onSucceed(RpcResponse<List<ParticleNFT>> result) {
                        if (result.getError() != null) {
                            callback.onError(result.getError().getMessage());
                            return;
                        }
                        NFTResponse nftResponse = new NFTResponse();
                        nftResponse.assets = ParticleNFT.pares(result.getResult(), chainType);
                        callback.onSuccess(nftResponse);
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

    /**
     * 获取代币列表
     */
    public void getTokenList(String address, BlockCallback<List<TokenBalance>> callback) {
        if (WalletUtil.isEvmAddress(address) && chainType.getId() == 99999 ||
                WalletUtil.isSolanaAddress(address) && chainType.getId() != 99999) {
            callback.onSuccess(tokenBalances);
            callback.onEnd();
            return;
        }
        EasyHttp.cancel("getTokenList");
        EasyHttp.post(new ApplicationLifecycle())
                .tag("getTokenList")
                .api(chainType.getId() == 99999 ? new SolanaTokensAndNFTsApi() : new ParticleTokensApi())
                .body(new JsonBody(chainType.getId() == 99999 ? SolanaTokensAndNFTsApi.createJson(address) : ParticleTokensApi.createJson(chainType.getId(), address)))
                .request(new OnHttpListener<RpcResponse<ParticleToken>>() {
                    @Override
                    public void onSucceed(RpcResponse<ParticleToken> result) {
                        if (result.getError() != null) {
                            callback.onSuccess(tokenBalances);
                            return;
                        }
                        // 添加主币数据
                        TokenBalance tokenBalance = new TokenBalance();
                        tokenBalance.symbol = chainType.getMain_currency_name();
                        tokenBalance.decimals = chainType.getId() == 99999 ? 9 : 18;
                        tokenBalance.imageRes = ResourceUtils.getDrawableIdByName("user_chain_logo_" + chainType.getId());
                        tokenBalance.balance = WalletUtil.fromWei(result.getResult().nativeX, chainType.getId() == 99999 ? 9 : 18);
                        tokenBalance.chainId = chainType.getId();
                        tokenBalances.add(tokenBalance);
                        List<String> addresses = new ArrayList<>();
                        addresses.add("native");
                        CollectionUtils.forAllDo(result.getResult().tokens, new CollectionUtils.Closure<ParticleToken.TokensEntity>() {
                            @Override
                            public void execute(int index, ParticleToken.TokensEntity item) {
                                tokenBalances.add(TokenBalance.parse(item, chainType.getId()));
                                addresses.add(item.address);
                            }
                        });
                        getTokenPrice(addresses, callback);
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onSuccess(tokenBalances);
                    }
                });
    }

    /**
     * 获取代币价格
     */
    public void getTokenPrice(List<String> addresses, BlockCallback<List<TokenBalance>> callback) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(chainType.getId() == 99999 ? new SolanaPriceApi() : new ParticlePriceApi())
                .body(new JsonBody(chainType.getId() == 99999 ? SolanaPriceApi.createJson(addresses) : ParticlePriceApi.createJson(chainType.getId(), addresses)))
                .request(new OnHttpListener<RpcResponse<List<ParticlePrice>>>() {
                    @Override
                    public void onSucceed(RpcResponse<List<ParticlePrice>> result) {
                        if (result.getError() != null) {
                            callback.onSuccess(tokenBalances);
                            return;
                        }
                        CollectionUtils.forAllDo(result.getResult(), new CollectionUtils.Closure<ParticlePrice>() {
                            @Override
                            public void execute(int index, ParticlePrice item) {
                                if ("native".equals(item.address)) { // 主币
                                    TokenBalance tokenBalance = tokenBalances.get(0);
                                    tokenBalance.price = item.currencies.get(0).price;
                                    tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                                } else {
                                    tokensPrice.put(item.address, item.currencies.get(0).price);
                                }
                            }
                        });
                        for (TokenBalance tokenBalance : tokenBalances) {
                            if (tokensPrice.get(tokenBalance.address) != null) {
                                tokenBalance.price = tokensPrice.get(tokenBalance.address);
                                tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                            }
                        }
                        callback.onSuccess(tokenBalances);
                    }

                    @Override
                    public void onFail(Exception e) {
                        callback.onSuccess(tokenBalances);
                    }

                    @Override
                    public void onEnd(Call call) {
                        callback.onEnd();
                    }
                });
    }


    /**
     * 获取主币余额
     */
    public void getMainCoinBalance(String address, BlockCallback<List<TokenBalance>> callback) {
        // 添加主币数据
        TokenBalance tokenBalance = new TokenBalance();
        tokenBalance.symbol = chainType.getMain_currency_name();
        tokenBalance.decimals = chainType.getId() == 999 ? 6 : 18;
        tokenBalance.imageRes = ResourceUtils.getDrawableIdByName("user_chain_logo_" + chainType.getId());
        tokenBalance.balance = "0";
        if (!tokenBalances.isEmpty()) {
            tokenBalances.add(0, tokenBalance);
        } else {
            tokenBalances.add(tokenBalance);
        }
        getBalance(address, new BlockCallback<BigInteger>() {
            @Override
            public void onSuccess(BigInteger data) {
                tokenBalance.balance = WalletUtil.fromWei(data.toString(), chainType.getId() == 999 ? 6 : 18);
                if (tokensPrice.get(tokenBalance.symbol) != null) {
                    tokenBalance.price = tokensPrice.get(tokenBalance.symbol);
                    tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                }
                callback.onSuccess(tokenBalances);
            }

            @Override
            public void onError(String msg) {
                callback.onSuccess(tokenBalances);
            }
        });
    }

    /**
     * 获取主币单价
     */
    public void getMainCoinPrice(BlockCallback<List<TokenBalance>> callback) {
        WalletUtil.requestMainCoinPrice(mainCurrency.getCoin_id(), new WalletUtil.RequestCoinPriceListener() {
            @Override
            public void requestEnd() {
                callback.onSuccess(tokenBalances);
            }

            @Override
            public void requestError(String msg) {
            }

            @Override
            public void requestSuccessful(CurrencyPriceEntity resultData) {
                tokensPrice.put(chainType.getMain_currency_name(), resultData.getUsd());
                for (TokenBalance tokenBalance : tokenBalances) {
                    if (tokensPrice.get(tokenBalance.symbol) != null) {
                        tokenBalance.price = tokensPrice.get(tokenBalance.symbol);
                        tokenBalance.balanceUSD = WalletUtil.toCoinPriceUSD(tokenBalance.balance, String.valueOf(tokenBalance.price));
                        break;
                    }
                }
            }
        });
    }

    /**
     * 调用合约方法
     */
    public void ethCall(String contractAddress, String data, BlockCallback<String> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                Transaction transaction = Transaction.createEthCallTransaction(null, contractAddress, data);
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
                return ethCall.getResult();
            }

            @Override
            public void onSuccess(String result) {
                try {
                    callback.onSuccess(result);
                } catch (NumberFormatException e) {
                    callback.onSuccess("0x0");
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * 获取NFT详情
     */
    public void getNftInfo(int index, NFTInfo nftInfo, BlockCallback<NFTResponse> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                String encodedFunction;
                if (nftInfo.token_standard.endsWith("721")) {
                    encodedFunction = Web3AbiDataUtils.encodeTokenURIData(BigInteger.valueOf(nftInfo.token_id));
                } else {
                    encodedFunction = Web3AbiDataUtils.encodeUriData(BigInteger.valueOf(nftInfo.token_id));
                }
                Transaction transaction = Transaction.createEthCallTransaction(ZERO_ADDRESS, nftInfo.contract_address, encodedFunction);
                EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
                return ethCall.getValue();
            }

            @Override
            public void onSuccess(String result) {
                if (TextUtils.isEmpty(result)) return;
                String json = ConvertUtils.bytes2String(FunctionReturnDecoder.decodeDynamicBytes(result));
                String url = json.replace("ipfs://", "https://ipfs.io/ipfs/");
                Timber.i("parseLink-->" + url);
                if (TextUtils.isEmpty(url)) return;
                OkHttpUtils.get().url(url).build().readTimeOut(20000).execute(new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        NFTMetadata nftMetadata = JsonUtil.parseJsonToBean(response, NFTMetadata.class);
                        if (nftMetadata != null) {
                            nftInfo.name = nftMetadata.getName();
                            String image = nftMetadata.getImage().replace("ipfs://", "https://ipfs.io/ipfs/");
                            if (!RegexUtils.isURL(image)) image = "https://ipfs.io/ipfs/" + image;
                            nftInfo.thumb_url = image;
                            nftInfo.setOriginal_url(nftInfo.thumb_url);
                            callback.onProgress(index, JsonUtil.parseObjToJson(nftInfo));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        callback.onError(e.toString());
                    }
                });
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
//                callback.onError(t.toString());
            }
        });
    }

    /**
     * 获取nft详情数据
     *
     * @param token_standard
     * @param token_id
     * @param contract_address
     * @param callback
     */
    public void getNftInfo(String token_standard, long token_id, String contract_address, BlockCallback<NFTMetadata> callback) {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                Web3j web3j = Web3j.build(new HttpService(chainType.getRpc_url()));
                String encodedFunction;
                if (token_standard.endsWith("721")) {
                    encodedFunction = Web3AbiDataUtils.encodeTokenURIData(BigInteger.valueOf(token_id));
                } else {
                    encodedFunction = Web3AbiDataUtils.encodeUriData(BigInteger.valueOf(token_id));
                }
                Transaction transaction = Transaction.createEthCallTransaction(ZERO_ADDRESS, contract_address, encodedFunction);
                EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
                return ethCall.getValue();
            }

            @Override
            public void onSuccess(String result) {
                if (TextUtils.isEmpty(result)) return;
                String json = ConvertUtils.bytes2String(FunctionReturnDecoder.decodeDynamicBytes(result));
                String url = json.replace("ipfs://", "https://ipfs.io/ipfs/");
                if (TextUtils.isEmpty(url)) return;
                OkHttpUtils.get().url(url).build().readTimeOut(20000).execute(new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        NFTMetadata nftMetadata = JsonUtil.parseJsonToBean(response, NFTMetadata.class);
                        callback.onSuccess(nftMetadata);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onError(e.toString());
                    }
                });
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                callback.onError(t.toString());
            }
        });
    }


    /***
     * 获取Particle用户信息
     *
     * @param uuid
     * @param token
     * @param callback
     */
    public static void getParticleUserInfo(String uuid, String token, BlockCallback<ParticleUserInfo> callback) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new ParticlePriceServerApi())
                .body(new JsonBody(ParticlePriceServerApi.createJson(uuid, token)))
                .request(new OnHttpListener<RpcResponse<ParticleUserInfo>>() {
                    @Override
                    public void onSucceed(RpcResponse<ParticleUserInfo> result) {
                        if (result != null) callback.onSuccess(result.getResult());
                        else callback.onSuccess(null);
                    }

                    @Override
                    public void onFail(Exception e) {
                    }

                    @Override
                    public void onEnd(Call call) {
                    }
                });
    }
}