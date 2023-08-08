package teleblock.model.ui;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Time:2023/3/6
 * Author:Perry
 * Description：web3列表数据
 */
public class Web3SocialCircleNodeData implements MultiItemEntity {

    public static final int TYPE_TITLE = 1;
    public static final int TYPE_DATA = 2;

    private int itemType;
    private String title;
    private Web3SocialCircleData data;

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Web3SocialCircleData getData() {
        return data;
    }

    public void setData(Web3SocialCircleData data) {
        this.data = data;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
