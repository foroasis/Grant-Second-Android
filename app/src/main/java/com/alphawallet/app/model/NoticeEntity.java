package teleblock.model;

import java.io.Serializable;
import java.util.List;

public class NoticeEntity implements Serializable {
    public int id;
    public String type;
    public String icon;
    public String title;
    public String content;
    public String link;
    public List<String> logic_lang;
    public List<String> logic_area_code;
    public int logic_is_admin;
    public int admin_group_numbers;
    public int logic_is_wallet;
    public int logic_is_active_chain;
    public long timestamp;
    public long user_id;
}
