package teleblock.model.wallet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2023/1/5
 * 描述：
 */
public class ParticleToken {


    @SerializedName("native")
    public String nativeX;
    public List<TokensEntity> tokens;

    public static class TokensEntity {
        public int decimals;
        public String amount;
        public String address;
        public String name;
        public String symbol;
        public String image;
    }
}