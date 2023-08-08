package teleblock.model;

import org.json.JSONObject;

import java.util.List;

/**
 * 创建日期：2023/3/9
 * 描述：
 */
public class GameInfoEntity {

    public String game_number; // 游戏编号
    public String amount_pool; // 奖池金额
    public String amount_lock; // 锁定金额
    public String receipt_account; // 收款地址
    public long chain_id;
    public String chain_name;
    public long currency_id;
    public String currency_name;
    public String bet_type; // 玩法：odd-even，big-small
    public List<Integer> bet_amount_list; // 投注金额列表
    public List<Integer> multiple_range; // 倍数，1-500倍
    public double game_odds; // 赔率
    public JSONObject rules;

}