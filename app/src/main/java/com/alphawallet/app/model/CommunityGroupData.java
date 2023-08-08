package teleblock.model;

/**
 * Time:2023/3/15
 * Author:Perry
 * Description：社群首页 群列表数据
 */
public class CommunityGroupData {
    private long tg_group_id;
    private String tg_group_link;
    private String type;
    private String message;
    private PrivateGroupEntity private_group;

    private boolean ifLoadingData;

    public void setIfLoadingData(boolean ifLoadingData) {
        this.ifLoadingData = ifLoadingData;
    }

    public boolean isIfLoadingData() {
        return ifLoadingData;
    }

    public long getTg_group_id() {
        return tg_group_id;
    }

    public void setTg_group_id(long tg_group_id) {
        this.tg_group_id = tg_group_id;
    }

    public String getTg_group_link() {
        return tg_group_link;
    }

    public void setTg_group_link(String tg_group_link) {
        this.tg_group_link = tg_group_link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PrivateGroupEntity getPrivate_group() {
        return private_group;
    }

    public void setPrivate_group(PrivateGroupEntity private_group) {
        this.private_group = private_group;
    }
}
