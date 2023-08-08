package teleblock.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.FragmentAllGameRankListBinding;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.GameRankListEntity;
import teleblock.ui.adapter.GameRankListAdapter;
import teleblock.widget.GlideHelper;
import timber.log.Timber;

/**
 * Time:2023/5/6
 * Author:Perry
 * Description：排行榜总榜单
 */
public class AllRankListPageFragment extends BaseFragment {

    private FragmentAllGameRankListBinding binding;
    private GameRankListAdapter mGameRankListAdapter;

    public static AllRankListPageFragment instance() {
        AllRankListPageFragment fragment = new AllRankListPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        binding = FragmentAllGameRankListBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onViewCreated() {
        binding.rvRankList.setLayoutManager(new LinearLayoutManager(getContext()));
        mGameRankListAdapter = new GameRankListAdapter();
        binding.rvRankList.setAdapter(mGameRankListAdapter);

        myRank();

        rankList();
    }

    /**
     * 我的排名数据
     */
    private void myRank() {
        GameRankListEntity.SelfRank selfRank = getMessageController().selfRank;

        if (selfRank == null) {
            binding.llUserInfo.setVisibility(View.GONE);
            return;
        }

        //自己的头像
        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
        binding.tgAvatar.setUserInfo(user).loadView();

        //排名
        if (selfRank.getRank() > 0) {
            binding.tvRankStatus.setText(LocaleController.getString("game_rank_list_my_rank", R.string.game_rank_list_my_rank));
            binding.tvMyRank.setVisibility(View.VISIBLE);
            binding.tvMyRank.setText(String.valueOf(selfRank.getRank()));
        } else {
            binding.tvRankStatus.setText(LocaleController.getString("game_rank_list_fail", R.string.game_rank_list_fail));
            binding.tvMyRank.setVisibility(View.INVISIBLE);
        }

        //最高分
        binding.tvHighestScore.setText(LocaleController.getString("game_rank_list_highest_score", R.string.game_rank_list_highest_score));
        binding.tvMyHighestScore.setText(String.valueOf(selfRank.getGame_score()));
    }

    /**
     * 榜单数据
     */
    private void rankList() {
        List<GameRankListEntity.RankList> allRankList = getMessageController().rankLists;
        if (CollectionUtils.isEmpty(allRankList)) {
            binding.llTopOfList.setVisibility(View.GONE);
            return;
        }

        try {
            //榜首数据
            List<GameRankListEntity.RankList> rankTopList = new ArrayList<>();
            //非榜首数据
            List<GameRankListEntity.RankList> rankList = new ArrayList<>();

            //榜首个数，如果榜单个数>3，则取3条，<3则取所有的榜单数据
            int rankTopNumber = Math.min(3, allRankList.size());
            for (int i = 0; i < allRankList.size(); i++) {
                GameRankListEntity.RankList rankItemData = allRankList.get(i);
                if (i < rankTopNumber) {
                    rankTopList.add(rankItemData);
                } else {
                    rankList.add(rankItemData);
                }
            }

            //榜首数据<3，则隐藏榜首样式，按照普通排行来
            if (rankTopNumber < 3) {
                binding.llTopOfList.setVisibility(View.GONE);
                mGameRankListAdapter.setList(rankTopList);
                return;
            }

            //榜首数据
            GameRankListEntity.RankList championData = rankTopList.get(0);
            GlideHelper.displayImage(binding.ivChampionAvatar.getContext(), binding.ivChampionAvatar, championData.getAvatar());
            binding.tvChampionName.setText(championData.getName());
            binding.tvChampionScore.setText(String.valueOf(championData.getGame_score()));

            GameRankListEntity.RankList runnerUpData = rankTopList.get(1);
            GlideHelper.displayImage(binding.ivRunnerUpAvatar.getContext(), binding.ivRunnerUpAvatar, runnerUpData.getAvatar());
            binding.tvRunnerUpName.setText(runnerUpData.getName());
            binding.tvRunnerUpScore.setText(String.valueOf(runnerUpData.getGame_score()));

            GameRankListEntity.RankList thirdRunnerUpData = rankTopList.get(2);
            GlideHelper.displayImage(binding.ivThirdPlaceRunnerUpAvatar.getContext(), binding.ivThirdPlaceRunnerUpAvatar, thirdRunnerUpData.getAvatar());
            binding.tvThirdPlaceRunnerUpName.setText(thirdRunnerUpData.getName());
            binding.tvThirdPlaceRunnerUpScore.setText(String.valueOf(thirdRunnerUpData.getGame_score()));

            //榜单列表数据
            mGameRankListAdapter.setList(rankList);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private MessagesController getMessageController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }
}
