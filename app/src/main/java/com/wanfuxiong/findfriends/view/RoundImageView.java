package com.wanfuxiong.findfriends.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.wanfuxiong.findfriends.R;

public class RoundImageView extends View {

    private Paint mPaint;
    private Bitmap mBitmap;
    private float mCircleRadio;

    public RoundImageView(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_round);// 暂时写死
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(getCircle(mBitmap), 0, 0, mPaint);
    }

    //将bitmap转换成圆形bitmap
    public Bitmap getCircle(Bitmap bitmap) {
        //圆形图片的半径
        float radius = (float) bitmap.getWidth() / 2;
        //创建一张新的bitmap，跟传入图片一样宽的正方形bitmap，
        Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        //初始化画布，并将刚才创建的bitmap给这画布，让画布画在这bitmap上面
        Canvas canvas = new Canvas(b);
        //初始化画笔
        Paint p = new Paint();
        //在画布中画一个等宽的圆
        canvas.drawCircle(radius, radius, radius, p);
        //设置画笔属性，让画笔只在哪圆圈中画画，关于画笔属性，可以百度一下，很多，但是非常有用
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, p);
        return b;
    }
}
