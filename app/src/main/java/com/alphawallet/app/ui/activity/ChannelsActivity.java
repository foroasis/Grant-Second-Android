package teleblock.ui.activity;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;

import teleblock.ui.view.ChannelsTabView;

/***
 * 频道焦点聚合页
 */
public class ChannelsActivity extends BaseFragment {
    private FrameLayout frameLayout;
    private ChannelsTabView channelsTabView;
    private BaseFragment dialogsActivity;

    public ChannelsActivity(BaseFragment dialogsActivity) {
        this.dialogsActivity = dialogsActivity;
    }

    @Override
    public View createView(Context context) {
        actionBar.setAddToContainer(false);
        frameLayout = new FrameLayout(context);
        channelsTabView = new ChannelsTabView(dialogsActivity) {
            @Override
            public void onBackClick() {
                finishFragment();
            }
        };
        frameLayout.addView(channelsTabView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        channelsTabView.initData();
        channelsTabView.setVisibility(View.VISIBLE);
        return fragmentView = frameLayout;
    }
}
