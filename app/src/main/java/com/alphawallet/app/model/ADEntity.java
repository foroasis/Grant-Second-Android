package teleblock.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LSD on 2022/3/3.
 * Desc
 */
public class ADEntity {
    public ADConfig home_bottom_banner = new ADConfig();// 首页底部banner   =>固定api广告
    public ADConfig chat_bottom_banner = new ADConfig(); // 聊天页面底部banner  =>固定api广告
    public ADConfig video_home_popup = new ADConfig(); // 视频首页弹窗广告
    public ADConfig video_view_slide = new ADConfig(); // 视频滑动页广告
    public ADConfig new_list_flow = new ADConfig(); // 资讯列表页信息流广告

    public ADConfig chat_top_banner = new ADConfig(); // 聊天界面顶部广告
    public ADConfig chat_message_bottom = new ADConfig();// 聊天界面消息尾部广告
    public ADConfig chat_message_flow = new ADConfig(); // 聊天界面间隔广告
    public ADConfig media_view_bottom = new ADConfig(); // 媒体预览底部广告
    public ADConfig web_view_bottom = new ADConfig(); // 网页详情底部广告
    public ADConfig home_message_flow = new ADConfig(); // 首页消息间隔广告

    public ADConfig home_flow_button = new ADConfig(); // 首页浮窗button
    public ADConfig web_view_popup = new ADConfig(); // 首页网页详情弹窗


    public static class ADConfig {
        @SerializedName("switch")
        public boolean adSwitch;
        public int type;
        public ADDetail detail;
    }

    public static class ADDetail {
        public String icon;
        public String title;
        public String desc;
        public String btn;
        public String url;
        public String media;
    }

    public boolean hasADOpened() {
        return home_bottom_banner.adSwitch
                || chat_bottom_banner.adSwitch
                || video_home_popup.adSwitch
                || video_view_slide.adSwitch
                || new_list_flow.adSwitch
                || chat_top_banner.adSwitch
                || chat_message_bottom.adSwitch
                || chat_message_flow.adSwitch
                || media_view_bottom.adSwitch
                || web_view_bottom.adSwitch
                || home_message_flow.adSwitch
                || home_flow_button.adSwitch
                || web_view_popup.adSwitch;
    }
}
