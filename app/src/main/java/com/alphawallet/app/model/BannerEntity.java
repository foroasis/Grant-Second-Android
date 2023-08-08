package teleblock.model;

/**
 * Time:2023/1/31
 * Author:Perry
 * Description：轮播图数据
 */
public class BannerEntity {

    private int id;
    private String image;
    private String url;
    private String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
