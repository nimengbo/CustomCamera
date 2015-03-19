package nab.customcamera.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import nab.customcamera.R;

/**
 * Created by Abner on 15/3/19.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private final static int REQUEST_CODE_PICK_IMAGE = 101;
    private final static String IMAGE_PATH = "image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        findViewById(R.id.tv_open_camera).setOnClickListener(this);
        findViewById(R.id.tv_open_album).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_camera:
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_open_album:
                getImageFromAlbum();
                break;
        }
    }

    /**
     * 通过相册找到图
     */
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            String imagePath = null;
            ContentResolver localContentResolver = getContentResolver();
            Uri localUri = data.getData();
            String[] arrayOfString = {"_data"};
            Cursor localCursor = null;
            if (localUri != null) {
                localCursor = localContentResolver.query(localUri, arrayOfString, null, null, null);
            }
            if (localCursor != null) {
                int i = localCursor.getColumnIndex(arrayOfString[0]);
                if (localCursor.moveToFirst()) {
                    imagePath = localCursor.getString(i);
                }
                localCursor.close();
            }
            if (!TextUtils.isEmpty(imagePath)) {
                Intent intent = new Intent(this, CropImageActivity.class);
                intent.putExtra(IMAGE_PATH, imagePath);
                startActivity(intent);
            }
        }
    }
}
