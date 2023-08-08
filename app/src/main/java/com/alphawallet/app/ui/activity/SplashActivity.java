package teleblock.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivitySplashBinding;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.HashMap;

import teleblock.config.AppConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.SystemEntity;
import teleblock.model.Web3ConfigEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.SystemCheckApi;
import teleblock.util.EventUtil;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.SystemUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;

/**
 * 创建日期：2022/6/21
 * 描述：
 */
public class SplashActivity extends BaseActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.transparentStatusBar(mActivity);//透明状态栏
        // 将window的背景图设置为空
        getWindow().setBackgroundDrawable(null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivitySplashBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        AndroidUtilities.setNavigationBarColor(getWindow(), Theme.getColor(Theme.key_chat_messagePanelBackground));

        //init
        binding.tvTitle.setText(LocaleController.getString("view_lancher_title", R.string.view_lancher_title));
        binding.tvDesc.setText(LocaleController.getString("view_lancher_desc", R.string.view_lancher_desc));

        if (MMKVUtil.firstLoad()) {
            new Thread(() -> applyDefaultTheme()).start();
            EventUtil.track(mActivity, EventUtil.Even.第一次打开, new HashMap<>());
            MMKVUtil.firstLoad(false);
        }

        firstOpenApp();
        startMainAct();
    }

    /**
     * 每次打开app
     */
    private void firstOpenApp() {
        // TODO: 版本升级要更新本地默认配置
        String json = ResourceUtils.readAssets2String("blockchain/web3Config.json");
        MMKVUtil.setWeb3ConfigData(JsonUtil.parseJsonToBean(json, Web3ConfigEntity.class));
        WalletUtil.requestWeb3ConfigData(null);
        getDeviceMsg();
        systemCheck();
    }

    /**
     * 获取设备信息
     */
    private void getDeviceMsg() {
        if (MMKVUtil.getString(AppConfig.MkKey.DEVICE_ID).isEmpty()) {
            //存储设备信息到本地
            MMKVUtil.saveValue(AppConfig.MkKey.DEVICE_ID, SystemUtil.getUniquePsuedoID());
            MMKVUtil.saveValue(AppConfig.MkKey.COUNTRY_CODE, SystemUtil.getCountryZipCode(this));
            MMKVUtil.saveValue(AppConfig.MkKey.SIM_CODE, SystemUtil.getTelContry(this));
        }
    }

    /**
     * 跳转主页面
     */
    private void startMainAct() {
        startActivity(new Intent(this, LaunchActivity.class));
        finish();
    }

    /**
     * 请求系统配置信息
     */
    private void systemCheck() {
        EasyHttp.post(new ApplicationLifecycle()).api(new SystemCheckApi()
                        .setCountrycode(MMKVUtil.getString(AppConfig.MkKey.COUNTRY_CODE))
                        .setSimcode(MMKVUtil.getString(AppConfig.MkKey.SIM_CODE)))
                .request(new OnHttpListener<BaseBean<SystemEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<SystemEntity> result) {
                        MMKVUtil.setSystemMsg(result.getData());
                        //搜索机器人添加到缓存
                        TelegramUtil.getBotInfo(result.getData(), () -> {
                            EventBus.getDefault().postSticky(new MessageEvent(EventBusTags.OBTAIN_BOT_SUCCESSFUL));
                        });
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    private void applyDefaultTheme() {
        String assetPath = "theme/alpha.attheme";
        String themeFilePath = PathUtils.getExternalAppFilesPath() + "/" + assetPath;
        File themeFile = new File(themeFilePath);
        if (!themeFile.exists()) {
            ResourceUtils.copyFileFromAssets(assetPath, themeFilePath);
        }
        if (themeFile.exists()) {
            Theme.ThemeInfo themeInfo = Theme.applyThemeFile(themeFile, "alpha", null, true);
            Theme.saveCurrentTheme(themeInfo, false, false, false);
            SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE).edit();
            editor.putString("lastDayTheme", themeInfo.getKey());
            editor.commit();
        }
    }
}