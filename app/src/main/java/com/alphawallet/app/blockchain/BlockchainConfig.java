
package teleblock.blockchain;


import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.R;

import java.util.List;

import teleblock.model.Web3ConfigEntity;
import teleblock.util.MMKVUtil;

/**
 * 创建日期：2022/8/29
 * 描述：
 */
public class BlockchainConfig {

    public static final String PKG_META_MASK = "io.metamask";
    public static final String PKG_IMTOKEN = "im.token.app";
    public static final String PKG_TRUST_WALLET = "com.wallet.crypto.trustapp";
    public static final String PKG_TOKEN_POCKET = "vip.mytokenpocket";
    public static final String PKG_SPOT_WALLET = "com.spot.spot";
    public static final String PKG_TT_WALLET = "com.thundercore.mobile";
    public static final String PKG_PHANTOM = "app.phantom";
    public static final String PKG_MY_WALLET = "app.alphagram.messenger";//我们内部去中心化钱包
    public static final String PKG_MY_TSS_WALLET = "app.alphagram.messenger";//我们内部tss钱包

    /**
     * 钱包 图标
     */
    public enum WalletIconType {
        METAMASK(1, "MetaMask", "MetaMask", PKG_META_MASK, "metamask", R.drawable.logo_meta_mask_connect_wallet),
        TRUSTWALLET(2, "Trust Wallet Android", "Trust Wallet", PKG_TRUST_WALLET, "trust", R.drawable.logo_trust_connect_wallet),
        SPOTWALLET(3, "Spot Wallet", "Spot Wallet", PKG_SPOT_WALLET, "spot", R.drawable.logo_spot_connect_wallet),
        TOKENPOCKET(4, "TokenPocket", "TokenPocket", PKG_TOKEN_POCKET, "token_pocket", R.drawable.logo_token_pocket_connect_wallet),
        IMTOKEN(5, "imToken", "imToken", PKG_IMTOKEN, "im_token", R.drawable.logo_imtoken_wallet),
        TT_WALLET(6, "TT Wallet", "TT Wallet", PKG_TT_WALLET, "tt_wallet", R.drawable.logo_tt_connect_wallet),
        PHANTOM(10, "Phantom Wallet", "Phantom Wallet", PKG_PHANTOM, "phantom", R.drawable.logo_phantom_wallet),
        MY_WALLET(99, "My Wallet", "My Wallet", PKG_MY_WALLET, "my_wallet", R.drawable.ic_paticle_wallet84),
        MY_TSS_WALLET(100, "Particle Network", "Particle Network", PKG_MY_TSS_WALLET, "my_tss_wallet", R.drawable.ic_paticle_wallet84),
        ;

        public int typeId;
        public String fullName;
        public String walletName;
        public String pkg;
        public String walletType;
        public int icon;

        WalletIconType(int typeId, String fullName, String walletName, String pkg, String walletType, int icon) {
            this.typeId = typeId;
            this.fullName = fullName;
            this.walletName = walletName;
            this.pkg = pkg;
            this.walletType = walletType;
            this.icon = icon;
        }
    }

    /**
     * 获取钱包包名
     */
    public static String getPkgByFullName(String fullName) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.fullName.equals(fullName)) {
                return walletIconType.pkg;
            }
        }
        return "";
    }

    /***
     * 获取钱包配置
     *
     * @param fullName
     * @return
     */
    public static WalletIconType getWalletTypeByFullName(String fullName) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.fullName.equals(fullName)) {
                return walletIconType;
            }
        }
        return null;
    }

    /***
     * 通过类型id获取
     * @param typeId
     * @return
     */
    public static WalletIconType getWalletTypeByTypeId(String typeId) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (typeId.equals(walletIconType.typeId + "")) {
                return walletIconType;
            }
        }
        return null;
    }

    public static WalletIconType getWalletTypeByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (!TextUtils.isEmpty(pkg) && pkg.equals(walletIconType.pkg)) {
                return walletIconType;
            }
        }
        return null;
    }

    /***
     * 通过类型获取
     * @param walletType
     * @return
     */
    public static WalletIconType getWalletTypeByTypeName(String walletType) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletType.equals(walletIconType.walletType + "")) {
                return walletIconType;
            }
        }
        return null;
    }

    /**
     * 获取钱包图标
     *
     * @param pkg
     * @return
     */
    public static int getWalletIconByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equalsIgnoreCase(pkg)) {
                return walletIconType.icon;
            }
        }
        return R.drawable.ic_chat_bottom_func_wallet;
    }

    //钱包小图标
    public static int getWalletFromIconByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equalsIgnoreCase(pkg)) {
                return walletIconType.icon;
            }
        }
        return R.drawable.ic_paticle_wallet;
    }

    /**
     * 获取钱包名称
     */
    public static String getWalletNameByPkg(String pkg) {
        for (WalletIconType walletIconType : WalletIconType.values()) {
            if (walletIconType.pkg.equals(pkg)) {
                return walletIconType.walletName;
            }
        }
        return "";
    }

    /**
     * 获取区块链配置
     */
    public static Web3ConfigEntity.WalletNetworkConfigChainType getChainType(long chainId) {
        List<Web3ConfigEntity.WalletNetworkConfigChainType> chainTypes = MMKVUtil.getWeb3ConfigData().getChainType();
        return CollectionUtils.find(chainTypes, item -> chainId == item.getId());
    }

    /**
     * 获取主币配置
     */
    public static Web3ConfigEntity.WalletNetworkConfigEntityItem getMainCurrency(long chainId) {
        return getMainCurrency(getChainType(chainId));
    }

    public static Web3ConfigEntity.WalletNetworkConfigEntityItem getMainCurrency(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        return CollectionUtils.find(chainType.getCurrency(), item -> item.isIs_main_currency());
    }
}