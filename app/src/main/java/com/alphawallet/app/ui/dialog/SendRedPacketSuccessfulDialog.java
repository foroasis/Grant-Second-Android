package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogSendRedpacketSuccessfulBinding;

import java.math.BigDecimal;

import teleblock.model.ui.MyCoinListData;
import teleblock.util.WalletUtil;
import teleblock.widget.GlideHelper;

/**
 * Time:2022/12/13
 * Author:Perry
 * Description：发送红包成功弹窗
 */
public class SendRedPacketSuccessfulDialog extends BaseBottomSheetDialog {
    private DialogSendRedpacketSuccessfulBinding binding;

    //选中的币种数据
    private MyCoinListData selectorCoinData;
    //转账总金额 单位：coinType
    private BigDecimal totalAmount;
    //合约网址查询
    private String url;
    //交易成功的hash
    private String hash;

    private SendRedPacketSuccessfulDialogListener listener;

    public SendRedPacketSuccessfulDialog(
            @NonNull Context context,
            MyCoinListData selectorCoinData,
            BigDecimal totalAmount,
            String url,
            SendRedPacketSuccessfulDialogListener listener
    ) {
        super(context);
        this.selectorCoinData = selectorCoinData;
        this.totalAmount = totalAmount;
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogSendRedpacketSuccessfulBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        binding.tvUpChainIngTitle.setText(LocaleController.getString("dialog_send_redpackets_upchain_ing", R.string.dialog_send_redpackets_upchain_ing));
        binding.tvUpChainIngTips.setText(LocaleController.getString("dialog_send_redpackets_upchain_ing_tips", R.string.dialog_send_redpackets_upchain_ing_tips));
        binding.tvSeeLinks.setText(LocaleController.getString("dialog_send_redpackets_see_links", R.string.dialog_send_redpackets_see_links));
        binding.tvBack.setText(LocaleController.getString("dialog_send_redpackets_back_up_chatpage", R.string.dialog_send_redpackets_back_up_chatpage));

        binding.tvUpChainIngTitle.setOnClickListener(v -> dismiss());
        binding.tvSeeLinks.setOnClickListener(v -> {
            Browser.openUrl(getContext(), url);
        });

        binding.tvBack.setOnClickListener(v -> {
            dismiss();
            listener.backUpPage();
        });

        //总价
        binding.tvCoinBalance.setText(WalletUtil.bigDecimalScale(totalAmount, 6).toPlainString());
        binding.tvCoinBalanceDoller.setText(WalletUtil.toCoinPriceUSD(totalAmount, selectorCoinData.getPrice(), 6));

        if (!StringUtils.isEmpty(selectorCoinData.getIcon())) {
            GlideHelper.getDrawableGlide(getContext(), selectorCoinData.getIcon(), drawable -> {
                binding.tvCoinBalance.getHelper().setIconNormalTop(drawable);
            });
        } else {
            Drawable coinDrawable = ResourceUtils.getDrawable(selectorCoinData.getIconRes());
            binding.tvCoinBalance.getHelper().setIconNormalTop(coinDrawable);
        }
    }

    public void showUpChainSuccessfulView() {
        binding.tvUpChainIngTitle.setText(LocaleController.getString("dialog_send_redpackets_successful_title", R.string.dialog_send_redpackets_successful_title));
        binding.llTransferSuccessful.setVisibility(View.GONE);
        binding.llUpchainSuccessful.setVisibility(View.VISIBLE);
    }

    public interface SendRedPacketSuccessfulDialogListener {
        void backUpPage();
    }
}
