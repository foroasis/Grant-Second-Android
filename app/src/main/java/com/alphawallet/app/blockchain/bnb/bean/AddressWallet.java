package teleblock.blockchain.bnb.bean;

import java.util.List;

/**
 * Time:2023/2/27
 * Author:Perry
 * Description：
 */
public class AddressWallet {

    public Wallet wallet;

    public class Wallet {
        public Profiles profiles;
        public String address;
    }

    public class Profiles {
        public List<Edges> edges;
    }

    public class Edges {
        public Node node;
    }

    public class Node {
        public String handle;
        public boolean isFollowedByMe;
        public int profileID;

        public String tokenURI;
        public String symbol;
        public String name;
    }
}
