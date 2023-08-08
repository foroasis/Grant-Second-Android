package teleblock.model.wallet;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import teleblock.blockchain.BlockchainConfig;
import teleblock.model.Web3ConfigEntity;
import teleblock.util.JsonUtil;


public class OpenSeaAssets {

    public String next;
    public Object previous;
    public List<AssetsEntity> assets; // v1
    public List<AssetsEntity> results; // v2

    public static NFTResponse parse(String result, long chain_id) {
        OpenSeaAssets openSeaAssets = JsonUtil.parseJsonToBean(result, OpenSeaAssets.class);
        NFTResponse nftResponse = new NFTResponse();
        List<NFTInfo> nftInfoList = new ArrayList<>();
        if (openSeaAssets == null) {
            return nftResponse;
        }
        List<AssetsEntity> list;
        if (openSeaAssets.assets == null) {
            list = openSeaAssets.results;
        } else {
            list = openSeaAssets.assets;
        }
        if (list == null) {
            return nftResponse;
        }
        for (AssetsEntity assetsEntity : list) {
//            if (assetsEntity.collection.hidden) {
//                continue; // 先这样过滤
//            }
            NFTInfo nftInfo = createNft(assetsEntity, chain_id);
            if (nftInfo != null && nftInfo.token_id != 0) {
                nftInfoList.add(nftInfo);
            }
        }
        nftResponse.assets = nftInfoList;
        nftResponse.next = openSeaAssets.next;
        return nftResponse;
    }

    public static NFTInfo createNft(AssetsEntity assetsEntity, long chain_id) {
        try {
            NFTInfo nftInfo = new NFTInfo();
            nftInfo.name = TextUtils.isEmpty(assetsEntity.name) ? assetsEntity.asset_contract.name : assetsEntity.name;
            if ("alpha - CyberConnect NFT".equalsIgnoreCase(nftInfo.name)) {
                nftInfo.name = "alphagram x CyberConnect NFT";
            }
            nftInfo.description = TextUtils.isEmpty(assetsEntity.description) ? assetsEntity.asset_contract.description : assetsEntity.description;
            nftInfo.thumb_url = TextUtils.isEmpty(assetsEntity.image_thumbnail_url) ? assetsEntity.asset_contract.image_url : assetsEntity.image_thumbnail_url;
            nftInfo.setOriginal_url(TextUtils.isEmpty(assetsEntity.image_url) ? assetsEntity.asset_contract.image_url : assetsEntity.image_url);
            nftInfo.token_id = assetsEntity.token_id;
            nftInfo.contract_address = assetsEntity.asset_contract.address;
            nftInfo.symbol = assetsEntity.asset_contract.symbol;
            if (!CollectionUtils.isEmpty(assetsEntity.seaport_sell_orders)) {
                nftInfo.price = assetsEntity.seaport_sell_orders.get(0).current_price;
            } else if (assetsEntity.last_sale != null) {
                nftInfo.price = assetsEntity.last_sale.total_price;
            }
            nftInfo.token_standard = assetsEntity.asset_contract.schema_name;
            Web3ConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(chain_id);
            if (chainType != null) {
                nftInfo.blockchain = chainType.getName();
            }
            nftInfo.chainId = chain_id;
            return nftInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public static class AssetsEntity {
        public int id;
        public int num_sales;
        public String image_url;
        public String image_preview_url;
        public String image_thumbnail_url;
        public String image_original_url;
        public String animation_url;
        public String animation_original_url;
        public String name;
        public String description;
        public String external_link;
        public AssetContractEntity asset_contract;
        public String permalink;
        public CollectionEntity collection;
        public List<SeaportSellOrdersEntity> seaport_sell_orders;
        public LastSaleEntity last_sale;
        public boolean is_presale;
        public long token_id;

        public static class AssetContractEntity {
            public String address;
            public String asset_contract_type;
            public String created_date;
            public String name;
            public String nft_version;
            public Object opensea_version;
            public String owner;
            public String schema_name;
            public String symbol;
            public String total_supply;
            public String description;
            public String external_link;
            public String image_url;
            public boolean default_to_fiat;
            public int dev_buyer_fee_basis_points;
            public int dev_seller_fee_basis_points;
            public boolean only_proxied_transfers;
            public int opensea_buyer_fee_basis_points;
            public int opensea_seller_fee_basis_points;
            public int buyer_fee_basis_points;
            public int seller_fee_basis_points;
            public String payout_address;
        }

        public static class CollectionEntity {
            public String banner_image_url;
            public Object chat_url;
            public String created_date;
            public boolean default_to_fiat;
            public String description;
            public String dev_buyer_fee_basis_points;
            public String dev_seller_fee_basis_points;
            public Object discord_url;
            public DisplayDataEntity display_data;
            public String external_url;
            public boolean featured;
            public String featured_image_url;
            public boolean hidden;
            public String safelist_request_status;
            public String image_url;
            public boolean is_subject_to_whitelist;
            public String large_image_url;
            public Object medium_username;
            public String name;
            public boolean only_proxied_transfers;
            public String opensea_buyer_fee_basis_points;
            public String opensea_seller_fee_basis_points;
            public String payout_address;
            public boolean require_email;
            public Object short_description;
            public String slug;
            public Object telegram_url;
            public String twitter_username;
            public Object instagram_username;
            public Object wiki_url;
            public boolean is_nsfw;

            public static class DisplayDataEntity {
                public String card_display_style;
            }
        }

        public static class OwnerEntity {
            public UserEntity user;
            public String profile_img_url;
            public String address;
            public String config;

            public static class UserEntity {
                public String username;
            }
        }

        public static class CreatorEntity {
            public UserEntity user;
            public String profile_img_url;
            public String address;
            public String config;

            public static class UserEntity {
                public String username;
            }
        }

        public static class LastSaleEntity {
            public AssetEntity asset;
            public Object asset_bundle;
            public String event_type;
            public String event_timestamp;
            public Object auction_type;
            public String total_price;
            public PaymentTokenEntity payment_token;
            public Object transaction;
            public String created_date;
            public String quantity;

            public static class AssetEntity {
                public Object decimals;
                public String token_id;
            }

            public static class PaymentTokenEntity {
                public String symbol;
                public String address;
                public String image_url;
                public String name;
                public int decimals;
                public String eth_price;
                public String usd_price;
            }
        }

        public static class SeaportSellOrdersEntity {
            public String created_date;
            public String closing_date;
            public int listing_time;
            public int expiration_time;
            public String order_hash;
            public String protocol_address;
            public String current_price;
            public String side;
            public String order_type;
            public boolean cancelled;
            public boolean finalized;
            public boolean marked_invalid;
            public String client_signature;
            public String relay_id;
        }
    }
}
