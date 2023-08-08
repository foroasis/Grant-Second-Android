package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActivityNftDetailsBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import teleblock.model.wallet.NFTInfo;

/**
 * Description：NFT详情
 */
public class NFTDetailsActivity extends BaseFragment {
    private ActivityNftDetailsBinding binding;
    private NFTInfo nftInfo;

    public NFTDetailsActivity(NFTInfo nftInfo) {
        this.nftInfo = nftInfo;
    }

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
        setNavigationBarColor(Color.WHITE, true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(nftInfo.name);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActivityNftDetailsBinding.inflate(LayoutInflater.from(context));
        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        if (nftInfo.chainId == 56 && nftInfo.getOriginal_url().endsWith("svg")) {
            WebView webView;
            if (binding.flNftImage.getChildCount() < 2) {
                webView = createWebView(getContext());
                binding.flNftImage.addView(webView);
            } else {
                webView = (WebView) binding.flNftImage.getChildAt(1);
            }
            webView.loadUrl(nftInfo.getOriginal_url());
        } else {
            Glide.with(getContext())
                    .load(nftInfo.thumb_url)
                    .transform(new RoundedCorners(AndroidUtilities.dp(10)))
                    .into(binding.ivNft);
        }
        binding.tvChainName.setText(nftInfo.blockchain);
        binding.tvTokenStandard.setText(nftInfo.token_standard);
        binding.tvTokenId.setText(nftInfo.token_id + "");
        binding.tvContractAddress.setText(nftInfo.contract_address);
    }

    private WebView createWebView(Context context) {
        WebView webView = new WebView(context);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setInitialScale(200); // 缩放比例
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        return webView;
    }
}
