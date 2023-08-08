package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityGameRankListBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.ui.adapter.TgFragmentVp2Adapter;
import teleblock.ui.fragment.AllRankListPageFragment;
import teleblock.ui.fragment.FriendsRankListPageFragment;
import teleblock.util.ViewUtil;

/**
 * Time:2023/5/6
 * Author:Perry
 * Description：游戏排行榜页面
 */
public class GameRankListActivity extends BaseFragment {

    private ActivityGameRankListBinding binding;

    private String[] tabNameList;
    //存储子页面的集合
    private List<teleblock.ui.fragment.BaseFragment> pageFragmentView = new ArrayList<>();

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        binding = ActivityGameRankListBinding.inflate(LayoutInflater.from(context));

        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.getRoot().setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);
        binding.tvTitle.setText(LocaleController.getString("game_rank_list_title", R.string.game_rank_list_title));
        binding.tvPlayAgain.setText(LocaleController.getString("game_rank_list_play", R.string.game_rank_list_play));
        tabNameList = LocaleController.getString("game_rank_list_tabs", R.string.game_rank_list_tabs).split("\\|");

        //初始化适配器
        pageFragmentView.add(AllRankListPageFragment.instance());
        pageFragmentView.add(FriendsRankListPageFragment.instance());

        TgFragmentVp2Adapter mTgFragmentVp2Adapter = new TgFragmentVp2Adapter(getParentActivity(), pageFragmentView);
        binding.vpPage.setAdapter(mTgFragmentVp2Adapter);
        binding.vpPage.setUserInputEnabled(false);

        //tab适配器
        binding.magicIndicator.setNavigator(ViewUtil.mibSetNavigat(getParentActivity(), textMibAdapter(tabNameList, binding.vpPage)));
        ViewUtil.vbBindMiTabListener(binding.magicIndicator, binding.vpPage, position -> {

        });

        binding.ivBack.setOnClickListener(v -> finishFragment());

        binding.tvPlayAgain.setOnClickListener(v -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.PLAY_GAME_AGAIN));
            finishFragment();
        });
    }

    /**
     * 普通文字样式的adapter
     * @param tabs
     * @param viewPager2
     * @return
     */
    public CommonNavigatorAdapter textMibAdapter(String[] tabs, ViewPager2 viewPager2) {
        return new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return tabs.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ColorTransitionPagerTitleView titleView = new ColorTransitionPagerTitleView(context);
                titleView.setNormalColor(Color.parseColor("#FF889198"));
                titleView.setSelectedColor(Color.parseColor("#FF4B5BFF"));
                titleView.setText(tabs[index]);
                titleView.setTextSize(15f);
                titleView.setTypeface(Typeface.DEFAULT_BOLD);
                titleView.setOnClickListener(v -> viewPager2.setCurrentItem(index, false));
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_MATCH_EDGE);
                linePagerIndicator.setColors(Color.parseColor("#FF4B5BFF"));
                linePagerIndicator.setRoundRadius(5);
                return linePagerIndicator;
            }
        };
    }
}
