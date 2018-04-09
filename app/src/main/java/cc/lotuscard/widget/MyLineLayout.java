package cc.lotuscard.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import cc.lotuscard.identificationcardtest.R;

/**
 * Created by Endless on 2017/10/16.
 */

public class MyLineLayout extends LinearLayout {
    boolean b;
    public MyLineLayout(Context context) {
        super(context);
    }

    public MyLineLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyLineLayoutBg);
        b = a.getBoolean(R.styleable.MyLineLayoutBg_unMeasure, false);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyLineLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getState() != state) {
            b = false;
        }
        if (b) {
//            Paint paint = new Paint();
//            paint.setColor(Color.YELLOW);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.drawRect(new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()), paint);
            canvas.drawColor(Color.WHITE);
        }else {
            canvas.drawColor(Color.YELLOW);
        }

        super.onDraw(canvas);
    }

    private int state = MeasureStateEnum.UNMEASUED.ordinal();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
