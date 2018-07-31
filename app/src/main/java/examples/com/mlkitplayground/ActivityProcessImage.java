package examples.com.mlkitplayground;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.io.IOException;

import examples.com.mlkitplayground.databinding.ActivityProcessImageBinding;

import static com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE;
import static com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS;
import static com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS;

/**
 * Activity that holds key methods for Firebase ML Kit processing operations
 * <p>
 * <p>
 * The included setup is based on the following documentation
 * https://firebase.google.com/docs/ml-kit/android/
 * <p>
 * Use full tutorial to get started:
 * https://medium.com/google-developer-experts/exploring-firebase-mlkit-on-android-face-detection-part-two-de7e307c52e0
 */

public class ActivityProcessImage extends AppCompatActivity {


    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALERY = 2;
    private static final int REQUEST_PERMISSIONS = 3;

    private static final String TAG = "Process";


    public static final int PROCESS_FACES = 11;
    public static final int PROCESS_DEVICE_OCR = 12;
    public static final int PROCESS_IMAGE_LABELING = 13;
    public static final int PROCESS_BARCODE = 14;
    public static final int PROCESS_CLOUD_OCR = 15;
    private ActivityProcessImageBinding mBinding;
    private Uri mSelectedImage;

    private final View.OnClickListener mListenerPickImage = view -> {
        checkPermissionsAndStart();
    };

    private int mActiveProcess;

    public static void newInstance(Context context, int activeProcess) {
        Intent intent = new Intent(context, ActivityProcessImage.class);
        intent.putExtra("process", activeProcess);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_process_image);
        mActiveProcess = getIntent().getIntExtra("process", 0);
        mBinding.fabPickImage.setOnClickListener(mListenerPickImage);
        mBinding.rvData.setVisibility(View.GONE);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
            if (mActiveProcess != 0) {
                processImageML(mSelectedImage, mActiveProcess);
            } else {
                Snackbar.make(mBinding.getRoot(), "No selected process", Snackbar.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Configuration options Face detection
     */
    private FirebaseVisionFaceDetectorOptions configureFaceML() {
        return new FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(ACCURATE_MODE)
                .setLandmarkType(ALL_LANDMARKS)
                .setClassificationType(ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.1f)
                .build();

    }

    /**
     * Configuration options Image Labeling
     */
    private FirebaseVisionLabelDetectorOptions configureLabelDetectionML() {
        return new FirebaseVisionLabelDetectorOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();
    }

    /**
     * Configure cloud Text Recognition(OCR)
     *
     * @return - configurations
     */
    private FirebaseVisionCloudDetectorOptions configureCloudOCR() {
        return new FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(15)
                .build();
    }


    private void processImageML(Uri selectedImage, int processingType) {

        switch (processingType) {
            case PROCESS_FACES:
                processFaceImageML(selectedImage);
                break;

            case PROCESS_DEVICE_OCR:
                processTextOCR(selectedImage);
                break;

            case PROCESS_IMAGE_LABELING:
                processImageLabeling(selectedImage);
                break;

            case PROCESS_BARCODE:
                processBarcodeImageML(selectedImage);
                break;

            case PROCESS_CLOUD_OCR:
                processCloudTextOCR(selectedImage);
                break;
        }
    }

    private void processImageLabeling(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);
            FirebaseVision
                    .getInstance()
                    .getVisionLabelDetector(configureLabelDetectionML())
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionLabels -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (FirebaseVisionLabel label : firebaseVisionLabels) {
                            Log.d(TAG, "onSuccess: "
                                    + label.getConfidence() + " "
                                    + label.getEntityId() + " "
                                    + label.getLabel());

                            stringBuilder.append(label.getLabel() + "confidence: " + label.getConfidence() + " Id:" + label.getEntityId());
                            stringBuilder.append("\n");
                        }
                        mBinding.tvResult.setText(stringBuilder);
                    }).addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCloudTextOCR(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);
            FirebaseVision
                    .getInstance()
                    .getVisionCloudTextDetector(configureCloudOCR())
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionCloudText ->
                    {
                        Log.d(TAG, "onSuccess: " + firebaseVisionCloudText.getText());
                        mBinding.tvResult.setText(firebaseVisionCloudText.getText());
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTextOCR(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);
            FirebaseVision
                    .getInstance()
                    .getVisionTextDetector()
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionText -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                            stringBuilder.append(block.getText());
                            stringBuilder.append("\n");
                        }
                        mBinding.tvResult.setText(stringBuilder.toString());
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processFaceImageML(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);

            FirebaseVision
                    .getInstance()
                    .getVisionFaceDetector(configureFaceML())
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionFaces -> {
                        StringBuilder stringBuilder = new StringBuilder();

                        if (firebaseVisionFaces.size() > 0) {
                            for (FirebaseVisionFace face :
                                    firebaseVisionFaces) {

                                stringBuilder.append("Left eye opened: " + face.getLeftEyeOpenProbability()
                                        + " Right eye opended:" + face.getRightEyeOpenProbability() + " Smiling:" + face.getSmilingProbability());
                            }
                            mBinding.tvResult.setText(stringBuilder);
                        }

                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processBarcodeImageML(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);

            FirebaseVision
                    .getInstance()
                    .getVisionBarcodeDetector()
                    .detectInImage(image)
                    .addOnSuccessListener(barcodes -> {
                        if (barcodes.size() > 0) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (FirebaseVisionBarcode barcode : barcodes) {
                                Log.d(TAG, "onSuccess: " + barcode.getRawValue());
                                stringBuilder.append(barcode.getRawValue());
                                stringBuilder.append("\n");
                            }
                            mBinding.tvResult.setText(stringBuilder);
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
