package teleblock.blockchain.bnb;

import com.blankj.utilcode.util.ActivityUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.ui.LaunchActivity;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockExplorer;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTResponse;
import teleblock.model.wallet.OpenSeaAssets;
import teleblock.network.api.blockchain.bnb.BnbNftListApi;
import teleblock.ui.activity.NFTSelectActivity;
import teleblock.util.WalletUtil;

public class BnbBlockExplorer extends BlockExplorer {

    public BnbBlockExplorer(Web3ConfigEntity.WalletNetworkConfigChainType chainType) {
        super(chainType);
    }

    @Override
    public void getNftList(String address, String cursor, BlockCallback<NFTResponse> callback) {
        if (!WalletUtil.isEvmAddress(address)) {
            callback.onSuccess(new NFTResponse());
            callback.onEnd();
            return;
        }
        if (((LaunchActivity) ActivityUtils.getTopActivity())
                .getActionBarLayout().getLastFragment() instanceof NFTSelectActivity) {
            EasyHttp.get(new ApplicationLifecycle())
                    .api(new BnbNftListApi()
                            .setOwner_address(address)
                            .setOrder_direction("desc")
                            .setLimit("10")
                            .setCursor(cursor)
                            .setInclude_orders(true))
                    .request(new OnHttpListener<String>() {
                        @Override
                        public void onSucceed(String result) {
                            callback.onSuccess(OpenSeaAssets.parse(result, chainType.getId()));
                        }

                        @Override
                        public void onFail(Exception e) {
                            callback.onError(e.toString());
                        }

                        @Override
                        public void onEnd(Call call) {
                            callback.onEnd();
                        }
                    });
        } else {
            super.getNftList(address, cursor, callback);
        }

    }
}
