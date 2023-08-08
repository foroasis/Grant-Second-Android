package teleblock.model;

import java.util.List;

/**
 * Time:2023/3/15
 * Author:Perry
 * Description：
 */
public class MintAssignmentList {

    private String id;
    private String home_title;
    private String home_description;
    private String home_icon;
    private String home_link;
    private String home_type;
    private String home_type_color;
    private int home_amount;
    private String home_check_config;
    private List<HomeBtn> home_btn;

    private boolean ifLoadingData;

    public String getHome_check_config() {
        return home_check_config;
    }

    public void setHome_check_config(String home_check_config) {
        this.home_check_config = home_check_config;
    }

    public void setIfLoadingData(boolean ifLoadingData) {
        this.ifLoadingData = ifLoadingData;
    }

    public boolean isIfLoadingData() {
        return ifLoadingData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHome_description() {
        return home_description;
    }

    public void setHome_description(String home_description) {
        this.home_description = home_description;
    }

    public String getHome_icon() {
        return home_icon;
    }

    public void setHome_icon(String home_icon) {
        this.home_icon = home_icon;
    }

    public String getHome_link() {
        return home_link;
    }

    public void setHome_link(String home_link) {
        this.home_link = home_link;
    }

    public String getHome_type() {
        return home_type;
    }

    public void setHome_type(String home_type) {
        this.home_type = home_type;
    }

    public String getHome_type_color() {
        return home_type_color;
    }

    public void setHome_type_color(String home_type_color) {
        this.home_type_color = home_type_color;
    }

    public void setHome_amount(int home_amount) {
        this.home_amount = home_amount;
    }

    public int getHome_amount() {
        return home_amount;
    }

    public String getHome_title() {
        return home_title;
    }

    public void setHome_title(String home_title) {
        this.home_title = home_title;
    }

    public List<HomeBtn> getHome_btn() {
        return home_btn;
    }

    public void setHome_btn(List<HomeBtn> home_btn) {
        this.home_btn = home_btn;
    }

    public static class HomeBtn {
        private String icon;
        private String text;

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
