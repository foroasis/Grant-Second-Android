package teleblock.model;

/**
 * 创建日期：2023/3/15
 * 描述：
 */
public class InviteReceiveEntity {

    public String amount;
    public String currency_name;

    public class RecordEntity {
        public int id;
        public String receipt_account;
        public long send_tg_user_id;
        public String amount;
        public long timestamp;
        public String currency_name;
    }
}