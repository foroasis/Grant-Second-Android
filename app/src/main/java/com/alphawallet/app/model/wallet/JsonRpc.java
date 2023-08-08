package teleblock.model.wallet;

import com.blankj.utilcode.util.JsonUtils;

import org.web3j.utils.Numeric;

import java.util.List;

import teleblock.util.JsonUtil;
import timber.log.Timber;

/**
 * 创建日期：2022/10/19
 * 描述：
 */
public class JsonRpc {

    private long id;
    private String jsonrpc;
    private String method;
    private List<Object> params;
    private long chainId;
    private Object result;

    public JsonRpc(String method, List<Object> params) {
        new JsonRpc(method, params, 0);
    }

    public JsonRpc(String method, List<Object> params, long chainId) {
        this.id = System.currentTimeMillis();
        this.jsonrpc = "2.0";
        this.method = method;
        this.params = params;
        this.chainId = chainId;
    }

    public String getResult() {
        if (result == null) {
            return "";
        }
        if (result instanceof String) {
            return (String) result;
        }
        return JsonUtil.parseObjToJson(result);
    }
}