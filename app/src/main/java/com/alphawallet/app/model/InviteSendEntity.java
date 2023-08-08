package teleblock.model;

import java.util.List;

/**
 * 创建日期：2023/3/15
 * 描述：
 */
public class InviteSendEntity {

    public String total; // 我发的总个数
    public String earn_amount; // 我领取的金额
    public String unreceived_amount;  // 待领取金额
    public String currency_name;

    public class RecordEntity {
        public int id;
        public int level_id;
        public int status;
        public String promotion_number;
        public String amount; // 金额
        public int users_number; // 发送数量
        public int users_number_exec; // 抢购数量
        public int timestamp;
        public String currency_name;
        public List<Long> users;
        public int type;
    }
}