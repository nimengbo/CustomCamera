/**
 *
 */
package nab.customcamera.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import nab.customcamera.R;


public class LoadingDialog extends Dialog {
    private Context mContext;

    /**
     * @param context
     */
    public LoadingDialog(Context context) {
        this(context, 0, 0);
    }

    /**
     * @param context
     */
    public LoadingDialog(Context context, int width, int height) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        mContext = context;
        initView();

    }

    private void initView() {
        setContentView(R.layout.view_loading_dialog);
        this.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                startAnimation();
            }
        });
    }

    /**
     * 设置对话框内容
     *
     * @param msg
     */
    public LoadingDialog setMessage(String msg) {
        TextView tv_message = (TextView) findViewById(R.id.tv_message);
        tv_message.setText(msg);
        return this;
    }

    private void startAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 开始动画
                final ImageView iv_logo = (ImageView) findViewById(R.id.iv_loading_logo);
                AnimationDrawable anim = (AnimationDrawable) iv_logo
                        .getDrawable();
                anim.setOneShot(false);
                anim.stop();
                anim.start();

//				
//				final int orginalHeight = iv_logo.getHeight();
//				final int orginalWidth = iv_logo.getWidth();
//				
//				
//				iv_logo.setScaleType(ScaleType.MATRIX);
//				Matrix mBaseMatrix = new Matrix();
//				mBaseMatrix.setScale(2, 2, 0, orginalHeight);
//				// mBaseMatrix.setTranslate(iv_logo.getWidth(),
//				// iv_logo.getHeight());
//
//				iv_logo.setImageMatrix(mBaseMatrix);


//				System.out.println("w="+orginalWidth+" h="+orginalHeight);
//				final LayoutParams lp =iv_logo.getLayoutParams();
//				ValueAnimator anim = ValueAnimator.ofInt(0, orginalHeight);
//				anim.addUpdateListener(new AnimatorUpdateListener() {
//					@Override
//					public void onAnimationUpdate(ValueAnimator value) {
//						lp.height = (Integer) value.getAnimatedValue();
//						System.out.println("w="+orginalWidth+" h="+lp.height);
//						iv_logo.setLayoutParams(lp);
//					}
//				});
//				anim.setDuration(5000);
//				anim.setRepeatMode(ValueAnimator.RESTART);
//				anim.setRepeatCount(10000);
//				anim.start();
            }
        }, 1000);
    }
}
