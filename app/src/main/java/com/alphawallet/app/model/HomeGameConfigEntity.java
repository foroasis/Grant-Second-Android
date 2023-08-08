package teleblock.model;

import java.util.List;

/**
 * Time:2023/3/31
 * Author:Perry
 * Description：首页小游戏配置接口
 */
public class HomeGameConfigEntity {

    private String address;
    private List<Tokens> tokens;
    private Rules rules;
    private int free_times;
    private int pay_times;
    private int times;
    private String symbol;
    private String icon;
    private int daily_pay_times;
    private int daily_max_pay_times;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Tokens> getTokens() {
        return tokens;
    }

    public void setTokens(List<Tokens> tokens) {
        this.tokens = tokens;
    }


    public int getFree_times() {
        return free_times;
    }

    public void setFree_times(int free_times) {
        this.free_times = free_times;
    }

    public int getPay_times() {
        return pay_times;
    }

    public void setPay_times(int pay_times) {
        this.pay_times = pay_times;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public int getDaily_pay_times() {
        return daily_pay_times;
    }

    public void setDaily_pay_times(int daily_pay_times) {
        this.daily_pay_times = daily_pay_times;
    }

    public int getDaily_max_pay_times() {
        return daily_max_pay_times;
    }

    public void setDaily_max_pay_times(int daily_max_pay_times) {
        this.daily_max_pay_times = daily_max_pay_times;
    }

    public class Tokens {
        private int id;
        private int chain_id;
        private int decimal;
        private String name;
        private String amount;
        private boolean is_main_currency;
        private String contract_address;
        private String icon;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getChain_id() {
            return chain_id;
        }

        public void setChain_id(int chain_id) {
            this.chain_id = chain_id;
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

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public boolean isIs_main_currency() {
            return is_main_currency;
        }

        public void setIs_main_currency(boolean is_main_currency) {
            this.is_main_currency = is_main_currency;
        }

        public String getContract_address() {
            return contract_address;
        }

        public void setContract_address(String contract_address) {
            this.contract_address = contract_address;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    public class Rules {
        private String header;
        private String image;
        private String footer;

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getFooter() {
            return footer;
        }

        public void setFooter(String footer) {
            this.footer = footer;
        }
    }
}