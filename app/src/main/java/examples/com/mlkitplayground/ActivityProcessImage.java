package examples.com.mlkitplayground;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.view.View;

import examples.com.mlkitplayground.databinding.ActivityProcessImageBinding;

//https://code.tutsplus.com/tutorials/getting-started-with-firebase-ml-kit-for-android--cms-31305
public class ActivityProcessImage extends AppCompatActivity {


    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALERY = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    ActivityProcessImageBinding mBinding;
    private Uri mSelectedImage;

    View.OnClickListener mListenerPickImage = view -> {
        checkPermissionsAndStart();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_process_image);
        mBinding.ivHeaderImage.setOnClickListener(mListenerPickImage);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionsAndStart();
                } else {
                    finish();
                }
        }
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_PERMISSIONS);
        } else {
            pickImage();
        }
    }


    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALERY && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            mSelectedImage = data.getData();
            UtilsImageLoader.loadImage(this, mBinding.ivHeaderImage, mSelectedImage);

        }
    }


}
