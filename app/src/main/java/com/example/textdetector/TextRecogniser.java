package com.example.textdetector;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import static android.content.Context.CAMERA_SERVICE;

public class TextRecogniser {

    public FirebaseVisionImage imageFromByteArray(byte[] image,int rotation) {
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setHeight(360)
                .setWidth(480)
                .setRotation(rotation)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build();
        return FirebaseVisionImage.fromByteArray(image,metadata);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int getRotationCompensation(String cameraId, Activity activity, Context context)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("TR", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    public int getRotation(Activity activity, Context context) throws CameraAccessException {
        int rotation = getRotationCompensation(getCameraId((CameraManager) activity.getSystemService(CAMERA_SERVICE)), activity, context);


         return rotation;
    }
    String getCameraId(CameraManager cManager) throws CameraAccessException {
        for(final String cameraId : cManager.getCameraIdList()){
            CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
            int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
            if(cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
        }
        return null;
    }
}