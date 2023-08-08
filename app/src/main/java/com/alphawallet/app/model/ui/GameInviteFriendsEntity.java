package teleblock.model.ui;

import org.telegram.tgnet.TLRPC;

/**
 * Time:2023/5/6
 * Author:Perry
 * Description：游戏邀请朋友数据
 */
public class GameInviteFriendsEntity {

    private TLRPC.User user;
    private boolean ifInvite;

    public TLRPC.User getUser() {
        return user;
    }

    public void setUser(TLRPC.User user) {
        this.user = user;
    }

    public boolean isIfInvite() {
        return ifInvite;
    }

    public void setIfInvite(boolean ifInvite) {
        this.ifInvite = ifInvite;
    }
}
