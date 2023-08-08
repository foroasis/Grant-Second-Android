package teleblock.model.wallet;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;

import org.web3j.utils.Convert;

import java.io.Serializable;

/**
 * 创建日期：2022/5/11
 * 描述：
 */
public class NFTInfo implements Serializable, Comparable<NFTInfo> {

    public String name; // 名称
    public String description; // 描述
    public String thumb_url; // 缩略图地址
    private String original_url; // 原始图地址
    public String contract_address; // 合约地址
    public String symbol; // 代币符号
    public String token_standard; // 代币标准
    public String blockchain; // 所在区块链
    public long token_id; // 代币id
    public long chainId; // 链id

    public String seller; // 卖家地址
    public String price; // 售卖价格
    public String sell_coin; // 售卖币种
    public int sell_decimal; // 售卖币精度
    public String sell_icon; // 售卖币图标

    public String getOriginal_url() {
        return original_url == null ? "" : original_url;
    }

    public void setOriginal_url(String original_url) {
        this.original_url = original_url;
    }

    public String getEthPrice() {
        if (TextUtils.isEmpty(price)) {
            return "";
        }
        return Convert.fromWei(price, Convert.Unit.ETHER).toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof NFTInfo) {
            NFTInfo info = (NFTInfo) obj;
            return info.chainId == chainId && info.token_id == token_id && TextUtils.equals(info.contract_address, contract_address);
        }
        return super.equals(obj);
    }


    public static boolean ifGrounding(String price) {
        return !StringUtils.isEmpty(price) && !price.equals("0");
    }

    @Override
    public int compareTo(NFTInfo o) {
        return 0;
    }
}