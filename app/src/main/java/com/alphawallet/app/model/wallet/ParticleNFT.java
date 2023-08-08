package teleblock.model.wallet;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.Web3ConfigEntity;

/**
 * 创建日期：2022/12/30
 * 描述：
 */
public class ParticleNFT {

    public String address;
    public boolean isSemiFungible;
    public long tokenId;
    public String tokenBalance;
    public String tokenURI;
    public String name;
    public String symbol;
    public String image;
    public DataEntity data;

    public static class DataEntity {
        public String image;
        public String animation_url;
        public String name;
        public String description;
        public List<?> attributes;
        public String external_link;
        public String animation;
    }


    public static List<NFTInfo> pares(List<ParticleNFT> nftList, Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        List<NFTInfo> nftInfoList = new ArrayList<>();
        try {
            for (ParticleNFT particleNFT : nftList) {
                NFTInfo nftInfo = new NFTInfo();
                nftInfo.name = particleNFT.name;
                if ("alpha - CyberConnect NFT".equals(nftInfo.name)) {
                    nftInfo.name = "alphagram x CyberConnect NFT";
                }
                nftInfo.description = particleNFT.data.description;
                nftInfo.thumb_url = particleNFT.image.replace("ipfs://", "https://ipfs.io/ipfs/");
                nftInfo.setOriginal_url(nftInfo.thumb_url);
                nftInfo.token_id = particleNFT.tokenId;
                nftInfo.symbol = particleNFT.symbol;
                nftInfo.contract_address = particleNFT.address;
                nftInfo.blockchain = chainType.getName();
                nftInfo.token_standard = particleNFT.isSemiFungible ? "ERC1155" : "ERC721";
                nftInfo.chainId = chainType.getId();
                nftInfoList.add(nftInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nftInfoList;
    }
}