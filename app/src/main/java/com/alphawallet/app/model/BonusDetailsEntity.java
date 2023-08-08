package teleblock.model;

import java.util.List;

/**
 * Time:2022/12/6
 * Author:Perry
 * Description：红包详情数据
 */
public class BonusDetailsEntity {

    public int id;
    public int user_id;
    public long tg_user_id;
    public String secret_num;
    public String tx_hash;
    public String payment_account;
    public int chain_id;
    public String chain_name;
    public int currency_id;
    public String currency_name;
    public String amount;  // 金额
    public String usd_amount;
    public String gas_amount;
    public int num; // 红包个数
    public int num_exec;  // 抢购红包个数
    public int receive_user_id;
    public int status; // 红包状态,1=发布待上链确认,2=上链确认,3=时间到期关闭,4=异常关闭,5=已经抢购完毕
    public int source; // 来源:1=群发，2=个人
    public String message; // 祝福语
    public int created_at;
    public boolean is_get;
    public List<RecordEntity> record;

    public static class RecordEntity {
        public String tx_hash;
        public String receipt_account;
        public String amount;
        public String usd_amount;
        public int status;
        public int created_at;
        public long tg_user_id;
    }
}
