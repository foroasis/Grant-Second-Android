package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.JsonUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogGameDetailBinding;

import teleblock.model.BaseLoadmoreModel;
import teleblock.model.GameInfoEntity;
import teleblock.model.GameUserEntity;
import teleblock.model.wallet.ETHWallet;
import teleblock.network.BaseBean;
import teleblock.network.api.GameUsersApi;
import teleblock.ui.adapter.GameUsersAdapter;
import teleblock.util.SystemUtil;
import teleblock.util.WalletUtil;

/**
 * 游戏详情—立即参与
 */
public class GameDetailDialog extends BaseBottomSheetDialog implements OnLoadMoreListener {

    private DialogGameDetailBinding binding;
    private GameInfoEntity gameInfo;
    private GameUsersAdapter gameUsersAdapter;
    private long dialogId;
    private int page = 1;

    public GameDetailDialog(Context context, GameInfoEntity gameInfo, long dialogId) {
        super(context);
        this.gameInfo = gameInfo;
        this.dialogId = dialogId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameDetailBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
        initData();
    }

    private void initView() {
        binding.rvGameUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        gameUsersAdapter = new GameUsersAdapter(gameInfo.currency_name);
        gameUsersAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        binding.rvGameUsers.setAdapter(gameUsersAdapter);

        binding.tvGameInvolve.setOnClickListener(v -> {
            WalletUtil.getWalletInfo(wallet -> new GameBetConfirmDialog(getContext(), gameInfo, dialogId).show());
            dismiss();
        });

        binding.tvGameName.setText(LocaleController.getString("game_detail_name", R.string.game_detail_name));
        binding.tvGameRule.setText(JsonUtils.getString(gameInfo.rules, SystemUtil.getAppLanguage()));
        binding.tvGameInvolve.setText(LocaleController.getString("game_detail_involve", R.string.game_detail_involve_text));
    }

    private void initData() {
        binding.tvAmountPool.setText(gameInfo.amount_pool + " " + gameInfo.currency_name);
        binding.tvChainName.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(ResourceUtils.getDrawableIdByName("user_chain_logo_" + gameInfo.chain_id)));
        binding.tvChainName.setText(gameInfo.chain_name);
        loadData();
    }

    @Override
    public void onLoadMore() {
        page += 1;
        loadData();
    }

    private void loadData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new GameUsersApi()
                        .setGame_number(gameInfo.game_number)
                        .setPage(page))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<GameUserEntity>>>() {

                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<GameUserEntity>> result) {
                        gameUsersAdapter.addData(result.getData().getData());
                        if (result.getData().whetherRemaining()) {
                            gameUsersAdapter.getLoadMoreModule().loadMoreComplete();
                        } else {
                            gameUsersAdapter.getLoadMoreModule().loadMoreEnd(true);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        gameUsersAdapter.getLoadMoreModule().loadMoreFail();
                    }
                });
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(50));
        getWindow().setGravity(Gravity.BOTTOM);
        resetPeekHeight();
    }

}