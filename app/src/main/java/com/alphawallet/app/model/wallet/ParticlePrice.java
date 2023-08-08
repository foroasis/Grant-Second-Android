package teleblock.model.wallet;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2023/1/5
 * 描述：
 */
public class ParticlePrice {

    public String address;
    public List<CurrenciesEntity> currencies;

    public static class CurrenciesEntity {
        public String type;
        public double price;
        public double marketCap;
        @SerializedName("24hChange")
        public double _$24hChange;
        @SerializedName("24hVol")
        public double _$24hVol;
        public int lastUpdatedAt;
    }
}