package teleblock.model;

import teleblock.model.wallet.ETHWallet;

public class WalletHubEntity {
    public int itemType;
    public ETHWallet wallet;
    public String title;

    public WalletHubEntity(String title) {
        this.itemType = 0;
        this.title = title;
    }

    public WalletHubEntity(ETHWallet wallet) {
        this.itemType = 1;
        this.wallet = wallet;
    }

}
