package com.example.tobinornottobin2.ObjectDetection.ObjectDetection;
/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;
import android.support.v4.app.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
//import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.*;
import android.hardware.camera2.CameraCaptureSession;

import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.tobinornottobin2.R;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;
import static com.example.tobinornottobin2.ObjectDetection.ObjectDetection.CameraConnectionFragment.chooseOptimalSize;
import static com.example.tobinornottobin2.ObjectDetection.ObjectDetection.ImageUtils.getYUVByteSize;


@SuppressLint("ValidFragment")
public class LegacycameraConnectionFragment extends Fragment {
    private static final Logger LOGGER = new Logger();
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append( Surface.ROTATION_0, 90 );
        ORIENTATIONS.append( Surface.ROTATION_90, 0 );
        ORIENTATIONS.append( Surface.ROTATION_180, 270 );
        ORIENTATIONS.append( Surface.ROTATION_270, 180 );
    }

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String cameraId;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler backgroundHandler;
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final CameraDevice cd) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    camera = cd;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(final CameraDevice cd) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    camera = null;
                }

                @Override
                public void onError(final CameraDevice cd, final int error) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    camera = null;
                    final Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final TotalCaptureResult result) {
                }
            };
    private CameraDevice camera;
    private CameraManager manager;
    private ImageReader.OnImageAvailableListener imageListener;
    /**
     * An {@link ImageReader} that handles preview frame capture.
     */
    private ImageReader previewReader;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession captureSession;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore cameraOpenCloseLock = new Semaphore( 1 );
    /**
     * The {@link Size} of camera preview.
     */
    private Size previewSize;
    private Size desiredSize;
    /**
     * The input size in pixels desired by TensorFlow (width and height of a square bitmap).
     */
    private final Size inputSize;
    /**
     /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder previewRequestBuilder;
    /**
     * The layout identifier to inflate for this Fragment.
     */
    private int layout;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView textureView;
    private SurfaceTexture availableSurfaceTexture = null;
    /**
     * {@link CaptureRequest} generated by {@link #previewRequestBuilder}
     */
    private CaptureRequest previewRequest;

    public final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    Bitmap bitmap = fromByteBuffer( buffer );
                    image.close();
                }
            } catch (Exception e) {
                Log.w( TAG, e.getMessage() );
            }
        }
    };

    Bitmap fromByteBuffer(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get( bytes, 0, bytes.length );
        return BitmapFactory.decodeByteArray( bytes, 0, bytes.length );
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
     * TextureView}.
     */
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {
                    availableSurfaceTexture = texture;
                    Object builder = null;
                    try {
                        startCamera( (CaptureRequest.Builder) builder);
                    } catch (CameraAccessException exception) {
                        exception.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;
    private Object Handler;
    private Object CameraManager;


    public LegacycameraConnectionFragment(
             final ImageReader.OnImageAvailableListener imageListener, final int layout, final Size desiredSize, Size inputSize) throws CameraAccessException {
        this.manager = manager;
        this.imageListener = imageListener;
        this.layout = layout;
        this.desiredSize = desiredSize;
        this.inputSize = inputSize;
    }

    @Override
    public View onCreateView(
            final LayoutInflater inflater, final ViewGroup containerCamera, final Bundle savedInstanceState) {
        return inflater.inflate( layout, containerCamera, false );
    }

    ;

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        textureView = (AutoFitTextureView) view.findViewById( R.id.texture );
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated( savedInstanceState );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).

        if (textureView.isAvailable()) {

            try {
                startCamera( previewRequestBuilder );
            } catch (CameraAccessException exception) {
                exception.printStackTrace();
            }

        } else {
            textureView.setSurfaceTextureListener( surfaceTextureListener );
        }
    }

    @Override
    public void onPause() {
        stopCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread( "CameraBackground" );
        backgroundThread.start();
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
        } catch (final InterruptedException e) {
            LOGGER.e( e, "Exception!" );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startCamera(CaptureRequest.Builder builder) throws CameraAccessException {
        builder.set( CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO );

        // String[] index = manager.getCameraIdList();
        int index2 = getCameraId();

        if (ActivityCompat.checkSelfPermission( getContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.openCamera( cameraId, stateCallback, backgroundHandler );

        //camera.openCamera(index, this, backgroundThread);
        //camera = Camera.open(index);
        try {
            final Activity activity = getActivity();
            if (null == activity || null == camera) {
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(previewReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //        // Orientation
//        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

            };
            // Camera api depreacted for camera2 api
//              try {
//            Camera.Parameters parameters = camera.getParameters();
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            if (focusModes != null
//                    && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            }
//            List<Camera.Size> cameraSizes = parameters.getSupportedPreviewSizes();
//            Size[] sizes = new Size[cameraSizes.size()];
//            int i = 0
//            for (Camera.Size size : cameraSizes) {
//                sizes[i++] = new Size(size.width, size.height);
//            }
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(String.valueOf(index2));
            final StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          Size  previewSize =
                    CameraConnectionFragment.chooseOptimalSize(
                            map.getOutputSizes(SurfaceTexture.class),
                            desiredSize.getWidth(),
                            desiredSize.getHeight());
                            //inputSize.getHeight());
//            Size previewSize =
//                    chooseOptimalSize(
//                            choices, desiredSize.getWidth(), desiredSize.getHeight());

            setPreviewSize();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);

            // camera.setParameters(parameters);
            //camera.setPreviewTexture(availableSurfaceTexture);
            textureView.setSurfaceTextureListener(surfaceTextureListener);

}
        catch( CameraAccessException exception )
        {

            final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {


                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        if (image != null) {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            Bitmap bitmap = fromByteBuffer(buffer);
                            image.close();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage());
                    }
                }
            };

           // camera.setPreviewCallbackWithBuffer(imageListener);
            Size s = setPreviewSize(); int height, width;
            //camera.callbackBuffer(new byte [getYUVByteSize(s.getHeight(),s.getWidth())]);
            final byte[] imageReader = new byte[getYUVByteSize(s.getHeight(), s.getWidth())];
            //manager.release();

            textureView.setAspectRatio(s.getHeight(), s.getWidth());
        }
        startPreview();
        
    }

        private Size setPreviewSize() {
            return null;
        }

    protected void startPreview() {
      
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(desiredSize.getWidth(), desiredSize.getHeight());
            Surface surface = new Surface(texture);

            CaptureRequest.Builder captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(surface);
            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                private Object CameraCaptureSession;

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == camera) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    CameraCaptureSession = cameraCaptureSession;

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    
                }

            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void stopCamera() {
        try {

            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != camera) {
                camera.close();
                camera = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private int getCameraId() throws CameraAccessException {
        String[] index = manager.getCameraIdList();
        CameraCharacteristics ci = null;
        for (int i = 0; i < index.length; i++) {
           // camera.getCameraInfo(i, ci);
            if (ci.CONTROL_AE_MODE_ON == CameraCharacteristics.LENS_FACING_BACK) return i;
        }
        return -1; // No camera found
    }
    /** Creates a new {@link CameraCaptureSession} for camera preview. */
    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            final Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            LOGGER.i("Opening camera preview: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            // Create the reader for the preview frames.
            previewReader =
                    ImageReader.newInstance(
                            previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);

            previewReader.setOnImageAvailableListener(imageListener, backgroundHandler);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            // Here, we create a CameraCaptureSession for camera preview.
            camera.createCaptureSession(
                    Arrays.asList(surface, previewReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == camera) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (final CameraAccessException e) {
                                LOGGER.e(e, "Exception!");
                            }
                        }

                        @Override
                        public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);
        } catch (final CameraAccessException e) {
            LOGGER.e(e, "Exception!");
        }
    }
    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}

