package com.example.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class SketchView extends AppCompatImageView implements OnTouchListener {

    // 画图板默认参数
    public static final int STROKE = 0;
    public static final int ERASER = 1;
    public static final int DEFAULT_STROKE_SIZE = 7;
    public static final int DEFAULT_ERASER_SIZE = 50;
    private float strokeSize = DEFAULT_STROKE_SIZE;
    // 默认初始化画笔颜色
    private int strokeColor = Color.BLACK;
    private float eraserSize = DEFAULT_ERASER_SIZE;

    private Path m_Path;
    private Paint m_Paint;
    private float mX, mY;
    private int width, height;
    private int clickId ;
//
//    public void arc(int clickid) {
//        this.clickid = clickid;
//        Log.e(TAG, "arc 方法");
//    }
//
//    public void rectangle(int clickid) {
//        this.clickid = clickid;
//        Log.e(TAG, "rectangle 方法");
//    }
//
//    public void ellipse(int clickid) {
//        this.clickid = clickid;
//        Log.e(TAG, "ellipse 方法");
//    }

    // 保存画图轨迹，用来做撤销和重做
    private ArrayList<Pair<Path, Paint>> paths = new ArrayList<>();
    private ArrayList<Pair<Path, Paint>> undonePaths = new ArrayList<>();
    // 记录选择的图片，方便后续保存操作
    private Bitmap bitmap;
    // 默认初始化的mode
    private int mode = STROKE;

    private OnDrawChangedListener onDrawChangedListener;

    public SketchView(Context context, AttributeSet attr) {
        super(context, attr);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);

        this.setOnTouchListener(this);
        // 初始化paint
        m_Paint = new Paint();
        m_Paint.setAntiAlias(true);
        m_Paint.setDither(true);
        m_Paint.setColor(strokeColor);
        m_Paint.setStyle(Paint.Style.STROKE);
        m_Paint.setStrokeJoin(Paint.Join.ROUND);
        m_Paint.setStrokeCap(Paint.Cap.ROUND);
        m_Paint.setStrokeWidth(strokeSize);
        m_Path = new Path();

        invalidate();
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        if (mode == STROKE || mode == ERASER)
            this.mode = mode;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                touch_start(x, y);
                // 清除undone list
                undonePaths.clear();
                if (mode == ERASER) {
                    m_Paint.setColor(Color.WHITE);
                    m_Paint.setStrokeWidth(eraserSize);
                } else {
                    m_Paint.setColor(strokeColor);
                    m_Paint.setStrokeWidth(strokeSize);
                }
                // 复制m_Paint
                Paint newPaint = new Paint(m_Paint);
                // 避免啥都没有的时候调用橡皮擦在那里乱擦
                if (!(paths.size() == 0 && mode == ERASER && bitmap == null)) {
                    paths.add(new Pair<>(m_Path, newPaint));
                }
                m_Path.reset();
                m_Path.moveTo(x, y);
                mX = x;
                mY = y;
                invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
//                touch_move(x, y);
                switch (clickId) {
                    case 1:
                        Log.e(TAG, "1m line" );
                         touch_move( x,y );
                        break;
                    case 2:
                        Log.e(TAG, "2m arc" );
                        m_Path.reset();
                        m_Path.arcTo( mX,mY,x,y,0,270,false );

                        break;
                    case 3:
                        Log.e(TAG, "3m rect" );
                        m_Path.reset();
                        m_Path.addRect( mX,mY,x,y, Path.Direction.CW );
                        break;
                    case 4:
                        Log.e(TAG, "4m oval" );
                        m_Path.reset();
                        m_Path.addOval( mX,mY,x,y,Path.Direction.CW );
                        break;
                    case 5:
                        // 心形坐标系再右下方，所以只能从左上到右下画
                        Log.e(TAG, "5m heart" );
                        m_Path.reset();
                        m_Path.arcTo( mX,mY,(x + mX) / 2,y,-180,180 ,false);
//                        m_Path.moveTo( (x + mX) / 2 ,(y + mY)/ 2 );
                        m_Path.arcTo( (mX + x) / 2,mY,x,y,-180,180 ,false );
//                        m_Path.lineTo( (mX + x ) / 2, (3 * y / 2 - mY / 2) );
//                        m_Path.lineTo( mX,(mY + y) / 2 );
                        m_Path.quadTo( x,y,(mX + x) / 2,(3 * y / 2 - mY / 2) );
                        m_Path.quadTo( mX ,y,mX,(mY + y) / 2);
                        m_Path.close();
                        break;

                    default:
                        Log.e(TAG, "1m line" );
                        touch_move( x,y );

                        break;
                }
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        for (Pair<Path, Paint> p : paths) {
            canvas.drawPath(p.first, p.second);
        }
        onDrawChangedListener.onDrawChanged();
    }
    public void line(int clickid) {
        this.clickId = clickid;
    }
    public void arc(int clickid) {
        this.clickId = clickid;
    }
    public void rect(int clickid) {
        this.clickId = clickid;
    }
    public void oval(int clickid) {
        this.clickId = clickid;
    }
    public void heart(int clickid) {this.clickId = clickid;}
    private void touch_start(float x, float y) {
        // 清除undone list
        if (mode == ERASER) {
            m_Paint.setColor(Color.WHITE);
            m_Paint.setStrokeWidth(eraserSize);
        } else {
            m_Paint.setColor(strokeColor);
            m_Paint.setStrokeWidth(strokeSize);
        }
        // 复制m_Paint
        Paint newPaint = new Paint(m_Paint);
        // 避免啥都没有的时候调用橡皮擦在那里乱擦
        if (!(paths.size() == 0 && mode == ERASER && bitmap == null)) {
            paths.add(new Pair<>(m_Path, newPaint));
        }

        m_Path.reset();
        m_Path.moveTo(x, y);
        mX = x;
        mY = y;
    }

