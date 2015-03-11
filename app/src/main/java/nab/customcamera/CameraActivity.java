/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nab.customcamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import nab.customcamera.utils.CameraHelper;
import nab.customcamera.utils.DensityUtils;
import nab.customcamera.utils.FileUtils;


public class CameraActivity extends Activity implements OnClickListener {
    private final String TAG = "CameraActivity";
    private GPUImage mGPUImage;
    private SquareGLSurfaceView squareGLSurfaceView;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    private View rl_top_bar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        initView();
    }

    public void initView() {
        rl_top_bar = findViewById(R.id.rl_top_bar);
        mGPUImage = new GPUImage(this);
        squareGLSurfaceView = (SquareGLSurfaceView) findViewById(R.id.surfaceView);
        mGPUImage.setGLSurfaceView(squareGLSurfaceView);
        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader();
        findViewById(R.id.iv_take_photo).setOnClickListener(this);
        View cameraSwitchView = findViewById(R.id.iv_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCameraHelper.hasFrontCamera() || !mCameraHelper.hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }
        squareGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //触摸对焦
                mCamera.mCameraInstance.autoFocus(autoFocusCallback);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.iv_take_photo:
                if (mCamera.mCameraInstance.getParameters().getFocusMode().equals(
                        Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    takePicture();
                } else {
                    mCamera.mCameraInstance.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(final boolean success, final Camera camera) {
                            if (success) {
                                takePicture();
                            } else {
                                Toast.makeText(CameraActivity.this, "对焦失败,请重新拍摄", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;

            case R.id.iv_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    /**
     * 找一个最佳的图片尺寸
     *
     * @param sizeList
     * @return
     */
    private Camera.Size getBestPictureSize(List<Camera.Size> sizeList) {
        Camera.Size size = null;
        // 优先找一个比例和屏幕一样的尺寸
        //由大到小排序
        Collections.sort(sizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                return size2.width - size.width;
            }
        });
        int screenW = DensityUtils.getScreenWidth(CameraActivity.this);
        int screenH = DensityUtils.getScreenHeight(CameraActivity.this);
        int compareWidth = 1200;
        int compareHeight = 1200;
        if (mCamera.getmPreViewSize() != null) {
            compareWidth = mCamera.getmPreViewSize().width;
            compareHeight = mCamera.getmPreViewSize().height;
        }
        final double ratio = 1.0 * Math.max(screenW, screenH) / Math.min(screenW, screenH);
        for (Camera.Size sz : sizeList) {
            Log.i(TAG, "SupportedPictureSizes: " + sz.width + "x" + sz.height + " ratio=" + (1.0 * sz.width / sz.height));
            if (1.0 * sz.width / sz.height == ratio) {
                if ((sz.width < compareWidth || sz.height < compareHeight) && size != null) {
                    //小于compareWidth的不再考虑
                    break;
                }
                size = sz;
            }
        }
        if (size == null) {///比例差
            double ratioAbs = 9999;
            //没找到一样的。。找一个接近的
            Log.i(TAG, "Could't found a SupportedPictureSize which ratio =" + ratio);
            for (Camera.Size sz : sizeList) {
                Log.i(TAG, "SupportedPictureSizes: " + sz.width + "x" + sz.height);
                if (Math.abs(sz.width / sz.height - ratio) < ratioAbs) {
                    if ((sz.width <= compareWidth || sz.height <= compareHeight) && size != null) {
                        //小于compareWidth的不再考虑
                        break;
                    }
                    size = sz;
                }
            }
        }
        return size;
    }

    private void takePicture() {
        // TODO get a size that is about the size of the screen
        Parameters params = mCamera.mCameraInstance.getParameters();
        params.setRotation(90);
        final Camera.Size size = getBestPictureSize(params.getSupportedPictureSizes());
        Log.i(TAG, "setPictureSize: " + size.width + "x" + size.height);
        params.setPictureSize(size.width, size.height);
        mCamera.mCameraInstance.setParameters(params);
        mCamera.mCameraInstance.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        int width = squareGLSurfaceView.getWidth();
                        int height = squareGLSurfaceView.getHeight();
//                        //由于width == height 是正方形 所以 photoX = Math.abs((size.height - size.width) / 2);
                        int photoX = Math.abs((size.height - (height * size.width / width)) / 2);
                        Bitmap bitmapTemp = null;
                        if (data != null) {
                            Matrix matrix = new Matrix();
                            if (mCamera.getmCurrentCameraId() == 1) {
                                //说明是前置的 要旋转270°
                                matrix.setRotate(270);
                            } else {
                                matrix.setRotate(90);
                            }
                            /**
                             * android 默认预览是0°，之前设置过相机为90°，拍完后照片是90°的是横着的
                             * size.width > size.height 实际上照片拍完后再旋转90° size.height是照片的宽
                             * 所以裁剪成正方形要以size.height为边 照片裁剪位置也是以X坐标开始
                             */
                            bitmapTemp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            bitmapTemp = Bitmap.createBitmap(bitmapTemp,
                                    0, 0, height, height, matrix, false);
                            Log.d("CameraActivity", "bitmapTemp.width : " + bitmapTemp.getWidth()
                                    + " bitmapTemp.height : " + bitmapTemp.getHeight());
                            String path = FileUtils.saveBitmapToLocal(bitmapTemp);
                            Intent intent = new Intent(CameraActivity.this, PictureFilterActivity.class);
                            intent.putExtra("bitmapPath", path);
                            startActivity(intent);
                            if (!bitmapTemp.isRecycled()) {
                                bitmapTemp.recycle();
                                bitmapTemp = null;
                            }
                        }
                    }
                });
    }


    private class CameraLoader {

        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;
        private Camera.Size mPreViewSize;

        public Camera.Size getmPreViewSize() {
            return mPreViewSize;
        }

        public int getmCurrentCameraId() {
            return mCurrentCameraId;
        }

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        /**
         * 找一个最佳的预览尺寸
         *
         * @param sizeList
         * @return
         */
        private Camera.Size getBestPreviewSize(List<Camera.Size> sizeList) {
            Camera.Size size = null;
            // 优先找一个比例和屏幕一样的尺寸
            //由大到小排序
            Collections.sort(sizeList, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size size, Camera.Size size2) {
                    return size2.width - size.width;
                }
            });
            int screenW = DensityUtils.getScreenWidth(CameraActivity.this);
            int screenH = DensityUtils.getScreenHeight(CameraActivity.this);
            final double ratio = 1.0 * Math.max(screenW, screenH) / Math.min(screenW, screenH);
            for (Camera.Size sz : sizeList) {
                Log.i(TAG, "SupportedPreviewSizes: " + sz.width + "x" + sz.height + " ratio=" + (1.0 * sz.width / sz.height));
                if (1.0 * sz.width / sz.height == ratio) {
                    if (sz.width < 1000) {
                        //小于1000即可，不继续找
                        break;
                    }
                    size = sz;
                }
            }
            if (size == null) {
                //比例差
                double ratioAbs = 9999;
                //没找到一样的。。找一个接近的
                Log.i(TAG, "Could't found a SupportedPreviewSize which ratio =" + ratio);
                for (Camera.Size sz : sizeList) {
                    Log.i(TAG, "SupportedPreviewSizes: " + sz.width + "x" + sz.height);
                    if (Math.abs(sz.width / sz.height - ratio) < ratioAbs) {
                        if (sz.width < 1000) {
                            //小于1000即可，不继续找
                            break;
                        }
                        size = sz;
                    }
                }
            }
            return size;
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            int bestW = 0;
            int bestH = 0;
            Camera.Size size = getBestPreviewSize(parameters.getSupportedPreviewSizes());
            if (size != null) {
                mPreViewSize = size;
                bestW = size.width;
                bestH = size.height;
            }
            Log.i(TAG, "setPreviewSize: " + bestW + "x" + bestH);
            parameters.setPreviewSize(bestW, bestH);

            if (parameters.getSupportedFocusModes().contains(
                    Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (parameters.getSupportedFocusModes().contains(
                    Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }
            mCameraInstance.setParameters(parameters);
            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    CameraActivity.this, mCurrentCameraId);
            CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /**
         * A safe way to get an instance of the Camera object.
         */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d(TAG, "auto success");
            mCamera.mCameraInstance.cancelAutoFocus();
        }
    };
}
