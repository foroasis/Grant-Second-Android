package teleblock.model;

import org.telegram.tgnet.TLRPC;

/**
 * 创建日期：2023/3/15
 * 描述：
 */
public class InviteFriendEntity {

    public long tg_user_id; // 已发送的用户tgId
    public int status; // 0=未邀请，1=未领取，2=已领取

    public transient TLRPC.User user; // 联系人
    public transient boolean checked;
}