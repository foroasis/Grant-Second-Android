package teleblock.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.DialogGameScoreBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import teleblock.model.GameRankListEntity;
import teleblock.model.GameUploadResultEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.GameRankListApi;
import teleblock.ui.activity.GameRankListActivity;
import teleblock.ui.activity.MyWalletActivity;
import teleblock.util.EventUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2023/3/30
 * 描述：游戏得分弹窗
 */
public class GameScoreDialog extends Dialog {

    private DialogGameScoreBinding binding;
    private BaseFragment baseFragment;
    private String coinLink;
    private Runnable runnable;
    private String score;
    private String symbol;
    private GameUploadResultEntity result;
    private String gameNumber;

    public GameScoreDialog(
            BaseFragment baseFragment,
            String score,
            String symbol,
            String coinLink,
            GameUploadResultEntity result,
            String gameNumber,
            Runnable runnable
    ) {
        super(baseFragment.getContext(), R.style.dialog2);
        this.baseFragment = baseFragment;
        this.score = score;
        this.symbol = symbol;
        this.coinLink = coinLink;
        this.result = result;
        this.gameNumber = gameNumber;
        this.runnable = runnable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameScoreBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
        requestListData();
    }

    private void initView() {
        EventUtil.track(getContext(), EventUtil.Even.小游戏奖励弹窗展示, new HashMap<>());

        binding.tvPlayAgain.setOnClickListener(v -> {
            runnable.run();
            dismissDialog();
        });

        binding.tvGotoWallet.setOnClickListener(view -> {
            baseFragment.presentFragment(new MyWalletActivity());
            dismissDialog();
        });

        binding.clBottom.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.游戏排行榜入口点击, new HashMap<>());
            if (!CollectionUtils.isEmpty(getMessageController().rankLists)) {
                baseFragment.presentFragment(new GameRankListActivity());
                dismissDialog();
            }
        });

        binding.tvScoreTitle.setText(LocaleController.getString("game_score_dialog_score_title", R.string.game_score_dialog_score_title));
        binding.tvRewardTitle.setText(LocaleController.getString("game_score_dialog_reward_title", R.string.game_score_dialog_reward_title));
        binding.tvGotoWallet.setText(LocaleController.getString("game_score_dialog_goto_wallet", R.string.game_score_dialog_goto_wallet));
        binding.tvPlayAgain.setText(LocaleController.getString("game_score_dialog_play_again", R.string.game_score_dialog_play_again));
        binding.tvListTitle.setText(LocaleController.getString("game_score_dialog_list_title", R.string.game_score_dialog_list_title));

        binding.tvScoreValue.setText(score);
        GlideHelper.getDrawableGlide(getContext(), coinLink, drawable -> binding.tvRewardValue.getHelper().setIconNormalLeft(drawable));
        binding.tvRewardValue.setText(result.getToken() + symbol);
        binding.tvRank.setText(String.valueOf(result.getRank()));

        int up = result.getUp();
        binding.ivRank.setVisibility(up == 0 ? View.GONE : View.VISIBLE);
        if (up < 0) {
            binding.ivRank.setImageResource(R.drawable.rank_arrow_down);
            binding.tvRankTitle.setText("Down:");
        } else {
            binding.ivRank.setImageResource(R.drawable.rank_arrow_up);
            binding.tvRankTitle.setText("Up:");
        }

        binding.tvRankNum.setText(up > 0 ? "+" + up : String.valueOf(up));
    }

    private void requestListData() {
        EasyHttp.post(new ApplicationLifecycle())
                .tag(this.getClass().getSimpleName())
                .api(new GameRankListApi().setGame_number(gameNumber))
                .request(new OnHttpListener<BaseBean<GameRankListEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<GameRankListEntity> result) {
                        GameRankListEntity resultData = result.getData();
                        if (resultData != null) {
                            List<GameRankListEntity.RankList> rank_list = resultData.getRank_list();
                            if (!CollectionUtils.isEmpty(rank_list)) {
                                //缓存
                                getMessageController().rankLists.clear();
                                getMessageController().rankLists.addAll(rank_list);
                            }

                            getMessageController().selfRank = resultData.getSelf_rank();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {}

                    @Override
                    public void onEnd(Call call) {
                        //榜单显示
                        showRankListView();
                    }
                });
    }

    private void showRankListView() {
        binding.flAvatar.removeAllViews();
        //榜单数据
        List<GameRankListEntity.RankList> rankList = getMessageController().rankLists;
        if (CollectionUtils.isEmpty(rankList)) {
            return;
        }

        //联系人数据
        ArrayList<TLRPC.TL_contact> contactsList = AccountInstance.getInstance(UserConfig.selectedAccount).getContactsController().contacts;
        //榜单前三集合
        List<GameRankListEntity.RankList> topThreeRankList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(contactsList) && contactsList.size() > 3) {
            for (GameRankListEntity.RankList rankListItem : rankList) {
                for (TLRPC.TL_contact tl_contactItem : contactsList) {
                    if (rankListItem.getTg_user_id().equals(String.valueOf(tl_contactItem.user_id))) {
                        if (topThreeRankList.size() < 3) {
                            topThreeRankList.add(rankListItem);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        //数据不满足三条，从榜单获取
        if (topThreeRankList.size() < 3) {
            topThreeRankList.clear();
            for (int i = 0; i < Math.min(3, rankList.size()); i++) {
                topThreeRankList.add(rankList.get(i));
            }
        }

        //倒序排列
        Collections.reverse(topThreeRankList);

        int size = 30;
        int margin = 20;
        for (int i = 0; i < topThreeRankList.size(); i++) {
            GameRankListEntity.RankList topThreeItem =topThreeRankList.get(i);

            ShapeableImageView shapeableImageView = new ShapeableImageView(getContext());
            shapeableImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ShapeAppearanceModel.Builder shapeAppearanceModelBuilder = ShapeAppearanceModel.builder();
            shapeAppearanceModelBuilder.setAllCorners(new RoundedCornerTreatment());
            shapeAppearanceModelBuilder.setAllCornerSizes(SizeUtils.dp2px(size / 2f));
            shapeableImageView.setShapeAppearanceModel(shapeAppearanceModelBuilder.build());
            GlideHelper.displayImage(shapeableImageView.getContext(), shapeableImageView, topThreeItem.getAvatar());

            binding.flAvatar.addView(
                    shapeableImageView
                    , LayoutHelper.createFrame(
                            size, size, Gravity.CENTER_VERTICAL | Gravity.END, 0f, 0f, margin * i, 0f
                    )
            );
        }
    }

    private MessagesController getMessageController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

    private void dismissDialog() {
        EasyHttp.cancel(this.getClass().getSimpleName());
        dismiss();
    }
}