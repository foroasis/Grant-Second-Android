package teleblock.model;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import teleblock.model.wallet.ETHWallet;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;

/**
 * Time:2022/12/6
 * Author:Perry
 * Description：红包配置接口
 */
public class BonusConfigEntity implements Serializable {
    private String address;
    private String tron_address;
    private String solana_address;
    private List<Config> config;

    public class Config implements Serializable {
        private int id;
        private String name;
        private String icon;
        private List<Currency> currency;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public List<Currency> getCurrency() {
            return currency;
        }

        public void setCurrency(List<Currency> currency) {
            this.currency = currency;
        }
    }

    public class Currency implements Serializable {
        private int id;
        private int decimal;
        private String name;
        private double gas_price;
        private boolean is_main_currency;
        private String icon;
        private int max_num;
        private String min_price;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDecimal() {
            return decimal;
        }

        public void setDecimal(int decimal) {
            this.decimal = decimal;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getGas_price() {
            return gas_price;
        }

        public void setGas_price(double gas_price) {
            this.gas_price = gas_price;
        }

        public boolean isIs_main_currency() {
            return is_main_currency;
        }

        public void setIs_main_currency(boolean is_main_currency) {
            this.is_main_currency = is_main_currency;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getMax_num() {
            return max_num;
        }

        public void setMax_num(int max_num) {
            this.max_num = max_num;
        }

        public String getMin_price() {
            return min_price;
        }

        public void setMin_price(String min_price) {
            this.min_price = min_price;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTron_address() {
        return tron_address;
    }

    public void setTron_address(String tron_address) {
        this.tron_address = tron_address;
    }

    public List<Config> getConfig() {
//        ETHWallet wallet = WalletDaoUtils.getCurrent();
//        if (wallet != null && !StringUtils.isEmpty(wallet.getAddress())) {
//            boolean isEvmAddress = WalletUtil.isEvmAddress(wallet.getAddress());
//            boolean isTronAddress = WalletUtil.isTronAddress(wallet.getAddress());
//            boolean isSolanaAddress = WalletUtil.isSolanaAddress(wallet.getAddress());
//            CollectionUtils.filter(config, new CollectionUtils.Predicate<Config>() {
//                @Override
//                public boolean evaluate(Config item) {
//                    if (isSolanaAddress) {
//                        return item.id == 99999;
//                    } else if (isTronAddress) {
//                        return item.id == 999;
//                    } else if (isEvmAddress) {
//                        return item.id != 99999 && item.id != 999;
//                    }
//                    return false;
//                }
//            });
//        }
        return config;
    }

    public void setConfig(List<Config> config) {
        this.config = config;
    }

    public String getSolana_address() {
        return solana_address;
    }

    public void setSolana_address(String solana_address) {
        this.solana_address = solana_address;
    }
}
