package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogSelectorChaintypeBinding;

import teleblock.model.Web3ConfigEntity;
import teleblock.ui.adapter.SelectorChainTypeAdapter;
import teleblock.util.MMKVUtil;
import teleblock.util.WalletDaoUtils;


/**
 * Time:2022/12/1
 * Author:Perry
 * Description：选择链类型弹窗
 */
public class SelectorChainTypeDialog extends BaseBottomSheetDialog {

    private DialogSelectorChaintypeBinding binding;
    private int currentItem;

    private SelectorChaintypeDialogListener listener;

    private SelectorChainTypeAdapter mSelectorChainTypeAdapter;

    public SelectorChainTypeDialog(@NonNull Context context, int currentItem, SelectorChaintypeDialogListener listener) {
        super(context);
        this.currentItem = currentItem;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogSelectorChaintypeBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_selector_chaintype_title", R.string.dialog_selector_chaintype_title));
        binding.tvAllChain.setText(LocaleController.getString("dialog_selector_chaintype_all_chain", R.string.dialog_selector_chaintype_all_chain));

        binding.ivClose.setOnClickListener(view -> dismiss());
        binding.tvAllChain.setOnClickListener(v -> {
            if (currentItem != 0) return;
            MMKVUtil.currentChainConfig(null, true);
            mSelectorChainTypeAdapter.setCurrentChainTypeData(null);
            listener.selectorChainData(null);
            dismiss();
        });

        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));
        mSelectorChainTypeAdapter = new SelectorChainTypeAdapter();
        mSelectorChainTypeAdapter.setCurrentChainTypeData(MMKVUtil.currentChainConfig(currentItem == 0));
        binding.rvSelect.setAdapter(mSelectorChainTypeAdapter);


        //点击事件
        mSelectorChainTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            Web3ConfigEntity.WalletNetworkConfigChainType data = mSelectorChainTypeAdapter.getItem(position);
            MMKVUtil.currentChainConfig(data, currentItem == 0);
            listener.selectorChainData(data);
            dismiss();
        });

        if (currentItem != 0) {
            binding.tvAllChain.getHelper()
                    .setIconNormalRight(null)
                    .setIconSelectedRight(null);
        } else {
            binding.tvAllChain.setSelected(mSelectorChainTypeAdapter.getCurrentChainType() == null);
        }
    }

    public interface SelectorChaintypeDialogListener {
        void selectorChainData(Web3ConfigEntity.WalletNetworkConfigChainType data);
    }
}
