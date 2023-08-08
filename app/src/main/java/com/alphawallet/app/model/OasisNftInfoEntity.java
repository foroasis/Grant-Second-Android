package teleblock.model;

/**
 * Time:2023/5/17
 * Author:Perry
 * Description：oasis nft数据
 */
public class OasisNftInfoEntity {

    private int nft_token_id;
    private String nft_name;
    private String nft_image;

    public int getNft_token_id() {
        return nft_token_id;
    }

    public void setNft_token_id(int nft_token_id) {
        this.nft_token_id = nft_token_id;
    }

    public String getNft_name() {
        return nft_name;
    }

    public void setNft_name(String nft_name) {
        this.nft_name = nft_name;
    }

    public String getNft_image() {
        return nft_image;
    }

    public void setNft_image(String nft_image) {
        this.nft_image = nft_image;
    }
}
