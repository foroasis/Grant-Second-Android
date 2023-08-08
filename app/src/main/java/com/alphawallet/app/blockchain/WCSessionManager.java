package teleblock.blockchain;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.particle.walletconnect.models.session.WCSession;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.walletconnect.Session;
import org.walletconnect.impls.WCSessionStore;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.ChainInfo;
import teleblock.model.wallet.ETHWallet;
import teleblock.ui.dialog.WalletBindingDialog;
import teleblock.util.ETHWalletUtils;
import teleblock.util.JsonUtil;
import teleblock.util.WalletConnectUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import timber.log.Timber;

/**
 * 创建日期：2022/8/4
 * 描述：
 */
public class WCSessionManager implements Session.Callback {

    private static WCSessionManager instance;
    public Session.Config config;
    private OkHttpClient client;
    private Moshi moshi;
//    private BridgeServer bridge;
    private WCSessionStore storage;
    public WCSession session;
    public String pkg;
    private WalletBindingDialog progressDialog;
    public String currentAccount;

    public static WCSessionManager getInstance() {
        if (instance == null) {
            synchronized (WCSessionManager.class) {
                if (instance == null) {
                    instance = new WCSessionManager();
                }
            }
        }
        return instance;
    }

    public WCSessionManager() {
//        client = new OkHttpClient.Builder().build();
//        moshi = new Moshi.Builder().addLast(new KotlinJsonAdapterFactory()).build();
//        bridge = new BridgeServer(moshi);
//        bridge.start();
//        File file = new File(PathUtils.getInternalAppFilesPath(), "session_store.json");
//        FileUtils.createOrExistsFile(file);
//        storage = new FileWCSessionStore(file, moshi);
    }

    public void init(ETHWallet ethWallet) {
//        currentAccount = "";
//        if (ethWallet.getConfig() != null) {
//            createSession(ethWallet.getConfig());
//            List<String> accounts = new ArrayList<>();
//            accounts.add(ethWallet.getAddress());
//            session.update(accounts, 0);
//        } else {
//            disConnect(false);
//        }
    }

    private void createSession(Session.Config config) {
//        session = new WCSession(
//                config,
//                new MoshiPayloadAdapter(moshi),
//                storage,
//                new OkHttpTransport.Builder(client, moshi),
//                new Session.PeerMeta(
//                        "https://alphagram.app/",
//                        AppUtils.getAppName(),
//                        "",
//                        new ArrayList<>()
//                ),
//                null
//        );
//        session.clearCallbacks();
//        session.addCallback(this);
    }

//    public void resetSession() {
//        progressDialog = new WalletBindingDialog(ActivityUtils.getTopActivity(), pkg);
//        progressDialog.show();
//        if (session != null) {
//            session.clearCallbacks();
//        }
//        byte[] bytes = new byte[32];
//        new Random().nextBytes(bytes);
//        String key = Numeric.toHexStringNoPrefix(bytes);
//        config = new Session.Config(
//                UUID.randomUUID().toString(),
////                "wss://bridge.walletconnect.org",
//                "wss://bridge.aktionariat.com:8887",
//                key,
//                "wc",
//                1
//        );
//        createSession(config);
////        session.offer();
//    }

