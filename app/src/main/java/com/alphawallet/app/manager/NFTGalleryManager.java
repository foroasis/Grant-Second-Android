package teleblock.manager;

import android.os.Bundle;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTResponse;
import teleblock.ui.activity.nft.NftBuyActivity;
import teleblock.ui.activity.nft.NftNoPriceInfoActivity;
import teleblock.ui.activity.nft.NftSellPriceActivity;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.util.MMKVUtil;


public class NFTGalleryManager {

    private static NFTGalleryManager instance;

    public static NFTGalleryManager getInstance() {
        if (instance == null) {
            synchronized (NFTGalleryManager.class) {
                if (instance == null) {
                    instance = new NFTGalleryManager();
                }
            }
        }
        return instance;
    }

    public void getAllNFTGallery(long chainId, String address, BlockCallback<List<NFTInfo>> callback) {
        List<Web3ConfigEntity.WalletNetworkConfigChainType> chainTypes = new ArrayList<>();
        if (chainId != 0) {
            chainTypes.add(BlockchainConfig.getChainType(chainId));
        } else {
            CollectionUtils.forAllDo(MMKVUtil.getWeb3ConfigData().getChainType(), (index, item) -> {
                if (CollectionUtils.find(MMKVUtil.getWeb3ConfigData().getNftMarketAddress(), nftMarketAddress -> nftMarketAddress.getChain_id() == item.getId()) != null) {
                    chainTypes.add(item);
                }
            });
        }
        int[] requestTime = {0};
        List<NFTInfo> nftInfoList = new ArrayList<>();
        CollectionUtils.forAllDo(chainTypes, new CollectionUtils.Closure<Web3ConfigEntity.WalletNetworkConfigChainType>() {
            @Override
            public void execute(int index, Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
                BlockFactory.get(chainType.getId()).getNftList(address, "", new BlockCallback<NFTResponse>() {
//                    @Override
//                    public void onProgress(int index, String data) {
//                        callback.onProgress(index, data);
//                    }

                    @Override
                    public void onSuccess(NFTResponse nftResponse) {
                        if (!CollectionUtils.isEmpty(nftResponse.assets)) {
                            nftInfoList.addAll(nftResponse.assets);
                        }
                    }

                    @Override
                    public void onEnd() {
                        requestTime[0] += 1;
                        if (requestTime[0] == chainTypes.size()) {
                            Collections.sort(nftInfoList);
                            callback.onSuccess(nftInfoList);
                        }
                    }
                });
            }
        });
    }

    public void getSellNFTData(NFTInfo nftInfo, BlockCallback<NFTInfo> callback) {
        Web3ConfigEntity.WalletNetworkConfigEntityItem mainCurrency = BlockchainConfig.getMainCurrency(nftInfo.chainId);
        Web3ConfigEntity.NftMarketAddress nftMarketAddress = CollectionUtils.find(MMKVUtil.getWeb3ConfigData().getNftMarketAddress(), item -> item.getChain_id() == nftInfo.chainId);
        String data = Web3AbiDataUtils.encodeGetListingData(nftInfo.contract_address, BigInteger.valueOf(nftInfo.token_id));
        BlockFactory.get(nftInfo.chainId).ethCall(nftMarketAddress.getContract_address(), data, new BlockCallback<>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                List outputParameters = new ArrayList<TypeReference<Type>>();
                outputParameters.addAll(Arrays.asList(
                        new TypeReference<Uint256>() {
                        },
                        new TypeReference<Address>() {
                        }
                ));
                List<Type> resultList = FunctionReturnDecoder.decode(data, outputParameters);
                if (CollectionUtils.isNotEmpty(resultList)) {
                    BigInteger price = ((BigInteger) resultList.get(0).getValue());
                    if (price.longValue() > 0) {
                        nftInfo.seller = (String) resultList.get(1).getValue();
                        nftInfo.price = String.valueOf(price);
                    }
                }

                nftInfo.sell_coin = mainCurrency.getName();
                nftInfo.sell_decimal = mainCurrency.getDecimal();
                nftInfo.sell_icon = mainCurrency.getIcon();
                callback.onSuccess(nftInfo);
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                nftInfo.sell_coin = mainCurrency.getName();
                nftInfo.sell_decimal = mainCurrency.getDecimal();
                nftInfo.sell_icon = mainCurrency.getIcon();
                callback.onSuccess(nftInfo);
            }
        });
    }


    /**
     * nft跳转逻辑
     * @param baseFragment
     * @param nftListAdapter
     * @param dialogId
     * @param isUserSelf
     * @param index
     */
    public void nftIndexPage(BaseFragment baseFragment, NFTAssetsAdapter nftListAdapter, long dialogId, boolean isUserSelf, int index) {
        NFTInfo nftInfo = nftListAdapter.getItem(index);

        if (!nftInfo.token_standard.endsWith("721")) {
            ToastUtils.showLong("非ERC721协议不可交易！");
            return;
        }

        if (!StringUtils.isEmpty(nftInfo.sell_coin)) {
            indexNftOperaPage(baseFragment, nftInfo, dialogId, isUserSelf);
            return;
        }

        AlertDialog progressDialog = new AlertDialog(baseFragment.getContext(), 3);
        progressDialog.setCancelable(true);
        baseFragment.showDialog(progressDialog);

        NFTGalleryManager.getInstance().getSellNFTData(nftInfo, new BlockCallback<>() {
            @Override
            public void onSuccess(NFTInfo data) {
                baseFragment.dismissCurrentDialog();
                nftListAdapter.setData(index, data);

                indexNftOperaPage(baseFragment, data, dialogId, isUserSelf);
            }
        });
    }

    private void indexNftOperaPage(BaseFragment baseFragment, NFTInfo nftInfo, long dialogId, boolean isUserSelf) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("nft_info", nftInfo);
        bundle.putLong("dialog_id", dialogId);

        if (NFTInfo.ifGrounding(nftInfo.price)) {
            if (isUserSelf) {
                baseFragment.presentFragment(new NftSellPriceActivity(bundle));
            } else {
                baseFragment.presentFragment(new NftBuyActivity(bundle));
            }
        } else {
            if (isUserSelf) {
                baseFragment.presentFragment(new NftNoPriceInfoActivity(bundle));
            }
        }
    }
}