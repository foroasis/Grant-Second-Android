package teleblock.model.wallet;

import java.util.List;


/**
 * Created by LPC43 on 2018/4/12.
 */

public class NFTResponse {

    public List<NFTInfo> assets;

    public String next;

    public NFTResponse() {
    }

    public NFTResponse(List<NFTInfo> nftInfoList) {
        assets = nftInfoList;
    }
}