    @Override
    public void onStatus(@NonNull Session.Status status) {
        Timber.i("onStatus-->" + status);
        AndroidUtilities.runOnUIThread(() -> {
            if (progressDialog != null) progressDialog.dismiss();
        });
//        if (Session.Status.Approved.INSTANCE.equals(status)) {
//            AppUtils.launchApp(AppUtils.getAppPackageName());
//            String address = session.approvedAccounts().get(0);
//            String peerMetaName = session.peerMeta().getName();
//            BlockchainConfig.WalletIconType walletIconType = BlockchainConfig.getWalletTypeByFullName(peerMetaName);
//            if (walletIconType == null) {
//                RuntimeException exception = new RuntimeException("本地钱包配置异常=> peerMetaName=" + peerMetaName);
//                Timber.e(exception);
//                throw exception;
//            }
//            ETHWallet ethWallet = CollectionUtils.find(WalletDaoUtils.loadAll(), new CollectionUtils.Predicate<ETHWallet>() {
//                @Override
//                public boolean evaluate(ETHWallet item) {
//                    return ArrayUtils.contains(ETHWallet.TYPE_CONNECT, item.getWalletType()) &&
//                            item.getAddress().equalsIgnoreCase(address) && item.getConnectedWalletPkg().equals(walletIconType.pkg);
//                }
//            });
//            if (ethWallet != null) { // 已创建过
//                ethWallet.setConfig(config);
////                ethWallet.setChainId(session.chainId());
//                WalletDaoUtils.update(ethWallet);
//                WalletDaoUtils.updateCurrent(ethWallet.getId());
//            } else {
//                ethWallet = new ETHWallet();
//                ethWallet.setId(System.currentTimeMillis());
//                ethWallet.setName(ETHWalletUtils.generateNewWalletName());
//                ethWallet.setAddress(address);
//                ethWallet.setWalletType(walletIconType.typeId);
//                ethWallet.setConnectedWalletPkg(walletIconType.pkg);
//                ethWallet.setConfig(config);
////                ethWallet.setChainId(session.chainId());
//                WalletDaoUtils.insertNewWallet(ethWallet);
//            }
//            // 绑定钱包
////            WalletUtil.walletBind(address, session.chainId(), walletIconType.typeId, new Runnable() {
////                @Override
////                public void run() {
////                    EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_APPROVED));
////                    EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CREATED));
////                }
////            });
//        } else if (Session.Status.Closed.INSTANCE.equals(status)) {
//        } else if (Session.Status.Connected.INSTANCE.equals(status)) {
//            if (CollectionUtils.isNotEmpty(session.approvedAccounts())) {
//                currentAccount = session.approvedAccounts().get(0);
//            }
//            if (TextUtils.isEmpty(pkg)) return;
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(config.toWCUri()));
//            intent.setPackage(pkg);
//            ActivityUtils.startActivity(intent);
//            pkg = null;
//        } else if (Session.Status.Disconnected.INSTANCE.equals(status)) {
//            if (!TextUtils.isEmpty(currentAccount)) { // 已链接过才断开
//                disConnect(false);
//            }
//        }
    }

    @Override
    public void onMethodCall(@NonNull Session.MethodCall call) {
        Timber.i("onMethodCall-->" + call);
        if (call instanceof Session.MethodCall.SessionUpdate) {
            Session.MethodCall.SessionUpdate sessionUpdate = (Session.MethodCall.SessionUpdate) call;
            if (CollectionUtils.isNotEmpty(sessionUpdate.getParams().getAccounts())) {
                currentAccount = sessionUpdate.getParams().getAccounts().get(0);
            } else {
                currentAccount = "";
            }
        }
    }

    /**
     * 断开链接
     */
    public void disConnect(boolean showToast) {
        if (showToast) {
            AppUtils.launchApp(AppUtils.getAppPackageName());
            ToastUtils.showLong(LocaleController.getString("wallet_failure_tips", R.string.wallet_failure_tips));
        }
//        if (session != null) {
//            session.kill();
//        }
//        ETHWallet ethWallet = WalletDaoUtils.getCurrent();
//        if (ethWallet != null && ArrayUtils.contains(ETHWallet.TYPE_CONNECT, ethWallet.getWalletType())) {
//            ETHWalletUtils.deleteWallet(ethWallet);
//        }
        EventBus.getDefault().post(new MessageEvent(EventBusTags.WALLET_CONNECT_CLOSED));
    }

    /**
     * 返回客户端持有的地址列表
     */
    public void getAccounts(Callback<String> callback) {
        if (!TextUtils.isEmpty(currentAccount)) {
            callback.onSuccess(currentAccount);
            return;
        }
        if (session == null || TextUtils.isEmpty(currentAccount)) {
            callback.onError(LocaleController.getString("wallet_failure_tips", R.string.wallet_failure_tips));
            disConnect(true);
            return;
        }
//        session.performMethodCall(
//                new Session.MethodCall.Custom(System.currentTimeMillis(), "eth_accounts", null),
//                response -> {
//                    AndroidUtilities.runOnUIThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (response.getError() == null) {
//                                if (response.getResult() instanceof List) {
//                                    String json = JsonUtil.parseObjToJson(response.getResult());
//                                    List<String> data = JsonUtil.parseJsonToList(json, String.class);
//                                    callback.onSuccess(CollectionUtils.isNotEmpty(data) ? data.get(0) : "");
//                                } else {
//                                    callback.onError("Unknown response");
//                                }
//                            } else {
//                                callback.onError(response.getError().getMessage());
//                            }
//                        }
//                    });
//                    return null;
//                });
        WalletConnectUtil.goToWallet();
    }

    /**
     * 返回当前配置的链ID
     */
    public void getChainId(boolean gotoWallet, Callback<String> callback) {
        if (session == null || TextUtils.isEmpty(currentAccount)) {
            callback.onError(LocaleController.getString("wallet_failure_tips", R.string.wallet_failure_tips));
            disConnect(true);
            return;
        }
//        session.performMethodCall(
//                new Session.MethodCall.Custom(System.currentTimeMillis(), "eth_chainId", null),
//                response -> {
//                    AndroidUtilities.runOnUIThread(() -> {
//                        if (response.getError() == null) {
//                            if (response.getResult() instanceof String) {
//                                callback.onSuccess((String) response.getResult());
//                            } else {
//                                callback.onError("Unknown response");
//                            }
//                        } else {
//                            callback.onError(response.getError().getMessage());
//                        }
//                    });
//                    return null;
//                });
        if (gotoWallet) {
            WalletConnectUtil.goToWallet();
        }
    }

    /**
     * 切换网络
     */
    public void switchNetwork(String chainId, Callback<String> callback) {
        List<ChainInfo> params = new ArrayList<>();
        Web3ConfigEntity.WalletNetworkConfigChainType chainType = BlockchainConfig.getChainType(Integer.parseInt(chainId));
        if (chainType == null) return;
        params.add(new ChainInfo(chainType.getId() + "", chainType.getName(), chainType.getMain_currency_name(), chainType.getRpc_url()));
//        session.performMethodCall(
//                new Session.MethodCall.Custom(System.currentTimeMillis(), "ETH".equals(chainType.getMain_currency_name()) ? "wallet_switchEthereumChain" : "wallet_addEthereumChain", params),
//                new Function1<Session.MethodCall.Response, Unit>() {
//                    @Override
//                    public Unit invoke(Session.MethodCall.Response response) {
//                        AndroidUtilities.runOnUIThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                AppUtils.launchApp(AppUtils.getAppPackageName());
//                                if (response.getError() == null) {
//                                    if (response.getResult() instanceof String) {
//                                        callback.onSuccess((String) response.getResult());
//                                    } else {
//                                        callback.onError("Unknown response");
//                                    }
//                                } else {
//                                    callback.onError(response.getError().getMessage());
//                                }
//                            }
//                        });
//                        return null;
//                    }
//                });
        WalletConnectUtil.goToWallet();
    }

    /**
     * 发起主币交易
     */
    public void sendTransaction(String from, String to, String amount, String data, Callback<String> callback) {
        sendTransaction(from, to, null, null, amount, 18, data, callback);
    }

    /**
     * 发起交易
     */
    public void sendTransaction(String from, String to, BigInteger gasPrice, BigInteger gasLimit, String amount, int decimal, String data, Callback<String> callback) {
//        String value = Numeric.toHexStringWithPrefix(new BigDecimal(WalletUtil.toWei(amount, decimal)).toBigInteger());
//        if (!TextUtils.isEmpty(data)) value = "";
//        session.performMethodCall(
//                new Session.MethodCall.SendTransaction(System.currentTimeMillis(), from, to, null,
//                        gasPrice != null ? Numeric.toHexStringWithPrefix(gasPrice) : null,
//                        gasLimit != null ? Numeric.toHexStringWithPrefix(gasLimit) : null, value, data),
//                response -> {
//                    AndroidUtilities.runOnUIThread(() -> {
//                        AppUtils.launchApp(AppUtils.getAppPackageName());
//                        if (response.getError() == null) {
//                            if (response.getResult() instanceof String) {
//                                callback.onSuccess((String) response.getResult());
//                            } else {
//                                callback.onError("Unknown response");
//                            }
//                        } else {
//                            callback.onError(response.getError().getMessage());
//                        }
//                    });
//                    return null;
//                });
//        WalletUtil.goToWallet();
    }

    public abstract static class Callback<T> {

        public abstract void onSuccess(T data);

        public void onError(String msg) {
            ToastUtils.showLong(msg);
            Timber.e(msg);
        }
    }
}