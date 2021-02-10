
/* Copyright 2020 The TensorFlow Authors. All Rights Reserved.
        *
        * Licensed under the Apache License, Version 2.0 (the "License");
        * you may not use this file except in compliance with the License.
        * You may obtain a copy of the License at
        *
        *       http://www.apache.org/licenses/LICENSE-2.0
        *
        * Unless required by applicable law or agreed to in writing, software
        * distributed under the License is distributed on an "AS IS" BASIS,
        * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        * See the License for the specific language governing permissions and
        * limitations under the License.
        */

package com.example.tobinornottobin2.ObjectDetection.ObjectDetection;

//import static com.google.common.truth.Truth.assertThat;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Size;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.example.tobinornottobin2.ObjectDetection.ObjectDetection.Detector.Recognition;
import com.example.tobinornottobin2.lib_task_api.src.main.java.org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.security.AccessController.getContext;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/** Golden test for Object Detection Reference app. */
@RunWith(AndroidJUnit4.class)
public class ScanService { //occurs whwn the scan button is cliked on the scan page
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int MODEL_INPUT_SIZE = 300;
    private static final boolean IS_MODEL_QUANTIZED = true;
    private static final String MODEL_FILE = "ssd_mibilenet_v1_1_metadata_1.tflite"; //file that hold the metadata downloaded from Tensorflow
    private static final String LABELS_FILE = "labelmap.txt"; //Lables for images is stored
    private static final Size IMAGE_SIZE = new Size(640, 480);  // used to calcuaate the height of the image scanned

    private Detector detector;
    private Bitmap croppedBitmap;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    @Before
    public void setUp() throws IOException { // method to begin the detection of the object you need to set up
        detector =
                TFLiteObjectDetectionAPIModel.create(   //calling the detection API and creating a a variable for each of the following to store the information gathered from the scan/images
                        InstrumentationRegistry.getInstrumentation().getContext(),
                        MODEL_FILE,
                        LABELS_FILE,
                        MODEL_INPUT_SIZE,
                        IS_MODEL_QUANTIZED);
        int cropSize = MODEL_INPUT_SIZE;
        int previewWidth = IMAGE_SIZE.getWidth();
        int previewHeight = IMAGE_SIZE.getHeight();
        int sensorOrientation = 0;
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888); //create a bitmap of the image in bits

        frameToCropTransform =  //calling the imageutils class to return a matrix of the source
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, false);
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }




    @Test // test to see if the scan can recongise the object from the images provided.
    public void detectionResultsShouldNotChange() throws Exception {
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(loadImage("table.jpg"), frameToCropTransform, null);
        final List<Recognition> results = detector.recognizeImage(croppedBitmap);
        final List<Recognition> expected = loadRecognitions("table_results.txt");

        for (Recognition target : expected) {
            // Find a matching result in results
            boolean matched = false;
            for (Recognition item : results) {
                RectF bbox = new RectF();
                cropToFrameTransform.mapRect(bbox, item.getLocation());
                if (item.getTitle().equals(target.getTitle())
                        && matchBoundingBoxes(bbox, target.getLocation())
                        && matchConfidence(item.getConfidence(), target.getConfidence())) {
                    matched = true;
                    break;
                }
            }
            assertTrue(matched);
        }

    }

    // Confidence tolerance: absolute 1%
    private static boolean matchConfidence(float a, float b) {
        return abs(a - b) < 0.01;
    }

    // Bounding Box tolerance: overlapped area > 95% of each one
    private static boolean matchBoundingBoxes(RectF a, RectF b) {
        float areaA = a.width() * a.height();
        float areaB = b.width() * b.height();

        RectF overlapped =
                new RectF(
                        max(a.left, b.left), max(a.top, b.top), min(a.right, b.right), min(a.bottom, b.bottom));
        float overlappedArea = overlapped.width() * overlapped.height();
        return overlappedArea > 0.95 * areaA && overlappedArea > 0.95 * areaB;
    }

    private static Bitmap loadImage(String fileName) throws Exception {
        AssetManager assetManager =
                InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        InputStream inputStream = assetManager.open(fileName);
        return BitmapFactory.decodeStream(inputStream);
    }

    // The format of result:
    // category bbox.left bbox.top bbox.right bbox.bottom confidence
    // ...
    // Example:
    // Apple 99 25 30 75 80 0.99
    // Banana 25 90 75 200 0.98
    // ...
    private static List<Recognition> loadRecognitions(String fileName) throws Exception {
        AssetManager assetManager =
                InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        InputStream inputStream = assetManager.open(fileName);
        Scanner scanner = new Scanner(inputStream);
        List<Recognition> result = new ArrayList<>();
        while (scanner.hasNext()) {
            String category = scanner.next();
            category = category.replace('_', ' ');
            if (!scanner.hasNextFloat()) {
                break;
            }
            float left = scanner.nextFloat();
            float top = scanner.nextFloat();
            float right = scanner.nextFloat();
            float bottom = scanner.nextFloat();
            RectF boundingBox = new RectF(left, top, right, bottom);
            float confidence = scanner.nextFloat();
            Recognition recognition = new Recognition(null, category, confidence, boundingBox);
            result.add(recognition);
        }
        return result; // result is the condidence score of the scan, this is the % of accuaracy
    }
}
