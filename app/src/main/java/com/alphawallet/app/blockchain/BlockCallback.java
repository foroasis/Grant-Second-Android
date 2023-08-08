package teleblock.blockchain;

import com.blankj.utilcode.util.GsonUtils;

import timber.log.Timber;


public abstract class BlockCallback<T> {

    public void onProgress(int index, String data) {
        Timber.i("onProgress-->" + index + "---" + data);
    }

    public void onSuccess(T data) {
        Timber.i("onSuccess-->" + GsonUtils.toJson(data));
    }

    public void onError(String msg) {
        Timber.e("onError-->" + msg);
    }

    public void onEnd() {

    }
}
