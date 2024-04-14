package com.example.machine_learning_application;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions;

import java.io.ByteArrayOutputStream;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "subject_segmentation_channel"; // Renamed channel

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("segmentImage")) {
                                byte[] imageBytes = call.argument("imageBytes");
                                // Convert byte array to Bitmap
                                Bitmap inputImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                System.out.println(inputImage.getWidth());
                                System.out.println(inputImage.getHeight());
                                System.out.println(inputImage);
                                InputImage image = InputImage.fromBitmap(inputImage, 0);
                                System.out.println("This 2nd initial");
                                System.out.println(image);

                                // Implement BitmapCallback (not used anymore)
                                // BitmapCallback callback = new BitmapCallback() {
                                //     @Override
                                //     public void onBitmapReady(Bitmap bitmap2) {
                                //         // Not used anymore
                                //     }
                                //
                                //     @Override
                                //     public void onFailure(Exception e) {
                                //         result.error("SEGMENTATION_ERROR", "Subject segmentation failed.", e);
                                //     }
                                // };

                                segmentImage(image, result);

                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    private void segmentImage(InputImage image, MethodChannel.Result result) {
        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build();

        SubjectSegmenter segmenter = SubjectSegmentation.getClient(options);
        segmenter.process(image)
                .addOnSuccessListener(new OnSuccessListener<SubjectSegmentationResult>() {
                    @Override
                    public void onSuccess(SubjectSegmentationResult segmentationResult) {
                        Bitmap foregroundBitmap = segmentationResult.getForegroundBitmap();
                        System.out.println("This is the Bitmap@e95f1ce. convert it to byte array");

                        // Convert Bitmap to byte array
                        byte[] imageBytes = null;
                        try {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            foregroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            imageBytes = outputStream.toByteArray();
                        } catch (Exception e) {
                            e.printStackTrace();
                            result.error("BITMAP_CONVERSION_ERROR", "Failed to convert bitmap to byte array.", e);
                            return;
                        }

                        // Send byte array to Flutter
                        result.success(imageBytes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        result.error("SEGMENTATION_ERROR", "Subject segmentation failed.", e);
                    }
                });
    }

    // Removed unused interface BitmapCallback
}
