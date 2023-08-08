package teleblock.model;

import java.util.List;

/**
 * Time:2023/1/13
 * Author:Perry
 * Description：社群首页功能数据
 */
public class CommunityFuntionEntity {
    private String title;
    private String icon;
    private String link;
    private List<String> btn;
    private boolean ifLoadingData;

    public void setIfLoadingData(boolean ifLoadingData) {
        this.ifLoadingData = ifLoadingData;
    }

    public boolean isIfLoadingData() {
        return ifLoadingData;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getBtn() {
        return btn;
    }

    public void setBtn(List<String> btn) {
        this.btn = btn;
    }
}
