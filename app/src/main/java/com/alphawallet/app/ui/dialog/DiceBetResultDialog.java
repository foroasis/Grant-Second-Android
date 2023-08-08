package teleblock.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogDiceBetResultBinding;
import org.telegram.ui.ChatActivity;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.util.StringUtil;
import teleblock.util.parse.DiceParseUtil;

/**
 * 创建日期：2023/3/9
 * 描述：骰子下注结果
 */
public class DiceBetResultDialog extends BaseAlertDialog {

    private ChatActivity chatActivity;
    private DialogDiceBetResultBinding binding;
    private String hash;
    private Integer[] diceNum = new Integer[]{0, 0, 0};

    public DiceBetResultDialog(ChatActivity chatActivity, String hash) {
        super(chatActivity.getContext());
        this.chatActivity = chatActivity;
        this.hash = hash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogDiceBetResultBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        initData();
    }

    private void initView() {
        binding.tvResultDesc.setText(LocaleController.getString("dice_bet_result_desc", R.string.dice_bet_result_desc));
        binding.tvConfirm.setText(LocaleController.getString("dice_bet_result_confirm", R.string.dice_bet_result_confirm));

        binding.ivClose.setOnClickListener(v -> dismiss());
        binding.tvConfirm.setOnClickListener(v -> {
            //加密数据
            String str = DiceParseUtil.setParseStr(
                    chatActivity.getDialogId(),
                    CollectionUtils.newArrayList(diceNum),
                    hash
            );
            EventBus.getDefault().post(new MessageEvent(EventBusTags.GAME_WIN, (Object) str));
            dismiss();
        });
    }

    private void initData() {
        handeDiceNum();
        handleDiceGif(0, binding.ivDice1, diceNum[0]);
        handleDiceGif(500, binding.ivDice2, diceNum[1]);
        handleDiceGif(1000, binding.ivDice3, diceNum[2]);
    }

    private void handleDiceGif(long delay, ImageView view, int diceNum) {
        AndroidUtilities.runOnUIThread(() -> Glide.with(getContext())
                .asGif()
                .load(R.drawable.dice_loading)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        AndroidUtilities.runOnUIThread(() -> Glide.with(getContext())
                                .asGif()
                                .load(ResourceUtils.getDrawableIdByName("dice_result_gif_" + diceNum))
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .listener(new RequestListener<GifDrawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model1, Target<GifDrawable> target1, boolean isFirstResource1) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GifDrawable resource1, Object model1, Target<GifDrawable> target1, DataSource dataSource1, boolean isFirstResource1) {
                                        if (diceNum != 0) resource1.setLoopCount(1);
                                        return false;
                                    }
                                })
                                .into(view), 1000);
                        return false;
                    }
                })
                .into(view), delay);
    }


    private void handeDiceNum() {
        int firstNum = -1, secondNum = 0;
        for (int i = hash.length(); i-- > 0; ) {
            if (firstNum == -1 && StringUtil.isNumber(String.valueOf(hash.charAt(i)))) {
                firstNum = Integer.parseInt(String.valueOf(hash.charAt(i)));
                continue;
            }
            if (StringUtil.isNumber(String.valueOf(hash.charAt(i)))) {
                secondNum = Integer.parseInt(String.valueOf(hash.charAt(i)));
                break;
            }
        }
        int totalNum = firstNum + secondNum;
        if (totalNum < 3) {
            diceNum[0] = firstNum;
            diceNum[1] = secondNum;
            return;
        }
        int maxNum = Math.min(totalNum, 6);
        while (diceNum[2] > maxNum || diceNum[2] < 1) {
            diceNum[0] = (int) (Math.random() * maxNum) + 1;
            diceNum[1] = (int) (Math.random() * maxNum) + 1;
            diceNum[2] = totalNum - diceNum[0] - diceNum[1];
        }
    }
}