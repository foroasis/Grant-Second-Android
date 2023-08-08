package teleblock.ui.view;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ResourceUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.OtherUserNftGalleryViewBinding;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ProfileActivity;

import teleblock.manager.NFTGalleryManager;
import teleblock.model.wallet.WalletInfo;
import teleblock.ui.activity.NFTGalleryActivity;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.widget.divider.CustomItemDecoration;

/**
 * Time:2023/5/30
 * Author:Perry
 * Description：其他用户钱包的nft列表view
 */
public class OtherUserNftGalleryView extends ConstraintLayout {

    private BaseFragment baseFragment;
    private OtherUserNftGalleryViewBinding binding;

    //钱包信息数据
    private WalletInfo walletInfo;
    private long dialogId;

    public OtherUserNftGalleryView(@NonNull BaseFragment baseFragment, WalletInfo walletInfo, long dialogId) {
        super(baseFragment.getParentActivity());
        this.baseFragment = baseFragment;
        this.walletInfo = walletInfo;
        this.dialogId = dialogId;
        binding = OtherUserNftGalleryViewBinding.inflate(LayoutInflater.from(getContext()), this, true);

        setThemeColor();
        initView();
    }

    private void setThemeColor() {
        binding.tvNftTitle.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        binding.tvNftShowAll.setTextColor(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));

        Drawable arrowIcon = ResourceUtils.getDrawable(R.drawable.arrow_newchat);
        arrowIcon.setColorFilter(new PorterDuffColorFilter(baseFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), PorterDuff.Mode.MULTIPLY));
        binding.tvNftShowAll.getHelper().setIconNormalRight(arrowIcon);
    }

    private void initView() {
        binding.tvNftTitle.setText(LocaleController.getString("nft_gallery_title", R.string.nft_gallery_title));
        binding.tvNftShowAll.setText(LocaleController.getString("nft_gallery_show_all", R.string.nft_gallery_show_all));

        binding.rvNft.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvNft.addItemDecoration(new CustomItemDecoration(RecyclerView.HORIZONTAL,"#00000000",5f));
        NFTAssetsAdapter nftListAdapter = new NFTAssetsAdapter(true,1);
        nftListAdapter.setEmptyView(nftListAdapter.createEmptyView(getContext()));
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.loadData(walletInfo.getWallet_info().get(0).getWallet_address());
        binding.rvNft.setAdapter(nftListAdapter);

        nftListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (baseFragment instanceof ProfileActivity) {
                ProfileActivity profileActivity = (ProfileActivity) baseFragment;
                NFTGalleryManager.getInstance().nftIndexPage(baseFragment, nftListAdapter, profileActivity.getDialogId(), false, position);
            }
        });

        //跳转到全部nft页面
        binding.tvNftShowAll.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("user_id", walletInfo.getTg_user_id());
            bundle.putLong("dialog_id", dialogId);
            bundle.putString("address", walletInfo.getWallet_info().get(0).getWallet_address());

            baseFragment.presentFragment(new NFTGalleryActivity(bundle));
        });
    }
}
