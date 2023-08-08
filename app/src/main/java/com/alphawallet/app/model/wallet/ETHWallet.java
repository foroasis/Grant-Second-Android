package teleblock.model.wallet;

import org.walletconnect.Session;

import teleblock.blockchain.BlockchainConfig;

/**
 * 钱包账号实体类
 */
public class ETHWallet {
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CREATE_OR_IMPORT = 99;
    public static final int TYPE_TSS = 100;
    public static final Integer[] TYPE_CONNECT = new Integer[]{1, 2, 3, 4, 5, 6, 10};

    private long id;
    private String address;
    private String name;
    private String password;
    private String keystorePath;
    private String mnemonic;
    private boolean isCurrent;
    private boolean isBackup;

    // 类型  0；默认创建的 ，99：创建或导入的  , 100：托管钱包；
    // WalletConnect  1：MetaMask，2：Trust，3：Spot，4：TokenPocket，5：imToken，6：TTWallet 10：Phantom
    private int walletType;
    private String connectedWalletPkg;
    private Session.Config config;
    private long chainId;

    public boolean isBackup() {
        return isBackup;
    }

    public void setBackup(boolean backup) {
        isBackup = backup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public boolean getIsCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public boolean getIsBackup() {
        return this.isBackup;
    }

    public void setIsBackup(boolean isBackup) {
        this.isBackup = isBackup;
    }

    public int getWalletType() {
        return walletType;
    }

    public void setWalletType(int walletType) {
        this.walletType = walletType;
    }

    public String getConnectedWalletPkg() {
        return connectedWalletPkg == null ? "" : connectedWalletPkg;
    }

    public void setConnectedWalletPkg(String connectedWalletPkg) {
        this.connectedWalletPkg = connectedWalletPkg;
    }

    public Session.Config getConfig() {
        return config;
    }

    public void setConfig(Session.Config config) {
        this.config = config;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }
}
