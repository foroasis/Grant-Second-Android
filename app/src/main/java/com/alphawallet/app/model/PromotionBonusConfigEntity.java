package teleblock.model;

import java.io.Serializable;
import java.util.List;

/**
 * Time:2023/4/4
 * Author:Perry
 * Description：欢迎红包数据配置
 */
public class PromotionBonusConfigEntity implements Serializable{
    private String address;
    private String tron_address;
    private List<BonusConfigEntity.Config> tokens;
    private ChatInfo chat_info;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTron_address() {
        return tron_address;
    }

    public void setTron_address(String tron_address) {
        this.tron_address = tron_address;
    }

    public List<BonusConfigEntity.Config> getTokens() {
        return tokens;
    }

    public void setTokens(List<BonusConfigEntity.Config> tokens) {
        this.tokens = tokens;
    }

    public ChatInfo getChat_info() {
        return chat_info;
    }

    public void setChat_info(ChatInfo chat_info) {
        this.chat_info = chat_info;
    }

    public class ChatInfo {
        private long id;
        private String link;
        private String title;
        private String image;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
