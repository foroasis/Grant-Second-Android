package teleblock.model;

import java.util.List;

/**
 * 创建日期：2023/3/22
 * 描述：
 */
public class DiceConfigEntity {

    public List<Tokens> tokens;
    public List<Integer> victory_number;
    public String address;

    public static class Tokens {
        public int id;
        public long chain_id;
        public int decimal;
        public String name;
        public String amount;
        public boolean is_main_currency;
        public String icon;
        public String contract_address;
    }
}