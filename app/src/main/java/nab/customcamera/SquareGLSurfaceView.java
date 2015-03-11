package nab.customcamera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by Abner on 15/3/9.
 * QQ 230877476
 * Email nimengbo@gmail.com
 */
public class SquareGLSurfaceView extends GLSurfaceView{

    public SquareGLSurfaceView(Context context) {
        super(context);
    }

    public SquareGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
