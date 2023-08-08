package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.shuyu.gsyvideoplayer.utils.MeasureHelper;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.ActGrantTaskDetailBinding;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.HashMap;

import okhttp3.Call;
import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.blockchain.Web3TransactionUtils;
import teleblock.model.GrantTaskEntity;
import teleblock.model.OrderResultEntity;
import teleblock.network.BaseBean;
import teleblock.network.GrantTaskApi;
import teleblock.network.api.GroupAccreditApi;
import teleblock.network.api.MintCallbackApi;
import teleblock.ui.adapter.GrantTaskProgressAdapter;
import teleblock.util.EventUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

import org.telegram.tgnet.TLRPC;

/**
 * grant任务详情页
 */
public class GrantTaskDetailAct extends BaseFragment {

    private ActGrantTaskDetailBinding binding;
    private GrantTaskProgressAdapter grantTaskProgressAdapter;
    private GrantTaskEntity grantTaskEntity;

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        removeActionbarViews();
        EventUtil.track(getContext(), EventUtil.Even.任务页, new HashMap<>());
        binding = ActGrantTaskDetailBinding.inflate(LayoutInflater.from(context));
        initView();
        initData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        grantTaskProgressAdapter = new GrantTaskProgressAdapter();
        binding.recyclerView.setAdapter(grantTaskProgressAdapter);

        binding.flBack.setOnClickListener(view -> finishFragment());
        binding.flRefresh.setOnClickListener(view -> {
            AlertDialog progressDialog = new AlertDialog(getContext(), 3);
            progressDialog.setCanCancel(false);
            showDialog(progressDialog);
            loadTaskData();
        });
        binding.tvMintNft.setOnClickListener(v -> {
            EventUtil.track(getContext(), EventUtil.Even.mint_Nft按钮, new HashMap<>());
            mintNft();
        });
        binding.tvJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventUtil.track(getContext(), EventUtil.Even.加入空投群按钮, new HashMap<>());
                validateJoin();
            }
        });

        binding.tvTaskTitle.setText(LocaleController.getString("grant_task_detail_title", R.string.grant_task_detail_title));
        binding.tvTaskCompletedTip.setText(LocaleController.getString("grant_task_detail_task_completed", R.string.grant_task_detail_task_completed));
        binding.tvMintCompletedTip.setText(LocaleController.getString("grant_task_detail_mint_completed", R.string.grant_task_detail_mint_completed));
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
        binding.tvJoinGroup.setText(LocaleController.getString("grant_task_detail_join_group", R.string.grant_task_detail_join_group));
    }

    private void initData() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.rlTopBar.getLayoutParams();
        layoutParams.topMargin = AndroidUtilities.statusBarHeight;
        binding.llBottom.setVisibility(View.GONE);
    }

    private void showDetail(GrantTaskEntity grantTaskEntity) {
        this.grantTaskEntity = grantTaskEntity;
        GlideHelper.displayImage(binding.ivImage.getContext(), binding.ivImage, grantTaskEntity.banner_info.image);
        grantTaskProgressAdapter.setList(grantTaskEntity.task_info);
        binding.llBottom.setVisibility(View.VISIBLE);
        boolean taskCompleted = true;
        for (GrantTaskEntity.TaskInfoEntity taskInfo : grantTaskProgressAdapter.getData()) {
            if (taskInfo.status == 0) {
                taskCompleted = false;
                break;
            }
        }
        if (taskCompleted) { // 任务都完成
            checkNftBalance(grantTaskEntity.contract_info, false);
        }
    }

    private void checkNftBalance(GrantTaskEntity.ContractInfoEntity contractInfo, boolean background) {
        if (WalletDaoUtils.getCurrent() == null) return;
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_loading", R.string.grant_task_detail_mint_nft_loading));
        String data = Web3TransactionUtils.encodeBalanceOfData(WalletDaoUtils.getCurrent().getAddress(), BigInteger.valueOf(contractInfo.token_id));
        BlockFactory.get(contractInfo.chain_id).ethCall(contractInfo.address, data, new BlockCallback<String>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                int num; // 个数
                try {
                    num = Numeric.decodeQuantity(data).intValue();
                } catch (Exception e) {
                    num = 0;
                }
                if (num > 0) {
                    binding.llMintNft.setVisibility(View.GONE);
                    binding.llJoinGroup.setVisibility(View.VISIBLE);
                } else {
                    if (background) {
                        binding.llMintNft.postDelayed(() -> checkNftBalance(contractInfo, true), 1000);
                    } else {
                        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                        binding.tvMintNft.setEnabled(true);
                    }
                }
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                ToastUtils.showLong(msg);
            }
        });
    }

    private void validateJoin() {
        long chat_id = grantTaskEntity.airdrop_group_info.chat_id;
        TLRPC.Chat chat = getMessagesController().getChat(chat_id);
        if (!ChatObject.isNotInChat(chat)) {
            Bundle args = new Bundle();
            args.putLong("chat_id", chat_id);
            presentFragment(new ChatActivity(args));
            return;
        }
        AlertDialog progressDialog = new AlertDialog(getContext(), 3);
        progressDialog.setCanCancel(false);
        showDialog(progressDialog);
        EasyHttp.post(new ApplicationLifecycle())
                .api(new GroupAccreditApi()
                        .setGroup_id(grantTaskEntity.airdrop_group_info.group_id)
                        .setPayment_account(WalletDaoUtils.getCurrent().getAddress())
                ).request(new OnHttpListener<BaseBean<OrderResultEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<OrderResultEntity> result) {
                        if (result.getCode() == 422) {
                            ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_not_satisfied", R.string.group_validate_join_validate_not_satisfied));
                        } else if (result.getData().ship != null) {
                            EventUtil.track(getContext(), EventUtil.Even.验证入群, new HashMap<>());
                            Browser.openUrl(getContext(), (result.getData().ship.url));
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_fail", R.string.group_validate_join_validate_fail));
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                    }
                });
    }

    private void loadTaskData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new GrantTaskApi())
                .request(new OnHttpListener<BaseBean<GrantTaskEntity>>() {

                    @Override
                    public void onSucceed(BaseBean<GrantTaskEntity> result) {
                        showDetail(result.getData());
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(e.getMessage());
                        finishFragment();
                    }

                    @Override
                    public void onEnd(Call call) {
                        if (getVisibleDialog() != null) {
                            getVisibleDialog().dismiss();
                        }
                    }
                });
    }

    private void mintNft() {
        binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_loading", R.string.grant_task_detail_mint_nft_loading));
        binding.tvMintNft.setEnabled(false);
        GrantTaskEntity.ContractInfoEntity contractInfo = grantTaskEntity.contract_info;
        WalletUtil.mintNft(contractInfo.chain_id, contractInfo.address, contractInfo.token_id, new WalletUtil.SendTransactionListener() {
            @Override
            public void paySuccessful(String hash) {
                checkNftBalance(contractInfo, true);
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new MintCallbackApi())
                        .request(null);
            }

            @Override
            public void payError(String error) {
                ToastUtils.showLong(error);
                binding.tvMintNft.setText(LocaleController.getString("grant_task_detail_mint_nft_prepare", R.string.grant_task_detail_mint_nft_prepare));
                binding.tvMintNft.setEnabled(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTaskData();
    }
}
