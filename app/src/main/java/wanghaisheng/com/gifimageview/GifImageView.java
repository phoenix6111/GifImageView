package wanghaisheng.com.gifimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.apkfuns.logutils.LogUtils;

import java.io.InputStream;

/**
 * Created by sheng on 2016/8/3.
 * 支持播放gif动画的ImageView
 */
public class GifImageView extends ImageView implements View.OnClickListener {

    //动画播放的关键类
    private Movie mMovie;

    //是否自动播放
    private boolean isAutoPlay;
    //是否正在播放
    private boolean isPlaying;
    //点击开始播放的按钮
    private Bitmap mPlayBtn;

    //动画图片的宽度
    private int mImageWidth;
    //动画图片的高度
    private int mImageHeight;

    //记录动画开始时间
    private long mMovieStartTime;

    public GifImageView(Context context) {
        super(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //关闭硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        LogUtils.d("GifImageView");

        //获取xml文件中配置的是否自动播放gif文件的属性值
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.GifImageView);
        isAutoPlay = typedArray.getBoolean(R.styleable.GifImageView_auto_play,false);
        typedArray.recycle();

        int resId = getResourceId(attrs);
        LogUtils.d("res id "+resId);
        if(resId != 0) {
            //获取资源对应的流
            InputStream in = context.getResources().openRawResource(resId);
            //初始化Movie
            mMovie = Movie.decodeStream(in);
            if(mMovie != null) {
                //初始化gif图片的宽度和高度
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                bitmap.recycle();

                if(!isAutoPlay) {
                    //初始化点击播放的按钮
                    mPlayBtn = BitmapFactory.decodeResource(getResources(),R.drawable.icon_play);
                    setOnClickListener(this);
                }
            }
        }
    }

    /**
     * 读取image的src
     * @param attrs
     * @return
     */
    private int getResourceId(AttributeSet attrs) {

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            if (attrs.getAttributeName(i).equals("src")) {
                Log.d("TAG",attrs.getAttributeResourceValue(i, 0) + "=========");
                return attrs.getAttributeResourceValue(i, 0);
            }
        }

        return 0;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == getId()) {
            //当用户点击图片时，开始播放GIF动画
            isPlaying = true;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mMovie != null) {
            //如果是GIF图片则重写设定PowerImageView的大小
            setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
        }
    }

    /**
     * 开始播放GIF动画，播放完成返回true，未完成返回false。
     * @param canvas
     */
    private boolean playGif(Canvas canvas) {
        long now = SystemClock.uptimeMillis();
        if (mMovieStartTime == 0) {
            mMovieStartTime = now;
        }
        int duration = mMovie.duration();
        if (duration == 0) {
            duration = 1000;
        }
        int relTime = (int) ((now - mMovieStartTime) % duration);
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        if ((now - mMovieStartTime) >= duration) {
            mMovieStartTime = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        LogUtils.d("ondraw  "+ mMovie);

        if(mMovie == null) {
            // mMovie等于null，说明是张普通的图片，则直接调用父类的onDraw()方法
            super.onDraw(canvas);
            return ;
        }

        // mMovie不等于null，说明是张GIF图片
        if (isAutoPlay) {
            // 如果允许自动播放，就调用playMovie()方法播放GIF动画
            playGif(canvas);
            invalidate();
        } else {
            // 不允许自动播放时，判断当前图片是否正在播放
            if(isPlaying) {
                if(playGif(canvas)) {
                    isAutoPlay = false;
                }
                invalidate();
            } else {
                LogUtils.d("first playing ");
                // 还没开始播放就只绘制GIF图片的第一帧，并绘制一个开始按钮
                mMovie.setTime(0);
                mMovie.draw(canvas,0,0);

                //draw playbtn
                int offsetX = mImageWidth/2 - mPlayBtn.getWidth()/2;
                int offsetY = mImageHeight/2 - mPlayBtn.getHeight()/2;
                LogUtils.d("offsetX "+offsetX+" offsetY "+offsetY);
                canvas.drawBitmap(mPlayBtn,offsetX,offsetY,null);
            }
        }


    }
}
