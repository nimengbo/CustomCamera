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

package nab.customcamera.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import nab.customcamera.GPUImageFilterTools;
import nab.customcamera.R;
import nab.customcamera.SGPUImageView;
import nab.customcamera.SquareImageView;


public class PictureFilterActivity extends Activity implements OnClickListener {
    SGPUImageView mGPUImageView;
    private GPUImageFilter mFilter;
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pic);
        String path = getIntent().getStringExtra("bitmapPath");
        mGPUImageView = (SGPUImageView)findViewById(R.id.iv_trans);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        mGPUImageView.setImage(bitmap);
        findViewById(R.id.tv_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.tv_filter:
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {

                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                        mGPUImageView.requestRender();
                    }
                });
                break;
        }
    }
    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
        }
    }
}
