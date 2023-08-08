package com.alphawallet.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogGroupPayJoinBinding;
import org.telegram.messenger.databinding.ItemGroupJoinTagBinding;

import java.util.List;

import teleblock.model.PrivateGroupEntity;
import teleblock.widget.GlideHelper;

/**
 * 创建日期：2022/7/15
 * 描述：付费入群
 */
public class GroupPayJoinDialog extends BaseBottomSheetDialog implements View.OnClickListener {

    private DialogGroupPayJoinBinding binding;
    private PrivateGroupEntity privateGroup;
    private BaseFragment fragment;

    public GroupPayJoinDialog(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        super(fragment.getParentActivity());
        this.fragment = fragment;
        this.privateGroup = privateGroup;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupPayJoinBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
        initData();
    }

    private void initView() {
        binding.tvTagTitle.setText(LocaleController.getString("group_pay_join_tag_title", R.string.group_pay_join_tag_title));
        binding.tvAmountTitle.setText(LocaleController.getString("group_pay_join_amount_title", R.string.group_pay_join_amount_title));
        binding.tvPayJoin.setText(LocaleController.getString("group_pay_join_pay_join", R.string.group_pay_join_pay_join));

        binding.tvCloseDialog.setOnClickListener(this);
        binding.tvPayJoin.setOnClickListener(this);
    }

    private void initData() {
        GlideHelper.displayImage(getContext(), binding.ivGroupAvatar, privateGroup.getAvatar());
        binding.tvGroupName.setText(privateGroup.getTitle());
        binding.tvGroupDesc.setText(privateGroup.getDescription());
        List<String> tagList = privateGroup.getTags();
        if (CollectionUtils.isEmpty(tagList)) {
            binding.tvTagTitle.setVisibility(View.GONE);
            binding.flTag.setVisibility(View.GONE);
        } else {
            for (String tag : tagList) {
                View view = createTagView(tag);
                binding.flTag.addView(view);
            }
        }
        binding.tvPayAmount.setText(privateGroup.getAmount() + " " + privateGroup.getCurrency_name());
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
                dismiss();
                break;
            case R.id.tv_pay_join:
                new GroupPayConfirmDialog(fragment, privateGroup).show();
                dismiss();
                break;
        }
    }
}
