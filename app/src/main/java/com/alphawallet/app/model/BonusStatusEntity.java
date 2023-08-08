package teleblock.model;

import java.io.Serializable;

/**
 * 创建日期：2022/12/6
 * 描述：红包状态数据
 */
public class BonusStatusEntity implements Serializable {

    public int id;
    public long user_id;
    public String secret_num; // 红包密钥
    public String tx_hash; // 付款的hash
    public String payment_account; // 付款的地址
    public int chain_id;
    public String chain_name;
    public int currency_id;
    public String currency_name;
    public String amount; // 金额
    public String gas_amount;
    public int num; // 红包个数
    public int num_exec; // 抢购红包个数
    public int receive_user_id;
    public int status; // 红包状态,1=发布待上链确认,2=上链确认,3=时间到期关闭,4=异常关闭,5=已经抢购完毕
    public int source; // 来源:1=群发，2=个人
    public int created_at; // 发送红包时间
    public boolean is_get; // 是否已经抢购
    public long tg_user_id; // 发送者id
    public String message; // 祝福语
}