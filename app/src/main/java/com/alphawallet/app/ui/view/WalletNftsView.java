package teleblock.ui.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnDownloadListener;
import com.hjq.http.model.HttpMethod;
import com.luck.picture.lib.utils.DateUtils;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yalantis.ucrop.UCrop;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewNftEmptyBinding;
import org.telegram.messenger.databinding.ViewWalletHomeBinding;
import org.telegram.ui.ActionBar.AlertDialog;

import java.io.File;

import teleblock.blockchain.BlockCallback;
import teleblock.blockchain.BlockFactory;
import teleblock.model.Web3ConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.NFTMetadata;
import teleblock.model.wallet.NFTResponse;
import teleblock.ui.activity.NFTDetailsActivity;
import teleblock.ui.activity.WalletHomeAct;
import teleblock.ui.adapter.NFTAssetsAdapter;
import teleblock.ui.dialog.CommonTipsDialog;
import teleblock.util.JsonUtil;
import teleblock.util.MMKVUtil;
import teleblock.widget.divider.CustomItemDecoration;
import timber.log.Timber;

public class WalletNftsView extends FrameLayout implements OnRefreshListener, OnLoadMoreListener, OnItemClickListener {

    private ViewWalletHomeBinding binding;
    private final WalletHomeAct walletHomeAct;
    private NFTAssetsAdapter nftListAdapter;
    private String order_direction = "desc";
    private String limit = "10";
    private String cursor;

    public WalletNftsView(WalletHomeAct walletHomeAct) {
        super(walletHomeAct.getParentActivity());
        this.walletHomeAct = walletHomeAct;
        initView();
        initData();
    }

    private void initView() {
        binding = ViewWalletHomeBinding.inflate(LayoutInflater.from(getContext()), this, true);

    }

    private void initData() {
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerView.addItemDecoration(new CustomItemDecoration(2, 20f, 20f, true));
        nftListAdapter = new NFTAssetsAdapter();
        nftListAdapter.setEmptyView(createEmptyView());
        nftListAdapter.getEmptyLayout().setVisibility(View.GONE);
        nftListAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        nftListAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(nftListAdapter);
        binding.refreshLayout.autoRefresh();
    }

    private View createEmptyView() {
        ViewNftEmptyBinding binding = ViewNftEmptyBinding.inflate(LayoutInflater.from(getContext()));
        binding.tvEmptyNft.setText(LocaleController.getString("nft_assets_empty_text", R.string.nft_assets_empty_text));
        return binding.getRoot();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        cursor = null;
        loadData();
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    private void loadData() {
        Web3ConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
        BlockFactory.get(chainType.getId()).getNftList(walletHomeAct.address, cursor, new BlockCallback<NFTResponse>() {
            @Override
            public void onProgress(int index, String data) {
                NFTInfo nftInfo = JsonUtil.parseJsonToBean(data, NFTInfo.class);
                if (nftInfo != null) {
                    nftListAdapter.setData(index, nftInfo);
                }
            }

            @Override
            public void onSuccess(NFTResponse nftResponse) {
                if (TextUtils.isEmpty(cursor)) {
                    nftListAdapter.setList(nftResponse.assets);
                } else {
                    nftListAdapter.addData(nftResponse.assets);
                }
                if (TextUtils.isEmpty(nftResponse.next)) {
                    nftListAdapter.getLoadMoreModule().loadMoreEnd(true);
                } else {
                    nftListAdapter.getLoadMoreModule().loadMoreComplete();
                    cursor = nftResponse.next;
                }
                binding.refreshLayout.finishRefresh();
                if (nftListAdapter.getData().isEmpty()) {
                    nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String msg) {
                nftListAdapter.getLoadMoreModule().loadMoreFail();
                binding.refreshLayout.finishRefresh();
                nftListAdapter.getEmptyLayout().setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        NFTInfo nftInfo = nftListAdapter.getItem(position);
        walletHomeAct.presentFragment(new NFTDetailsActivity(nftInfo));
    }

    private void downloadPhoto(NFTInfo nftInfo, AlertDialog progressDialog, String path) {
        EasyHttp.download(new ApplicationLifecycle())
                .method(HttpMethod.GET)
                .file(new File(path))
                .url(nftInfo.getOriginal_url())
                .listener(new OnDownloadListener() {
                    @Override
                    public void onStart(File file) {
                        progressDialog.show();
                    }

                    @Override
                    public void onProgress(File file, int progress) {
                        Timber.i("onProgress-->" + progress);
                    }

                    @Override
                    public void onComplete(File file) {
                        openPhotoForSelect(nftInfo, file);
                    }

                    @Override
                    public void onError(File file, Exception e) {
                        Timber.e("onError-->" + e);
                        FileUtils.delete(file);
                    }

                    @Override
                    public void onEnd(File file) {
                        progressDialog.dismiss();
                    }
                }).start();
    }

    private void handleSvgPhoto(NFTInfo nftInfo, AlertDialog progressDialog, String path) {
        progressDialog.show();
        Glide.with(walletHomeAct.getParentActivity()).load(nftInfo.getOriginal_url()).addListener(new RequestListener<Drawable>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                progressDialog.dismiss();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<String>() {
                    @Override
                    public String doInBackground() throws Throwable {
                        Bitmap bitmap = ImageUtils.view2Bitmap(binding.ivNftAvatar);
                        FileIOUtils.writeFileFromBytesByStream(path, ConvertUtils.bitmap2Bytes(bitmap));
                        return null;
                    }

                    @Override
                    public void onSuccess(String result) {
                        progressDialog.dismiss();
                        openPhotoForSelect(nftInfo, new File(path));
                    }
                });
                return false;
            }
        }).into(binding.ivNftAvatar);
    }

    private void openPhotoForSelect(NFTInfo nftInfo, File file) {
        walletHomeAct.nftInfo = nftInfo;
        Uri inputUri = Uri.fromFile(file);
        String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(PathUtils.getExternalAppCachePath(), fileName));
        UCrop uCrop = UCrop.of(inputUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);
        options.setCircleDimmedLayer(true);
        options.withAspectRatio(1, 1);
        options.isDarkStatusBarBlack(true);
        uCrop.withOptions(options);
        uCrop.start(walletHomeAct.getParentActivity());
    }
}