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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
    private GLSurfaceView squareGLSurfaceView;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    private View rl_top_bar;
    private View trasView;
    private ImageView displayImageView;
    /**
     * 图片尺寸
     */
    private int pictureWidth;
    private int pictureHeight;
    /**
     * 预览尺寸
     */
    private int preViewWidth;
    private int preViewHeight;
    private final int bestHeight = 1200; //比较清晰的分辨率
    private final int bestWidth = 1200;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        initView();
    }

    public void initView() {
        displayImageView = (ImageView) findViewById(R.id.iv_display);
        trasView = findViewById(R.id.iv_trans);
        rl_top_bar = findViewById(R.id.rl_top_bar);
        mGPUImage = new GPUImage(this);
        squareGLSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
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
     * 找到最好的尺寸
     *
     * @param preViewSizeList
     * @param pictureSizeList
     */
    public void getBestSize(List<Camera.Size> preViewSizeList, List<Camera.Size> pictureSizeList) {
        /**
         *  最好的方案是 根据屏幕比例 找到 图片尺寸
         *  再找到预览尺寸 使得 屏幕比 == 图片比 == 预览比 (这种机型太少) 
         *  以下是根据 宽最接近1200 找的尺寸
         */


        //由大到小排序
        Collections.sort(pictureSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                return size2.width - size.width;
            }
        });
        for (int i = pictureSizeList.size() - 1; i >= 0; i--) {
            if (i < 0) {
                break;
            }
            if (i == 0) {
                //说明没找到最接近的(相机分辨率太低)
                pictureHeight = pictureSizeList.get(0).height;
                pictureWidth = pictureSizeList.get(0).width;
                Log.d(TAG, "not find match pictuerSize");
            }
            //优先找一个和bestHeight最近接的pictureSize.height
            if (pictureSizeList.get(i - 1).height < bestHeight
                    && pictureSizeList.get(i).height >= bestHeight) {
                pictureHeight = pictureSizeList.get(i).height;
                pictureWidth = pictureSizeList.get(i).width;
                break;
            }
        }
        //由大到小排序
        Collections.sort(preViewSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                return size2.width - size.width;
            }
        });
        //相机分辨率比
        final float ratio = 1.0f * pictureHeight / pictureWidth;

        for (int i = 0; i < preViewSizeList.size(); i++) {
            if (i == preViewSizeList.size() - 1) {
                //说明没找到最接近的(手机分辨率太低)
                preViewHeight = preViewSizeList.get(0).height;
                preViewWidth = preViewSizeList.get(0).width;
                Log.d(TAG, "not find match preViewSize");
            }
            float ratioTemp = (1.0f * preViewSizeList.get(i).height / preViewSizeList.get(i).width);
            //比例相等，并且一定比图片尺寸小或等
            if (ratio == ratioTemp && preViewSizeList.get(i).height <= pictureHeight) {
                preViewHeight = preViewSizeList.get(i).height;
                preViewWidth = preViewSizeList.get(i).width;
                break;
            }
        }

    }

    private void takePicture() {
        // TODO get a size that is about the size of the screen
        Parameters params = mCamera.mCameraInstance.getParameters();
        params.setRotation(90);
        mCamera.mCameraInstance.setParameters(params);
        mCamera.mCameraInstance.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
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
                            Log.d("CameraActivity", "decodeByteArray bitmapTemp.width : " + bitmapTemp.getWidth()
                                    + " bitmapTemp.height : " + bitmapTemp.getHeight());
                            bitmapTemp = Bitmap.createBitmap(bitmapTemp, 0, 0, bitmapTemp.getWidth(), bitmapTemp.getHeight(), matrix, false);
                            ImageView displayImageView = (ImageView) findViewById(R.id.iv_display);
                            displayImageView.setImageBitmap(bitmapTemp);
                            cropBitmapFromScreen();
                        }
                    }
                });
    }

    private void cropBitmapFromScreen() {
        View decorview = this.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);

        decorview.buildDrawingCache();

        Bitmap bitmap = decorview.getDrawingCache();
        int width = DensityUtils.getScreenWidth(this);
        int top = rl_top_bar.getHeight() + DensityUtils.getBarHeight(this);
        bitmap = Bitmap.createBitmap(bitmap,0,top,width,width);
        Log.d("CameraActivity", "bitmapTemp.width : " + bitmap.getWidth()
                + " bitmapTemp.height : " + bitmap.getHeight());
        String path = FileUtils.saveBitmapToLocal(bitmap);
        
        Intent intent = new Intent(CameraActivity.this, PictureFilterActivity.class);
        intent.putExtra("bitmapPath", path);
        startActivity(intent);

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
            displayImageView.setImageBitmap(null);
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }


        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            Log.i(TAG, "screenSize: " + DensityUtils.getScreenHeight(CameraActivity.this)
                    + "x" + DensityUtils.getScreenWidth(CameraActivity.this));
            int bestW = 0;
            int bestH = 0;
            getBestSize(parameters.getSupportedPreviewSizes(), parameters.getSupportedPictureSizes());

            Log.i(TAG, "setPreviewSize: " + preViewWidth + "x" + preViewHeight);
            Log.i(TAG, "setPictureSize: " + pictureWidth + "x" + pictureHeight);
            parameters.setPreviewSize(preViewWidth, preViewHeight);
            parameters.setPictureSize(pictureWidth, pictureHeight);

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
