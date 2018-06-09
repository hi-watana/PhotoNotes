package jp.ac.titech.itpro.sdl.phototaker;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.LinkedList;

public class ZoomableImageView extends ImageView {
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private final float SCALE_MAX = 3.0f;
    private final float SCALE_MIN = 0.3f;
    private final float PINCH_SENSITIVITY = 2.0f;

    private float previousScaleFactor = -1.0f;

    public ZoomableImageView(Context context) {
        super(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        scaleGestureDetector = new ScaleGestureDetector(context, simpleOnScaleGestureListener);
        gestureDetector = new GestureDetector(context,simpleOnGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setImageMatrix(matrix);
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private ScaleGestureDetector.SimpleOnScaleGestureListener simpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        float focusX;
        float focusY;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float previousScale = getMatrixValue(Matrix.MSCALE_Y);

            Log.d("debug", new Float(previousScale).toString());
            float prop = (previousScaleFactor < 0.0f) ? 1.0f : scaleFactor / previousScaleFactor;
            previousScaleFactor = scaleFactor;
            scaleFactor = 1 + (float) Math.log(prop) / (PINCH_SENSITIVITY);

            float scale = scaleFactor * previousScale;

            if (scale < SCALE_MIN) {
                return false;
            }

            if (scale > SCALE_MAX) {
                return false;
            }

            matrix.postScale(scaleFactor, scaleFactor, focusX,focusY);

            invalidate();

            return super.onScale(detector);

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            previousScaleFactor = -1.0f;
        }

    };

    private float getMatrixValue(int index) {
        if (matrix == null) {
            matrix = getImageMatrix();
        }

        float[] values = new float[9];
        matrix.getValues(values);

        float value = values[index];
        return value;
    }

    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener= new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            float imageViewWidth = getWidth();
            float imageViewHeight = getHeight();
            float imageWidth = getImageWidth();
            float imageHeight = getImageHeight();
            float leftSideX = getMatrixValue(Matrix.MTRANS_X);
            float rightSideX = leftSideX + imageWidth;
            float topY = getMatrixValue(Matrix.MTRANS_Y);
            float bottomY = topY + imageHeight;

            if (imageViewWidth >= imageWidth && imageViewHeight >= imageHeight) {
                return false;
            }
            float x = -distanceX;
            float y = -distanceY;

            if (imageViewWidth > imageWidth) {
                x = 0;
            } else {
                if (leftSideX >  0 && x > 0){
                    x = -leftSideX;
                } else if (rightSideX < imageViewWidth && x < 0) {
                    x = imageViewWidth - rightSideX;
                }
            }

            if (imageViewHeight > imageHeight) {
                y = 0;
            } else {
                if (topY > 0 && y > 0) {
                    y = -topY;

                } else if (bottomY < imageViewHeight && y < 0) {
                    y = imageViewHeight - bottomY ;
                }
            }
            matrix.postTranslate(x,y);
            invalidate();

            return super.onScroll(event1, event2, distanceX, distanceY);
        }
    };

    private float getImageWidth() {
        return (getDrawable().getIntrinsicWidth())*getMatrixValue(Matrix.MSCALE_X);
    }

    private float getImageHeight() {
        return (getDrawable().getIntrinsicHeight())*getMatrixValue(Matrix.MSCALE_Y);
    }

}
