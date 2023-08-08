package teleblock.model.ui;

/**
 * Time:2023/1/13
 * Author:Perry
 * Description：公开群数据
 */
public class PublicGroupInfoData {
    private String name;
    private String avatar;
    private String num;
    public int type;// 1= channel, 2= group

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
