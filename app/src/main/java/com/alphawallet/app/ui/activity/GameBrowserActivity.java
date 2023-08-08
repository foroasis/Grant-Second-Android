package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityBrowserBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;

import javax.annotation.Nonnull;

import okhttp3.Call;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.GameUploadResultEntity;
import teleblock.model.HomeGameConfigEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.JumpGameRankApi;
import teleblock.network.api.RequestHomeGameConfig;
import teleblock.ui.dialog.GameScoreDialog;
import teleblock.ui.dialog.HomeGameConfirmDialog;
import teleblock.ui.dialog.LoadingDialog;
import teleblock.util.EventUtil;
import teleblock.util.SystemUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.WebViewWrapper;


/**
 * 创建日期：2022/3/15
 * 描述：小游戏浏览器界面
 */
public class GameBrowserActivity extends BaseFragment {
    private ActivityBrowserBinding binding;

    //游戏编号
    private String gameNumber;
    //游戏链接
    private String gameLink;

    //加载框
    private LoadingDialog loadingDialog;
    private HomeGameConfigEntity gameConfigEntity;

    public static void start(BaseFragment baseFragment, @Nonnull Bundle bundle) {
        WalletUtil.getWalletInfo(wallet -> baseFragment.presentFragment(new GameBrowserActivity(bundle)));
    }

    public GameBrowserActivity(@Nonnull Bundle bundle) {
        super(bundle);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("common_web_page_loading", R.string.common_web_page_loading));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        gameNumber = getArguments().getString("gameNumber");
        gameLink = getArguments().getString("gameLink");

        binding = ActivityBrowserBinding.inflate(LayoutInflater.from(context));
        EventBus.getDefault().register(this);
        setWebView();
        requestGameConfig(this::loadGame);
        return fragmentView = binding.getRoot();
    }

    /**
     * 设置webview参数
     */
    private void setWebView() {
        binding.webViewWrapper.initListener(new WebViewWrapper.WebViewListener() {
            @Override
            public void onReceivedError(WebView view) {
            }

            @Override
            public void onReceivedTitle(String title) {
                actionBar.setTitle(title);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.startsWith("https://play.google.com/store/apps/details?id=")) {
                    SystemUtil.gotoGooglePlay(getParentActivity(), url);
                    return true;
                }
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                EventUtil.track(getContext(), EventUtil.Even.跳一跳小游戏加载完成, new HashMap<>());
            }
        });
        binding.webViewWrapper.getWebView().addJavascriptInterface(new WebInterface(), "alphagram");
    }

    /**
     * 请求游戏配置数据
     * @param runnable
     */
    private void requestGameConfig(Runnable runnable) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext(), LocaleController.getString("Loading", R.string.Loading));
        }
        loadingDialog.show();
        if (!TextUtils.isEmpty(gameNumber)) {
            EasyHttp.post(new ApplicationLifecycle())
                    .tag(this.getClass().getSimpleName())
                    .api(new RequestHomeGameConfig().setGame_number(gameNumber))
                    .request(new OnHttpListener<BaseBean<HomeGameConfigEntity>>() {
                        @Override
                        public void onSucceed(BaseBean<HomeGameConfigEntity> result) {
                            gameConfigEntity = result.getData();
                            if (gameConfigEntity != null) {
                                int times = gameConfigEntity.getTimes();//剩余次数
                                if (times > gameConfigEntity.getFree_times()) {//如果剩余次数>免费次数，说明支付过
                                    runnable.run();
                                } else {
                                    new HomeGameConfirmDialog(getContext(), gameNumber, gameConfigEntity, runnable).show();
                                }
                            } else {
                                finishFragment();
                            }
                        }

                        @Override
                        public void onFail(Exception e) {
                            ToastUtils.showLong(e.getMessage());
                            finishFragment();
                        }

                        @Override
                        public void onEnd(Call call) {
                            loadingDialog.dismiss();
                        }
                    });
        }
    }

    /**
     * 加载游戏
     */
    private void loadGame() {
        EventUtil.track(getContext(), EventUtil.Even.小游戏入口点击, new HashMap<>());
        binding.webViewWrapper.loadUrl(gameLink);
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        EventBus.getDefault().unregister(this);
        binding.webViewWrapper.onDestroy();
        EasyHttp.cancel(this.getClass().getSimpleName());
    }

    /**
     * js交互
     */
    private class WebInterface {
        @JavascriptInterface
        public void gameReport(String gameNumber, String score) {
            uploadScore(gameNumber, score);
        }
    }

    /**
     * 上报分数
     * @param gameNumber
     * @param score
     */
    private void uploadScore(String gameNumber, String score) {
        AndroidUtilities.runOnUIThread(() -> {
            EasyHttp.post(new ApplicationLifecycle())
                    .tag(this.getClass().getSimpleName())
                    .api(new JumpGameRankApi()
                            .setGame_number(gameNumber)
                            .setGame_score(score)
                            .setReceipt_account(WalletDaoUtils.getCurrent().getAddress())
                    ).request(new OnHttpListener<BaseBean<GameUploadResultEntity>>() {
                        @Override
                        public void onSucceed(BaseBean<GameUploadResultEntity> result) {
                            if (result.getCode() == 12001) {
                                ToastUtils.showLong(result.getMessage());
                                return;
                            }
                            new GameScoreDialog(
                                    GameBrowserActivity.this
                                    , score
                                    , gameConfigEntity.getSymbol()
                                    , gameConfigEntity.getIcon()
                                    , result.getData()
                                    , gameNumber
                                    , () -> requestGameConfig(GameBrowserActivity.this::loadGame)
                            ).show();
                        }

                        @Override
                        public void onFail(Exception e) {
                            ToastUtils.showLong(e.getMessage());
                        }
                    });
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.PLAY_GAME_AGAIN:
                requestGameConfig(GameBrowserActivity.this::loadGame);
                break;
        }
    }
}