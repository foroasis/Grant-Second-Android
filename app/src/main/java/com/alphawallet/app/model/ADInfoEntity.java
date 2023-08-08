package teleblock.model;

import com.google.gson.annotations.SerializedName;

/**
 * 创建日期：2022/3/16
 * 描述：广告配置
 */
public class ADInfoEntity {

    public String name; // 广告名称
    @SerializedName("switch")
    public boolean switchX; // 广告开关
    public int type; // 广告平台
    public DetailEntity detail; // 广告数据

    public static class DetailEntity {
        public int id;
        public int target; // 打开方式
        public int style; // 广告样式
        public boolean adflag; // 广告标识
        public String icon;
        public String media;
        public String title;
        public String desc;
        public String btn;
        public String url;
    }
}