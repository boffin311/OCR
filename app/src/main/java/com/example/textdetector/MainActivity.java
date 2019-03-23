package com.example.textdetector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnCapture, btnPreview;
    Camera camera;
    ImageView image;
    CameraKitView cameraKitView;
    TextView tvData;
    SurfaceView obtImage;
    public static final int REQUEST_CAMERA = 12345;
    public static final String TAG = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}
                , 123);
        btnPreview = findViewById(R.id.btnPreview);
        btnCapture = findViewById(R.id.btnCapture);
        tvData=findViewById(R.id.tvData);
        cameraKitView=findViewById(R.id.camera);
        obtImage = findViewById(R.id.obtImage);
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         //      startPreview();
            }
        });
        image = findViewById(R.id.image);
         cameraKitView.setPreviewListener(new CameraKitView.PreviewListener() {
             @Override
             public void onStart() {

             }

             @Override
             public void onStop() {

             }
         });


//        obtImage.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//                if (holder.getSurface().isValid()) {
//                    try {
//                        camera.stopPreview();
//                    } catch (Exception e) {
//                        Log.d(TAG, "surfaceChanged: " + " No preview to stop");
//                    }
//
//                    try {
//                        startPreview();
//                    } catch (Exception e) {
//                        Log.d(TAG, "surfaceChanged: " + " No preview to start");
//                    }
//
//
//                }
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//
//            }
//        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                camera.takePicture(null, null, new Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//                        if (data == null) return;
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                        Matrix matrix=new Matrix();
//                        matrix.postRotate(90);
//                        Bitmap rotatedImage=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true );
//                        image.setImageBitmap(rotatedImage);
//                        try {
//                          //  Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length);
//                            image.setImageBitmap(rotatedImage);
//                            textRecogniserCallback(rotatedImage);
//                        } catch (CameraAccessException e) {
//                            e.printStackTrace();
//                        }
//                      startPreview();
//                    }
//                });
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        try {
                            Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            textRecogniserCallback(bitmap);
                            image.setImageBitmap(bitmap);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
      }
       });
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                //  Log.d(TAG, "onActivityResult: " + "Hello"+data.g);
                //  Bitmap bitmap= (Bitmap) data.getExtras().get("data");
                // obtImage.setImageBitmap(bitmap);


            }
        }
    }


    void startPreview() {
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(obtImage.getHolder());
            switch (getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    camera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    camera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    camera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    camera.setDisplayOrientation(180);
                    break;
            }
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void textRecogniserCallback(Bitmap bitmap) throws CameraAccessException {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(bitmap);
      //  TextRecogniser textRecogniser = new TextRecogniser();
        //textRecogniser.imageFromByteArray(image, textRecogniser.getRotation(MainActivity.this, getBaseContext()))
        Task<FirebaseVisionText> task = detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String result = firebaseVisionText.getText();
                tvData.setText(result);
                Log.d("REAL", "onSuccess0: " + firebaseVisionText.getText());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
