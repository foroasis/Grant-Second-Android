package teleblock.blockchain;

import teleblock.blockchain.bnb.BnbBlockExplorer;
import teleblock.blockchain.ethereum.EthBlockExplorer;
import teleblock.blockchain.metis.MetisBlockExplorer;
import teleblock.blockchain.oasis.OasisBlockExplorer;
import teleblock.blockchain.polygon.PolygonBlockExplorer;
import teleblock.blockchain.solana.SolanaExplorer;
import teleblock.blockchain.thundercore.TTBlockExplorer;
import teleblock.blockchain.tron.TronExplorer;
import teleblock.model.Web3ConfigEntity;


public class BlockFactory {

    public static BlockExplorer get(long chainId) {
        Web3ConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(chainId);
        BlockExplorer explorer = new BlockExplorer(chainType);
        switch (chainId + "") {
            case "1":
                explorer = new EthBlockExplorer(chainType);
                break;
            case "137":
                explorer = new PolygonBlockExplorer(chainType);
                break;
            case "108":
                explorer = new TTBlockExplorer(chainType);
                break;
            case "42262":
                explorer = new OasisBlockExplorer(chainType);
                break;
            case "999":
                explorer = new TronExplorer(chainType);
                break;
            case "1088":
                explorer = new MetisBlockExplorer(chainType);
                break;
            case "56":
                explorer = new BnbBlockExplorer(chainType);
                break;
            case "99999":
                explorer = new SolanaExplorer(chainType);
                break;
        }
        return explorer;
    }

}
