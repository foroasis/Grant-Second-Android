package teleblock.model.ui;

import org.telegram.tgnet.TLRPC;

/**
 * Time:2023/2/27
 * Author:Perry
 * Description：web3社交圈用户列表数据
 */
public class Web3SocialCircleData {
    private boolean ifContacts;
    private TLRPC.User tgUserData;
    private String address;
    private String handle;
    private boolean ifActivate;//是否激活
    private boolean ifPayAttentionTo;//是否关注

    public boolean isIfContacts() {
        return ifContacts;
    }

    public void setIfContacts(boolean ifContacts) {
        this.ifContacts = ifContacts;
    }

    public TLRPC.User getTgUserData() {
        return tgUserData;
    }

    public void setTgUserData(TLRPC.User tgUserData) {
        this.tgUserData = tgUserData;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public boolean isIfActivate() {
        return ifActivate;
    }

    public void setIfActivate(boolean ifActivate) {
        this.ifActivate = ifActivate;
    }

    public boolean isIfPayAttentionTo() {
        return ifPayAttentionTo;
    }

    public void setIfPayAttentionTo(boolean ifPayAttentionTo) {
        this.ifPayAttentionTo = ifPayAttentionTo;
    }
}
