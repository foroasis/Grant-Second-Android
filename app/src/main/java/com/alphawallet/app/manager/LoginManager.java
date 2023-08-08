package teleblock.manager;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.util.List;

import teleblock.config.AppConfig;
import teleblock.model.LoginDataResult;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.LoginApi;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;

/**
 * Time:2022/6/30
 * Author:Perry
 * Description：登录管理类
 */
public class LoginManager {

    /**
     * 用户登录
     * @param loginSuccessful
     * @param loginError
     */
    public static void userLogin(Runnable loginSuccessful, Runnable loginError) {
        String requestTag = "LoginRequest";
        EasyHttp.cancel(requestTag);
        //tgid
        long tgClientUserId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
        //上次登录存储的tgid
        long lastLoginClientId = MMKVUtil.getLong(AppConfig.MkKey.LAST_LOGIN_TGID);
        if (LoginManager.isLogin() && tgClientUserId == lastLoginClientId) {
            requestUserInfor(loginSuccessful);
            return;
        }

        //判断是否是新用户
        boolean ifNewUser = MMKVUtil.telegramNewUser();

        //参数设置
        LoginApi requestLogin = new LoginApi()
                .setTg_user_id(tgClientUserId)
                .setDevice(MMKVUtil.getString(AppConfig.MkKey.DEVICE_ID))
                .setCountrycode(MMKVUtil.getString(AppConfig.MkKey.COUNTRY_CODE))
                .setSimcode(MMKVUtil.getString(AppConfig.MkKey.SIM_CODE))
                .setIs_tg_new(ifNewUser ? 1 : 0);

        EasyHttp.post(new ApplicationLifecycle())
                .api(requestLogin)
                .tag(requestTag)
                .request(new OnHttpListener<BaseBean<LoginDataResult>>() {
                    @Override
                    public void onSucceed(BaseBean<LoginDataResult> result) {
                        //登录成功，存储当前的tgid
                        MMKVUtil.saveValue(AppConfig.MkKey.LAST_LOGIN_TGID, tgClientUserId);
                        //存储当前的登录数据
                        LoginManager.saveLoginData(result.getData());

                        if (ifNewUser) {
                            MMKVUtil.telegramNewUser(false);
                        }

                        requestUserInfor(loginSuccessful);
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (loginError != null) {
                            loginError.run();
                        }
                    }
                });
    }

    /**
     * 存储登录返回
     *
     * @param user
     */
    public static void saveLoginData(LoginDataResult user) {
        MMKVUtil.saveValue(AppConfig.MkKey.LOGIN_DATA, JsonUtil.parseObjToJson(user));
    }

    /**
     * 是否登录
     *
     * @return
     */
    public static boolean isLogin() {
        LoginDataResult user = getUser();
        return (user != null && !TextUtils.isEmpty(user.getToken()));
    }

    /**
     * 获取User实体
     *
     * @return
     */
    public static LoginDataResult getUser() {
        //获取登录json
        String loginJson = MMKVUtil.getString(AppConfig.MkKey.LOGIN_DATA);

        if (loginJson.isEmpty()) {
            return new LoginDataResult();
        }

        return JsonUtil.parseJsonToBean(loginJson, LoginDataResult.class);
    }

    /***
     * 用户ID
     * @return
     */
    public static String getUserId() {
        try {
            return getUser().getUser().getUser_id();
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 获取用户getUserToken
     *
     * @return
     */
    public static String getUserToken() {
        LoginDataResult user = getUser();
        if (user == null) {
            return "";
        } else {
            return user.getToken();
        }
    }

    /**
     * 修改用户信息
     * @param user
     */
    public static void saveUserInfo(LoginDataResult.UserEntity user) {
        LoginDataResult loginDataResult = getUser();
        loginDataResult.setUser(user);
        saveLoginData(loginDataResult);
    }

    /***
     * 是否我们的新用户
     * @return
     */
    public static boolean isNewer() {
        LoginDataResult user = getUser();
        if (user == null) {
            return false;
        } else {
            return user.isNewer();
        }
    }

    /**
     * 请求tg个人信息数据
     */
    private static void requestUserInfor(Runnable runnable) {
        //获取本地存储的user
        LoginDataResult userData = getUser();
        if (StringUtils.isEmpty(userData.getUser().getAvatar())) {
            TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
            TLRPC.UserProfilePhoto userProfilePhoto = user.photo;
            if (userProfilePhoto != null) {
                TLRPC.FileLocation fileLocation = userProfilePhoto.photo_big;
                if (fileLocation != null) {
                    File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(fileLocation, false);
                    String ext = FileUtils.getFileExtension(f);
                    boolean isVideo = ext.contains("webm") || ext.contains("mp4") || ext.contains("gif");
                    if (f != null && !f.exists()) {
                        f = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), f.getName());
                    }

                    if (f != null && f.exists()) {
                        File finalF = f;
                        MediaController.saveFile(
                                f.toString()
                                , ApplicationLoader.applicationContext
                                , isVideo ? 1 : 0
                                , null, null
                                , () -> TelegramUtil.uploadFile(null, finalF, "avatar", link ->
                                        TelegramUtil.editUserInfo(link, UserObject.getUserName(user))
                                ));
                    }
                }
            }
        }

        //获取当前用户nft或者数据类型
        long tgClientUserId = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
        TelegramUtil.getUserNftData(tgClientUserId, new TelegramUtil.UserNftDataListener() {
            @Override
            public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                if (!CollectionUtils.isEmpty(walletInfoList)) {
                    WalletInfo walletInfo = walletInfoList.get(0);
                    MMKVUtil.setNftphotoIfShow(walletInfo.getIs_show_wallet());//保存nft头像状态
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }

            @Override
            public void nftDataRequestError(String errorMsg) {
            }
        });

    }
}
