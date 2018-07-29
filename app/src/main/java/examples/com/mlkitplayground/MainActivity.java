package examples.com.mlkitplayground;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import examples.com.mlkitplayground.databinding.ActivityMainSelectionBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainSelectionBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_selection);


        mBinding.btnTextDetection.setOnClickListener(v -> ActivityProcessImage.newInstance(MainActivity.this, ActivityProcessImage.PROCESS_OCR));

        mBinding.btnBarcodeDetection.setOnClickListener(v -> ActivityProcessImage.newInstance(MainActivity.this, ActivityProcessImage.PROCESS_BARCODE));

        mBinding.btnFaceDetection.setOnClickListener(v -> ActivityProcessImage.newInstance(MainActivity.this, ActivityProcessImage.PROCESS_FACES));

        mBinding.btnImageLabeling.setOnClickListener(v -> ActivityProcessImage.newInstance(MainActivity.this, ActivityProcessImage.PROCESS_IMAGE_LABELING));
    }
}
