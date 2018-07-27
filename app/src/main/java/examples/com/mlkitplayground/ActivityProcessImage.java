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
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import examples.com.mlkitplayground.databinding.ActivityProcessImageBinding;

import static com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE;
import static com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS;
import static com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS;

//https://code.tutsplus.com/tutorials/getting-started-with-firebase-ml-kit-for-android--cms-31305
public class ActivityProcessImage extends AppCompatActivity {


    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALERY = 2;
    private static final int REQUEST_PERMISSIONS = 3;
    private static final int REQUEST_OCR = 4;
    private static final int REQUEST_IMAGE_LABELING = 5;
    private static final String TAG = "Process";


    private static final int PROCESS_FACES = 11;
    private static final int PROCESS_OCR = 12;
    private static final int PROCESS_IMAGE_LABELING = 13;
    private static final int PROCESS_BARCODE = 14;
    ActivityProcessImageBinding mBinding;
    private Uri mSelectedImage;

    View.OnClickListener mListenerPickImage = view -> {
        checkPermissionsAndStart();
    };

//    https://medium.com/google-developer-experts/exploring-firebase-mlkit-on-android-face-detection-part-two-de7e307c52e0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_process_image);
        mBinding.fabPickImage.setOnClickListener(mListenerPickImage);
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
        startActivityForResult(intent, PROCESS_FACES);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROCESS_FACES && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            mSelectedImage = data.getData();
            UtilsImageLoader.loadImage(this, mBinding.ivHeaderImage, mSelectedImage);
            processImageML(mSelectedImage, PROCESS_BARCODE);
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

            case PROCESS_OCR:
                processTextOCR(selectedImage);
                break;

            case PROCESS_IMAGE_LABELING:
                processImageLabeling(selectedImage);
                break;

            case PROCESS_BARCODE:
                processBarcodeImageML(selectedImage);

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
                        for (FirebaseVisionLabel label : firebaseVisionLabels) {
                            Log.d(TAG, "onSuccess: "
                                    + label.getConfidence() + " "
                                    + label.getEntityId() + " "
                                    + label.getLabel());

                        }
                    }).addOnFailureListener(e -> e.printStackTrace());
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
                    .getVisionCloudTextDetector(configureCloudOCR())
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionCloudText ->
                            Log.d(TAG, "onSuccess: " + firebaseVisionCloudText.getText()))
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //https://firebase.google.com/docs/ml-kit/android/detect-faces
    private void processFaceImageML(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);

            FirebaseVision
                    .getInstance()
                    .getVisionFaceDetector(configureFaceML())
                    .detectInImage(image)
                    .addOnSuccessListener(firebaseVisionFaces -> {
                        if (firebaseVisionFaces.size() > 0) {
                            for (FirebaseVisionFace face :
                                    firebaseVisionFaces) {
                            }
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //https://firebase.google.com/docs/ml-kit/android/detect-faces
    private void processBarcodeImageML(Uri uri) {

        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, uri);

            FirebaseVision
                    .getInstance()
                    .getVisionBarcodeDetector()
                    .detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                            if (barcodes.size() > 0) {
                                for (FirebaseVisionBarcode barcode : barcodes) {
                                    Log.d(TAG, "onSuccess: " + barcode.getRawValue());
                                    switch (barcode.getValueType()) {
                                        case FirebaseVisionBarcode.TYPE_PHONE:
                                            Log.d(TAG, "phone: " + barcode.getPhone());
                                            break;
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //https://firebase.google.com/docs/ml-kit/android/detect-faces
    private void processLandmarksImageML(Uri uri) {
//
//        FirebaseVisionImage image = null;
//        try {
//            image = FirebaseVisionImage.fromFilePath(this, uri);
//
//            FirebaseVision
//                    .getInstance()
//                    .get()
//                    .detectInImage(image)
//                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
//                        @Override
//                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
//                            if (barcodes.size() > 0) {
//                                for (FirebaseVisionBarcode barcode : barcodes) {
//                                    Log.d(TAG, "onSuccess: " + barcode.getRawValue());
//                                    switch (barcode.getValueType()){
//                                        case FirebaseVisionBarcode.TYPE_PHONE:
//                                            Log.d(TAG, "phone: " + barcode.getPhone());
//                                            break;
//                                    }
//                                }
//                            }
//                        }
//                    })
//                    .addOnFailureListener(e -> e.printStackTrace());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    // TODO: 7/27/18 Process Clould: OCR
    // TODO: 7/27/18 Process Clould: Labeling
    // TODO: 7/27/18 Process Clould: Face Detection
    // TODO: 7/27/18 Process Clould: Landmarks
    // TODO: 7/27/18 Process Clould: Barcode

}
