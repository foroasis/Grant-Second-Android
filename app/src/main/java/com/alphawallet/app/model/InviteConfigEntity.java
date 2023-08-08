package teleblock.model;

import java.io.Serializable;
import java.util.List;

/**
 * 创建日期：2023/3/15
 * 描述：
 */
public class InviteConfigEntity implements Serializable {

    public String title;
    public String description;
    public String rule;
    public List<Level> level;

    public static class Level implements Serializable{
        public int id;
        public String title;
        public int numbers;
        public String amount;
        public String currency_name;
    }
}