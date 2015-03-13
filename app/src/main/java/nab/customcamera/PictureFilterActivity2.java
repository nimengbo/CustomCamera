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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import nab.customcamera.utils.ImageUtils;


public class PictureFilterActivity2 extends Activity implements OnClickListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pic2);
        String path = getIntent().getStringExtra("bitmapPath");
        SquareImageView squareImageView = (SquareImageView)findViewById(R.id.iv_trans);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        squareImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        }
    }
}
