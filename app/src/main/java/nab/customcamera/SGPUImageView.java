package nab.customcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by Abner on 15/3/9.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class SGPUImageView extends GPUImageView {

    public SGPUImageView(Context context) {
        super(context);
    }

    public SGPUImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
