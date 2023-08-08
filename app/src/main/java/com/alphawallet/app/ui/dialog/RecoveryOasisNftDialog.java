package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogRecoveryOasisnftBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.web3j.abi.TypeDecoder;

import java.math.BigInteger;
import java.util.HashMap;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.Web3AbiDataUtils;
import teleblock.model.OasisNftInfoEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.RecoveryOasisNftApi;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/5/17
 * Author:Perry
 * Description：回收oasis nft对话框
 */
public class RecoveryOasisNftDialog extends Dialog {

    private DialogRecoveryOasisnftBinding binding;
    private OasisNftInfoEntity mOasisNftInfoEntity;
    private AlertDialog progressDialog;

    public RecoveryOasisNftDialog(@NonNull Context context, OasisNftInfoEntity mOasisNftInfoEntity) {
        super(context, R.style.dialog2);
        this.mOasisNftInfoEntity = mOasisNftInfoEntity;
        binding = DialogRecoveryOasisnftBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvTitle.setText(LocaleController.getString("dialog_recovery_oasisnft_title", R.string.dialog_recovery_oasisnft_title));
        binding.tvDesc.setText(LocaleController.getString("dialog_recovery_oasisnft_desc", R.string.dialog_recovery_oasisnft_desc));
        binding.tvBtn.setText(LocaleController.getString("dialog_recovery_oasisnft_btn", R.string.dialog_recovery_oasisnft_btn));

        GlideHelper.displayImage(binding.ivNftIcon.getContext(), binding.ivNftIcon, mOasisNftInfoEntity.getNft_image());
        binding.tvNftName.setText(mOasisNftInfoEntity.getNft_name());

        binding.tvBtn.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.OasisNft回收按钮点击, new HashMap<>());
            if (progressDialog == null) {
                progressDialog = new AlertDialog(context, 3);
                progressDialog.setCancelable(true);
            }

            if (mOasisNftInfoEntity.getNft_token_id() != 0) {
                approveNft(true);
                progressDialog.show();
            }
        });
    }


    private void approveNft(boolean needApprove) {
        String recycleAddress = MMKVUtil.getSystemMsg().oasis_grant_recycle_contract;
        String nftAddress = MMKVUtil.getSystemMsg().oasis_grant_nft_contract;
        BigInteger tokenId = BigInteger.valueOf(mOasisNftInfoEntity.getNft_token_id());
        String data = Web3AbiDataUtils.encodeGetApprovedData(tokenId);
        BlockFactory.get(42262).ethCall(nftAddress, data, new BlockCallback<String>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                if (!recycleAddress.equalsIgnoreCase(TypeDecoder.decodeAddress(data).getValue())) {
                    if (!needApprove) { // 轮询判断是否授权成功
                        approveNft(false);
                        return;
                    }
                    data = Web3AbiDataUtils.encodeApproveData(recycleAddress, tokenId);
                    WalletTransferUtil.writeContract(42262, nftAddress, data, new WalletUtil.SendTransactionListener() {
                        @Override
                        public void paySuccessful(String hash) {
                            approveNft(false);
                        }

                        @Override
                        public void payError(String error) {
                            ToastUtils.showLong(error);
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    recycleNft(nftAddress, tokenId, recycleAddress);
                }
            }

            @Override
            public void onError(String msg) {
                ToastUtils.showLong(msg);
                progressDialog.dismiss();
            }
        });
    }

    /**
     * 回收nft
     *
     * @param nftAddress
     * @param tokenId
     * @param recycleAddress
     */
    private void recycleNft(String nftAddress, BigInteger tokenId, String recycleAddress) {
        String data = Web3AbiDataUtils.encodeRecycleData(nftAddress, tokenId);
        WalletTransferUtil.writeContract(42262, recycleAddress, data, new WalletUtil.SendTransactionListener() {
            @Override
            public void paySuccessful(String hash) {
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new RecoveryOasisNftApi()
                                .setNft_address(nftAddress)
                                .setToken_id(String.valueOf(tokenId))
                                .setReceipt_account(WalletDaoUtils.getCurrent().getAddress())
                                .setTx_hash(hash))
                        .request(new OnHttpListener<BaseBean>() {
                            @Override
                            public void onSucceed(BaseBean result) {
                                ToastUtils.showShort(result.getMessage());
                            }

                            @Override
                            public void onFail(Exception e) {
                                ToastUtils.showLong(e.getMessage());
                            }

                            @Override
                            public void onEnd(Call call) {
                                progressDialog.dismiss();
                                dismiss();
                            }
                        });
            }

            @Override
            public void payError(String error) {
                ToastUtils.showLong(error);
                progressDialog.dismiss();
            }
        });
    }
}