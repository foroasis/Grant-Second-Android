package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.CollectionUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogChaintypeSelectorBinding;

import java.util.ArrayList;
import java.util.List;

import teleblock.model.Web3ConfigEntity;
import teleblock.ui.adapter.TranferSelectorChainTypeAdapter;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：选择链类型的对话框
 */
public class ChainTypeSelectorDialog extends Dialog {

    private boolean isNftGallery;
    private DialogChaintypeSelectorBinding binding;

    private TranferSelectorChainTypeAdapter mTranferSelectorChainTypeAdapter;

    public ChainTypeSelectorDialog setCurrentChainType(Web3ConfigEntity.WalletNetworkConfigChainType mWalletNetworkConfigChainType) {
        mTranferSelectorChainTypeAdapter.setCurrentChainTypeData(mWalletNetworkConfigChainType);
        return this;
    }

    public ChainTypeSelectorDialog(@NonNull Context context, TransferSelectorChaintypeDialogListener listener) {
        this(context, false, listener);
    }

    public ChainTypeSelectorDialog(@NonNull Context context, boolean isNftGallery, TransferSelectorChaintypeDialogListener listener) {
        super(context, R.style.dialog2);
        this.isNftGallery = isNftGallery;
        binding = DialogChaintypeSelectorBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        setCancelable(true);

        initView(listener);
        initData();
    }

    private void initView(TransferSelectorChaintypeDialogListener listener) {
        binding.tvTitle.setText(LocaleController.getString("dialog_transfer_selector_chaintype_title", R.string.dialog_transfer_selector_chaintype_title));
        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));

        mTranferSelectorChainTypeAdapter = new TranferSelectorChainTypeAdapter();
        binding.rvSelect.setAdapter(mTranferSelectorChainTypeAdapter);

        binding.fl.setOnClickListener(view -> dismiss());
        binding.tvTitle.setOnClickListener(view -> dismiss());
        //点击事件
        mTranferSelectorChainTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            listener.selectorChainData(mTranferSelectorChainTypeAdapter.getData().get(position));
            dismiss();
        });
    }

    private void initData() {
        if (isNftGallery) {
            List<Web3ConfigEntity.WalletNetworkConfigChainType> chainTypeList = new ArrayList<>();
            chainTypeList.add(new Web3ConfigEntity.WalletNetworkConfigChainType(0));
            CollectionUtils.forAllDo(MMKVUtil.getWeb3ConfigData().getChainType(), (index, item) -> {
                if (CollectionUtils.find(MMKVUtil.getWeb3ConfigData().getNftMarketAddress(), nftMarketAddress -> nftMarketAddress.getChain_id() == item.getId()) != null) {
                    chainTypeList.add(item);
                }
            });
            mTranferSelectorChainTypeAdapter.setList(chainTypeList);
        } else {
            mTranferSelectorChainTypeAdapter.setList(MMKVUtil.getWeb3ConfigData().getChainType());
        }
    }

    public interface TransferSelectorChaintypeDialogListener {
        void selectorChainData(Web3ConfigEntity.WalletNetworkConfigChainType data);
    }
}
