package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogGroupConditionJoinBinding;
import org.telegram.messenger.databinding.ItemGroupJoinTagBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import okhttp3.Call;
import teleblock.model.OrderResultEntity;
import teleblock.model.PrivateGroupEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.GroupAccreditApi;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：条件入群
 */
public class GroupValidateJoinDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupConditionJoinBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;

    public GroupValidateJoinDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupConditionJoinBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
//        binding.tvTagTitle.setText(LocaleController.getString("group_validate_join_tag_title", R.string.group_validate_join_tag_title));
        binding.tvConditionJoinTitle.setText(LocaleController.getString("group_validate_join_condition_join_title", R.string.group_validate_join_condition_join_title));
        binding.tvJoinSuccess.setText(LocaleController.getString("group_validate_join_join_success", R.string.group_validate_join_join_success));
        binding.tvValidating.setText(LocaleController.getString("group_validate_join_validating", R.string.group_validate_join_validating));
        binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_join", R.string.group_validate_join_validate_join));
        binding.tvValidateCancel.setText(LocaleController.getString("group_validate_join_validate_cancel", R.string.group_validate_join_validate_cancel));
        binding.etInputNftId.setHint(LocaleController.getString("group_validate_join_condition_join_input_nft", R.string.group_validate_join_condition_join_input_nft));

        binding.tvCloseDialog.setOnClickListener(this);
        binding.tvValidateJoin.setOnClickListener(this);
        binding.tvValidateCancel.setOnClickListener(this);
    }

    private void initData() {
        GlideHelper.displayImage(getContext(), binding.ivGroupAvatar, privateGroup.getAvatar());
        binding.tvGroupName.setText(privateGroup.getTitle());
        binding.tvGroupDesc.setText(privateGroup.getDescription());
//        List<String> tagList = privateGroup.getTags();
//        if (CollectionUtils.isEmpty(tagList)) {
//            binding.tvTagTitle.setVisibility(View.GONE);
//            binding.flTag.setVisibility(View.GONE);
//        } else {
//            for (String tag : tagList) {
//                View view = createTagView(tag);
//                binding.flTag.addView(view);
//            }
//        }

        if (privateGroup.getToken_name().equalsIgnoreCase("ERC20")) {
            String format = LocaleController.getString("group_validate_join_condition_join", R.string.group_validate_join_condition_join);
            binding.tvConditionJoin.setText(String.format(format, privateGroup.getAmount() + privateGroup.getCurrency_name()));
        } else {
            binding.tvConditionJoin.setText(String.format(LocaleController.getString("group_validate_join_condition_join_hold_nft", R.string.group_validate_join_condition_join_hold_nft), WalletUtil.formatAddress(privateGroup.getToken_address())));
            if (privateGroup.getToken_name().equalsIgnoreCase("ERC1155") && (StringUtils.isEmpty(privateGroup.getNft_token_id()) || privateGroup.getNft_token_id().equals("0"))) {
                binding.etInputNftId.setVisibility(View.VISIBLE);
            } else {
                binding.etInputNftId.setVisibility(View.GONE);
            }
        }
    }

    private View createTagView(String tag) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_group_join_tag, null);
        ItemGroupJoinTagBinding binding = ItemGroupJoinTagBinding.bind(view);
        binding.tvTag.setText(tag);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close_dialog:
            case R.id.tv_validate_cancel:
                dismiss();
                break;
            case R.id.tv_validate_join:
                validateJoin();
                break;
        }
    }

    private void validateJoin() {
        String nftTokenId = "";
        if (privateGroup.getToken_name().equals("ERC1155") && (StringUtils.isEmpty(privateGroup.getNft_token_id()) || privateGroup.getNft_token_id().equals("0"))) {
            nftTokenId = binding.etInputNftId.getText().toString().trim();
            if (StringUtils.isEmpty(nftTokenId)) {
                ToastUtils.showLong(LocaleController.getString("group_validate_join_condition_join_input_nft", R.string.group_validate_join_condition_join_input_nft));
                return;
            }
        }

        binding.llValidating.setVisibility(View.VISIBLE);
        binding.tvValidateCancel.setVisibility(View.VISIBLE);
        binding.tvValidateJoin.setVisibility(View.GONE);

        EasyHttp.post(new ApplicationLifecycle())
                .api(new GroupAccreditApi()
                        .setGroup_id(privateGroup.getId())
                        .setNft_token_id(nftTokenId)
                        .setPayment_account(WalletDaoUtils.getCurrent().getAddress())
                ).request(new OnHttpListener<BaseBean<OrderResultEntity>>() {

                    @Override
                    public void onEnd(Call call) {
                        binding.llValidating.setVisibility(View.GONE);
                        binding.tvValidateCancel.setVisibility(View.GONE);
                        binding.tvValidateJoin.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSucceed(BaseBean<OrderResultEntity> result) {
                        if (result.getCode() == 422) {
                            ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_not_satisfied", R.string.group_validate_join_validate_not_satisfied));
                            binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_join", R.string.group_validate_join_validate_join));
                        } else if (result.getData().ship != null) {
                            binding.tvValidateJoin.setVisibility(View.GONE);
                            binding.tvJoinSuccess.setVisibility(View.VISIBLE);
                            Browser.openUrl(fragment.getParentActivity(), (result.getData().ship.url));
                            dismiss();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showLong(LocaleController.getString("group_validate_join_validate_fail", R.string.group_validate_join_validate_fail));
                        binding.tvValidateJoin.setText(LocaleController.getString("group_validate_join_validate_join", R.string.group_validate_join_validate_join));
                    }
                });
    }
}