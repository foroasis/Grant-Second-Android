package teleblock.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;

import org.telegram.messenger.databinding.ActivityTestBinding;

public class TestActivity extends BaseActivity {
    ActivityTestBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(LayoutInflater.from(mActivity));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {

    }
}
