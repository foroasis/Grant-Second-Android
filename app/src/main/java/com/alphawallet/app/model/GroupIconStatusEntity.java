package teleblock.model;

/**
 * Time:2023/2/1
 * Author:Perry
 * Description：群角标实体
 */
public class GroupIconStatusEntity {

    private int status;
    private String nft_image;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNft_image() {
        return nft_image;
    }

    public void setNft_image(String nft_image) {
        this.nft_image = nft_image;
    }
}
