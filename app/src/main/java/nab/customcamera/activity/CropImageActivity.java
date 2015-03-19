package nab.customcamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import nab.customcamera.PinchableImageView;
import nab.customcamera.R;
import nab.customcamera.SquareImageView;
import nab.customcamera.utils.DensityUtils;
import nab.customcamera.utils.FileUtils;
import nab.customcamera.utils.ImageUtils;

/**
 * Created by Abner on 15/3/16.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class CropImageActivity extends Activity implements View.OnClickListener{

    private final String TAG = "CropImageActivity";

    private final static String IMAGE_PATH = "image_path";

    private PinchableImageView pinchableImageView;

    private Bitmap bitmap;
    
    
    private TextView tv_next_step;
    private ImageView iv_roate;
    private ImageView iv_lock_img;
    private SquareImageView squareImageView;
    private View rl_root;
    
    //锁定图片
    private boolean isLocked = false;
    //背景色
    private boolean isWhite = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        initView();

    }

    public void initView() {
        pinchableImageView = (PinchableImageView) findViewById(R.id.iv_display);
        pinchableImageView.setCropHeight(calTopDistance());
        String path = getIntent().getStringExtra(IMAGE_PATH);
        bitmap = BitmapFactory.decodeFile(path);
        processBitmap(bitmap,pinchableImageView);
        iv_roate = (ImageView)findViewById(R.id.iv_roate);
        iv_lock_img = (ImageView)findViewById(R.id.iv_lock_img);
        iv_roate.setOnClickListener(this);
        iv_lock_img.setOnClickListener(this);
        squareImageView = (SquareImageView)findViewById(R.id.iv_trans);
        squareImageView.setImageBitmap(bitmap);
        squareImageView.setVisibility(View.INVISIBLE);
        squareImageView.setOnClickListener(this);
        tv_next_step = (TextView)findViewById(R.id.tv_next_step);
        tv_next_step.setOnClickListener(this);
        rl_root = findViewById(R.id.rl_root);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_roate:
                bitmap = ImageUtils.rotateBitmap(90,bitmap);
                processBitmap(bitmap,pinchableImageView);
                squareImageView.setImageBitmap(bitmap);
                break;
            case R.id.iv_lock_img:
                if(isLocked){
                    squareImageView.setVisibility(View.INVISIBLE);
                    pinchableImageView.setVisibility(View.VISIBLE);
                    Toast.makeText(this,"已解锁图片,拖动试试看",Toast.LENGTH_SHORT).show();
                }else{
                    squareImageView.setVisibility(View.VISIBLE);
                    pinchableImageView.setVisibility(View.INVISIBLE);
                    Toast.makeText(this,"当前已锁定图片,不能拖动哦",Toast.LENGTH_SHORT).show();
                }
                isLocked = !isLocked;
                break;
            case R.id.tv_next_step:
                cropBitmapFromScreen();
                break;
            case R.id.iv_trans:
                if(squareImageView.getVisibility() == View.VISIBLE){
                    if(isWhite) {
                        squareImageView.setBackgroundColor(getResources().getColor(android.R.color.black));
                    }else{
                        squareImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                    }
                    isWhite = !isWhite;
                }
                break;
        }
    }

    /**
     * 计算裁剪位置离顶部的距离，下面和上面是一样的，对称
     */
    private int calTopDistance() {
        int screenW = DensityUtils.getScreenWidth(this);
        int screenH = DensityUtils.getScreenHeight(this);
        return (screenH - screenW) / 2;
    }

    /**
     * 如果图片比框还小 就缩放到框的高或者宽
     */
    private void processBitmap(Bitmap bitmap,ImageView view) {

        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        //截图所用的框是正方形框 height == width
        int cropWidht = DensityUtils.getScreenWidth(this);

        if (bWidth < cropWidht && bHeight < cropWidht) {

            if (bWidth > bHeight) {
                //按宽缩放
                float scale = 1.0f * cropWidht / bWidth;
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, matrix, false);
                view.setImageBitmap(bitmap);
            } else {
                //height >= width 按高缩放
                float scale = 1.0f * cropWidht / bHeight;
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, matrix, false);
                view.setImageBitmap(bitmap);
            }
            
        }else{
            view.setImageBitmap(bitmap);
        }
    }
    /**
     * 截屏，然后根据高度截取正方形的图
     */
    private void cropBitmapFromScreen() {
        View decorview = this.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);

        decorview.buildDrawingCache();

        Bitmap bitmap = decorview.getDrawingCache();
        int width = DensityUtils.getScreenWidth(this);
        //+ DensityUtils.getBarHeight(this);
        int top = (DensityUtils.getScreenHeight(this) - width) / 2;
        bitmap = Bitmap.createBitmap(bitmap,0,top,width,width);
        Log.d("CameraActivity", "bitmapTemp.width : " + bitmap.getWidth()
                + " bitmapTemp.height : " + bitmap.getHeight());
        String path = FileUtils.saveBitmapToLocal(bitmap);

        Intent intent = new Intent(this, PictureFilterActivity.class);
        intent.putExtra("bitmapPath", path);
        startActivity(intent);

    }
}
