package teleblock.model;

/**
 * 创建日期：2023/3/9
 * 描述：
 */
public class GameUserEntity {

    public long tg_user_id;
    public String amount; // 投注金额
    public int status; // 中奖状态,1=待开奖,2=中奖，3=未中奖，4=退款
    public String pay_amount; // 中奖、退款金额
    public int timestamp; // 投注时间
}