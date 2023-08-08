package teleblock.ui.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCcprofileMintBinding;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.HashMap;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.network.api.blockchain.bnb.CyberConnectApi;
import teleblock.ui.activity.MyWalletActivity;
import teleblock.util.EventUtil;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletDaoUtils;
import teleblock.util.WalletTransferUtil;
import teleblock.util.WalletUtil;
import teleblock.widget.CommonCallback;
import teleblock.widget.GlideHelper;

/**
 * 铸造CyberProfile
 */
public class CcProfileMintDialog extends BaseAlertDialog {

    private DialogCcprofileMintBinding binding;
    private BaseFragment baseFragment;
    private String address;
    private boolean minted;
    private boolean showFollowed;

    private Bitmap qrCode;

    public CcProfileMintDialog(BaseFragment baseFragment) {
        super(baseFragment.getParentActivity());
        this.baseFragment = baseFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = org.telegram.messenger.databinding.DialogCcprofileMintBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        initData();
    }

    private void initView() {
        EventUtil.track(getContext(), EventUtil.Even.ccProfile_mint弹窗展示, new HashMap<>());
        binding.tvMintTitle.setText(LocaleController.getString("ccprofile_mint_title_not_sign", R.string.ccprofile_mint_title_not_sign));
        binding.tvMintDesc.setText(LocaleController.getString("ccprofile_mint_desc_not_sign", R.string.ccprofile_mint_desc_no_sign));
        binding.etMintName.setHint(LocaleController.getString("ccprofile_mint_hint_text", R.string.ccprofile_mint_hint_text));
        binding.tvMintConfirm.setText(LocaleController.getString("ccprofile_mint_sign_message", R.string.ccprofile_mint_sign_message));
        binding.tvCancel.setText(LocaleController.getString("ccprofile_mint_cancel", R.string.ccprofile_mint_cancel));
        GlideHelper.displayImage(binding.ivDefault.getContext(), binding.ivDefault, R.drawable.ccprofile_mint_avator_ic);

        binding.etMintName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.llMintConfirm.setEnabled(s.length() >= 12);
            }
        });
        binding.llMintConfirm.setOnClickListener(v -> {
            if (binding.pbMintLoading.getVisibility() == View.VISIBLE) return;
            binding.tvMintConfirm.setVisibility(View.INVISIBLE);
            binding.pbMintLoading.setVisibility(View.VISIBLE);
            if (MMKVUtil.ccRefreshToken().isEmpty()) {
                gotoSign();
            } else if (!minted) {//没有minted完成
                EventUtil.track(getContext(), EventUtil.Even.ccProfile_mint弹窗按钮点击, new HashMap<>());
                gotoMint();
            } else if (minted && !showFollowed) {//minted完成
                showFollowView();
            } else if (showFollowed) {//开启自动关注开关，关注官方账户
                gotoFollowAlphaAccount();
            } else {
                baseFragment.presentFragment(new MyWalletActivity());
                dismiss();
            }
        });
        binding.tvCancel.setOnClickListener(view -> dismiss());
    }

    private void initData() {
        address = WalletDaoUtils.getCurrent().getAddress();
        if (!MMKVUtil.ccRefreshToken().isEmpty()) { // 已签名
            String format = LocaleController.getString("ccprofile_mint_title_signed", R.string.ccprofile_mint_title_signed);
            binding.tvMintTitle.setText(String.format(format, WalletUtil.formatAddress(address)));
            binding.tvMintDesc.setText(LocaleController.getString("ccprofile_mint_desc_signed", R.string.ccprofile_mint_desc_signed));
            binding.etMintName.setVisibility(View.VISIBLE);
            binding.tvMintConfirm.setVisibility(View.VISIBLE);
            binding.pbMintLoading.setVisibility(View.INVISIBLE);
            binding.tvMintConfirm.setText(LocaleController.getString("ccprofile_mint_goto_mint", R.string.ccprofile_mint_goto_mint));
            binding.tvMintGas.setVisibility(View.VISIBLE);
            binding.llMintConfirm.setEnabled(false);
        }
    }

    private void gotoSign() {
        CyberConnectApi.loginGetMessage(address, new CommonCallback<String>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                WalletTransferUtil.signMessageData(56, data, new CommonCallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        super.onSuccess(data);
                        CyberConnectApi.loginVerify(address, data, new CommonCallback<String>() {
                            @Override
                            public void onSuccess(String data) {
                                super.onSuccess(data);
                                initData();
                            }

                            @Override
                            public void onError(String msg) {
                                super.onError(msg);
                                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                                binding.tvMintConfirm.setVisibility(View.VISIBLE);
                                binding.pbMintLoading.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(String msg) {
                        super.onError(msg);
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        binding.tvMintConfirm.setVisibility(View.VISIBLE);
                        binding.pbMintLoading.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                binding.tvMintConfirm.setVisibility(View.VISIBLE);
                binding.pbMintLoading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void gotoMint() {
        String mintNftName = binding.etMintName.getText().toString();
        KeyboardUtils.hideSoftInput(binding.etMintName);
        CyberConnectApi.createProfile(address, mintNftName, new CommonCallback<String>() {
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
//        NFTInfo nftInfo = new NFTInfo();
//        nftInfo.token_standard = "ERC721";
//        nftInfo.contract_address = "0x2723522702093601e6360CAe665518C4f63e9dA6";
//        nftInfo.token_id = "474";
//        ((BnbBlockExplorer) BlockFactory.get(56)).getNftInfo(nftInfo, new BlockCallback<String>() {
//            @Override
//            public void onSuccess(String data) {
//                });
//            }
                MMKVUtil.ccprofileHandler(mintNftName);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_CCPROFILE_ID));
                EventUtil.track(getContext(), EventUtil.Even.ccProfile_mint成功展示, new HashMap<>());
                binding.ivDefault.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.INVISIBLE);
                binding.ivCardBg.setVisibility(View.INVISIBLE);
                binding.grpupNft.setVisibility(View.VISIBLE);
                binding.etMintName.setVisibility(View.INVISIBLE);
                binding.tvMintConfirm.setVisibility(View.VISIBLE);
                binding.pbMintLoading.setVisibility(View.INVISIBLE);
                binding.animationView.playAnimation();

                //钱包地址拼接
                String key = "https://link3.to/" + mintNftName;
                //显示二维码
                binding.ivQrcode.setImageBitmap(qrCode = createQR(key, qrCode));

                binding.tvNftName.setText(mintNftName);
                binding.tvNftNameBottom.setText(mintNftName);
                binding.tvLink.setText("link3.to/");

                binding.tvMintTitle.setText(LocaleController.getString("ccprofile_mint_title_minted", R.string.ccprofile_mint_title_minted));
                binding.tvMintDesc.setText(LocaleController.getString("ccprofile_mint_desc_minted", R.string.ccprofile_mint_desc_minted));
                binding.tvMintConfirm.setText(LocaleController.getString("ccprofile_mint_goto_minted", R.string.ccprofile_mint_goto_minted));
                binding.llMintConfirm.setEnabled(true);
                minted = true;
            }

            @Override
            public void onError(String msg) {
                super.onError(msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                binding.tvMintConfirm.setVisibility(View.VISIBLE);
                binding.pbMintLoading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showFollowView() {
        binding.tvMintConfirm.setVisibility(View.VISIBLE);
        binding.tvCancel.setVisibility(View.VISIBLE);
        binding.pbMintLoading.setVisibility(View.GONE);
        binding.grpupNft.setVisibility(View.GONE);
        binding.ivFollow.setVisibility(View.VISIBLE);

        binding.tvMintTitle.setText(LocaleController.getString("ccprofile_mint_title_minted_follow", R.string.ccprofile_mint_title_minted_follow));
        binding.tvMintDesc.setText(LocaleController.getString("ccprofile_mint_desc_minted_follow", R.string.ccprofile_mint_desc_minted_follow));
        binding.tvMintConfirm.setText(LocaleController.getString("ccprofile_mint_goto_minted_follow", R.string.ccprofile_mint_goto_minted_follow));
        binding.tvCancel.setText(LocaleController.getString("ccprofile_mint_cancel_minted_follow", R.string.ccprofile_mint_cancel_minted_follow));
        showFollowed = true;
    }

    private void gotoFollowAlphaAccount() {
        TelegramUtil.followOrUnFollowBnbUser(MMKVUtil.getSystemMsg().getCc_profile(), false, new TelegramUtil.FollowSignatureResultListener() {
            @Override
            public void onStart() {}

            @Override
            public void requestSuccessful() {
                MMKVUtil.autoFollowStatus(true);
                EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_AUTO_FOLLOW_SWITCH));
                dismiss();
            }

            @Override
            public void requestError(String error) {
                ToastUtils.showLong(error);
            }

            @Override
            public void onEnd() {
                binding.tvMintConfirm.setVisibility(View.VISIBLE);
                binding.pbMintLoading.setVisibility(View.GONE);
            }
        });
    }

    public Bitmap createQR(String key, Bitmap oldBitmap) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            QRCodeWriter writer = new QRCodeWriter();
            writer.setIfShowIocn(false);
            Bitmap bitmap = writer.encode(key, 768, 768, hints, oldBitmap);
            return bitmap;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }
}