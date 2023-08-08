package teleblock.model;

/**
 * 创建日期：2023/3/10
 * 描述：
 */
public class BetStatusEntity {

    public String tx_hash; // 交易hash
    public int block_status; // 上链状态 1=待上链确认,2=上链确认，3=异常数据
    public int status; // 中奖状态,1=待开奖,2=中奖，3=未中奖，4=退款
    public String pay_amount; // 中奖/退款金额
    public String block_hash; // 区块hash
}