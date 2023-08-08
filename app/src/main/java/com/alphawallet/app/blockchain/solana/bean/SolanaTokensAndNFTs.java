package teleblock.blockchain.solana.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建日期：2023/5/10
 * 描述：
 */
public class SolanaTokensAndNFTs {
    @JsonProperty("native")
    public String nativeX;
    public int lamports;
    public List<Nfts> nfts;
    public List<Tokens> tokens;

    public static class Nfts {
        public String mint;
        public String address;
        public boolean isSemiFungible;
        public String name;
        public String symbol;
        public String image;
        public int sellerFeeBasisPoints;
        public Metadata metadata;

        public static class Metadata {
            public int key;
            public String updateAuthority;
            public String mint;
            public Data data;
            public boolean primarySaleHappened;
            public boolean isMutable;
            public int editionNonce;
            public int tokenStandard;

            public static class Data {
                public String name;
                public String symbol;
                public String uri;
                public int sellerFeeBasisPoints;
                public List<Creators> creators;

                public static class Creators {
                    public String address;
                    public boolean verified;
                    public int share;
                }
            }
        }
    }

    public static class Tokens {
        public int decimals;
        public int amount;
        public String address;
        public String mint;
        public String name;
        public String symbol;
        public String image;
    }
}