//    private void touch_move(float x, float y) {
//        switch (clickid) {
//
//            case 1:
//                // 弧线比较特殊 当 mPath 调用 close() 方法的时候，这个弧形会自动封闭
//                m_Path.reset();
//                m_Path.addArc(mX, mY, x, y, 0, 270);
//                clean();
//                break;
//            case 2:
//                m_Path.reset();
//                m_Path.addRect(mX, mY, x, y, Path.Direction.CW);
//                clean();
//                m_Path.close();
//                break;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void touch_move(float x, float y){
        m_Path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mX = x;
        mY = y;

    }

    private void touch_up() {
        if (clickId == 1){
            Log.e(TAG, "1up line" );
            m_Path.lineTo(mX, mY);
        }else if (clickId == 2){
            Log.e(TAG, "2up arc" );
        }else if (clickId == 3){
            Log.e(TAG, "3up rect" );
        }else if (clickId == 4){
            Log.e(TAG, "4up oval" );
        } else if (clickId == 5){
            Log.e(TAG, "5up heart" );
        }


        // 复制m_Paint
        Paint newPaint = new Paint(m_Paint);
        // 避免啥都没有的时候调用橡皮擦在那里乱擦
        if (!(paths.size() == 0 && mode == ERASER && bitmap == null)) {
            paths.add(new Pair<>(m_Path, newPaint));
        }
        // 避免重复画两次
        m_Path = new Path();
    }

    // 返回画图结果用来保存
    public Bitmap getBitmap() {
        if (paths.size() == 0)
            return null;
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            // 底色设置成白色
            bitmap.eraseColor(Color.WHITE);
        }
        Canvas canvas = new Canvas(bitmap);
        for (Pair<Path, Paint> p : paths) {
            canvas.drawPath(p.first, p.second);
        }
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // 撤销一笔
    public void undo() {
        if (paths.size() >= 2) {
            undonePaths.add(paths.remove(paths.size() - 1));
            // 有两种动作，一种是down,一种是move，所以需要做两次
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        }
    }

    // 重做一笔
    public void redo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
    }

    public int getUndoneCount() {
        return undonePaths.size();
    }

    public ArrayList<Pair<Path, Paint>> getPaths() {
        return paths;
    }

    public void setSize(int size, int eraserOrStroke) {
        switch (eraserOrStroke) {
            case STROKE:
                strokeSize = size;
                break;
            case ERASER:
                eraserSize = size;
                break;
        }
    }

    public int getStrokeColor() {
        return this.strokeColor;
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
    }

    // 删除所有画图，包括选择的图片
    public void erase() {
        paths.clear();
        undonePaths.clear();
        // 先判断是否已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
        invalidate();
    }

    public void setOnDrawChangedListener(OnDrawChangedListener listener) {
        this.onDrawChangedListener = listener;
    }



    public interface OnDrawChangedListener {
        void onDrawChanged();
    }
}