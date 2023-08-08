package teleblock.ui.activity.nft;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityNftNopriceInfoBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.model.wallet.NFTInfo;
import teleblock.widget.GlideHelper;

/**
 * Time:2023/5/30
 * Author:Perry
 * Description：nft信息页面
 */
public class NftNoPriceInfoActivity extends BaseFragment {

    private ActivityNftNopriceInfoBinding binding;
    private NFTInfo nftInfo;

    public NftNoPriceInfoActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        nftInfo = (NFTInfo) getArguments().getSerializable("nft_info");
        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(nftInfo.name);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityNftNopriceInfoBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("nft_info_details", R.string.nft_info_details));
        binding.tvBlockNameTitle.setText(LocaleController.getString("nft_info_blockchain", R.string.nft_info_blockchain));
        binding.tvTokenTypeTitle.setText(LocaleController.getString("nft_info_token_standard", R.string.nft_info_token_standard));
        binding.tvTokenIdTitle.setText(LocaleController.getString("nft_info_token_id", R.string.nft_info_token_id));
        binding.tvContractAddressTitle.setText(LocaleController.getString("nft_info_contract_address", R.string.nft_info_contract_address));
        binding.tvBack.setText(LocaleController.getString("nft_back", R.string.nft_back));
        binding.tvSellNft.setText(LocaleController.getString("nft_sell", R.string.nft_sell));

        binding.tvBack.setOnClickListener(v -> finishFragment());

        binding.tvSellNft.setOnClickListener(v -> presentFragment(new NftSellPriceActivity(getArguments()), true));

        GlideHelper.displayImage(binding.ivNft.getContext(), binding.ivNft, nftInfo.thumb_url);
        binding.tvBlockName.setText(nftInfo.blockchain);
        binding.tvTokenType.setText(nftInfo.token_standard);
        binding.tvTokenId.setText(String.valueOf(nftInfo.token_id));
        binding.tvContractAddress.setText(nftInfo.contract_address);
    }
}