package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogGameDoubleBetBinding;

import java.util.List;

/**
 * 创建日期：2023/3/9
 * 描述：下注加倍
 */
public class GameBetDoubleDialog extends BaseAlertDialog {

    private DialogGameDoubleBetBinding binding;
    private int bet_double, start, end;

    public GameBetDoubleDialog(Context context, int bet_double, List<Integer> multiple_range, OnCloseListener onCloseListener) {
        super(context, onCloseListener);
        this.bet_double = bet_double;
        start = (int) CollectionUtils.get(multiple_range, 0);
        end = (int) CollectionUtils.get(multiple_range, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameDoubleBetBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        initView();
        initData();
    }

    private void initView() {
        binding.tvBetTitle.setText(LocaleController.getString("game_double_bet_title", R.string.game_double_bet_title));
        binding.tvBetDesc.setText(String.format(LocaleController.getString("game_double_bet_desc", R.string.game_double_bet_desc), start, end));
        binding.tvBetConfirm.setText(LocaleController.getString("game_double_bet_confirm", R.string.game_double_bet_confirm));

        binding.ivBetClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        binding.etBetNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    int number = Integer.parseInt(s.toString());
                    if (number < start || number > end) {
                        binding.etBetNumber.setText("");
//                        binding.etBetNumber.setSelection(1);
                    }
                } else {
//                    binding.etBetNumber.setText("1");
//                    binding.etBetNumber.setSelection(1);
                }
            }
        });
        binding.tvBetConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = binding.etBetNumber.getText().toString();
                if (TextUtils.isEmpty(number) || "0".equals(number)) return;
                onCloseListener.onClose(number);
                dismiss();
            }
        });
    }

    private void initData() {
        binding.etBetNumber.setText(bet_double + "");
    }
}