package teleblock.ui.dialog.sendredpacket;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogChaintypeSelectorBinding;

import java.util.List;

import teleblock.model.BonusConfigEntity;
import teleblock.ui.adapter.SendRedpacketSelectorChainTypeAdapter;

/**
 * Time:2023/1/4
 * Author:Perry
 * Description：发送红包选择链对话框
 */
public class SendRedpacketSelectorChainTypeDialog extends Dialog {
    private DialogChaintypeSelectorBinding binding;

    private SendRedpacketSelectorChainTypeAdapter mSendRedpacketSelectorChainTypeAdapter;

    public SendRedpacketSelectorChainTypeDialog setCurrentChainType(BonusConfigEntity.Config currentChainType, List<BonusConfigEntity.Config> chainTypeConfigs) {
        if (mSendRedpacketSelectorChainTypeAdapter.getData().isEmpty()) {
            mSendRedpacketSelectorChainTypeAdapter.setList(chainTypeConfigs);
        }

        mSendRedpacketSelectorChainTypeAdapter.setCurrentChainTypeData(currentChainType);
        return this;
    }

    public SendRedpacketSelectorChainTypeDialog(
            @NonNull Context context,
            SelectorChaintypeDialogListener listener
    ) {
        super(context, R.style.dialog2);
        setCancelable(true);
        binding = DialogChaintypeSelectorBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        initView(listener);
    }

    private void initView(SelectorChaintypeDialogListener listener) {
        binding.tvTitle.setText(LocaleController.getString("dialog_transfer_selector_chaintype_title", R.string.dialog_transfer_selector_chaintype_title));
        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));

        mSendRedpacketSelectorChainTypeAdapter = new SendRedpacketSelectorChainTypeAdapter();
        binding.rvSelect.setAdapter(mSendRedpacketSelectorChainTypeAdapter);

        binding.fl.setOnClickListener(view -> dismiss());
        binding.tvTitle.setOnClickListener(view -> dismiss());
        //点击事件
        mSendRedpacketSelectorChainTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            listener.selectorChainData(mSendRedpacketSelectorChainTypeAdapter.getData().get(position));
            dismiss();
        });
    }

    public interface SelectorChaintypeDialogListener {
        void selectorChainData(BonusConfigEntity.Config currentChainType);
    }
}