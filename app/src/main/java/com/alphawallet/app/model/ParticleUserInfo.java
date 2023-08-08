package teleblock.model;

import java.util.List;

/**
 * Time：2023/6/13
 * Creator：LSD
 * Description：
 */
public class ParticleUserInfo {
    public String uuid;
    public String phone;
    public String email;
    public String name;
    public String avatar;
    public String facebookId;
    public String facebookEmail;
    public String googleId;
    public String googleEmail;
    public String appleId;
    public String appleEmail;
    public String twitterId;
    public String twitterEmail;
    public String telegramId;
    public String telegramPhone;
    public String discordId;
    public String discordEmail;
    public String githubId;
    public String githubEmail;
    public String twitchId;
    public String twitchEmail;
    public String microsoftId;
    public String microsoftEmail;
    public String linkedinId;
    public String linkedinEmail;
    public String jwtId;
    public String createdAt;
    public String updatedAt;
    public List<WalletsEntity> wallets;

    public static class WalletsEntity {
        public String chain;
        public String publicAddress;
    }
}
