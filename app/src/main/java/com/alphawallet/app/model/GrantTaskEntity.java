package teleblock.model;

import java.io.Serializable;
import java.util.List;

/**
 * 创建日期：2023/1/31
 * 描述：
 */
public class GrantTaskEntity implements Serializable {

    public BannerInfoEntity banner_info;
    public List<TaskInfoEntity> task_info;
    public ContractInfoEntity contract_info;
    public GroupInfoEntity airdrop_group_info;
    public String notice;

    public static class BannerInfoEntity {
        public String image;
    }

    public static class TaskInfoEntity {
        public String name;
        public int status;
        public String url;
    }

    public static class ContractInfoEntity {
        public long chain_id;
        public String address;
        public long token_id;
        public int is_cyber_connect;
        public String profileID;
        public String essenceID;
    }

    public static class GroupInfoEntity {
        public long group_id;
        public long chat_id;
        public int is_join_group;
    }
}