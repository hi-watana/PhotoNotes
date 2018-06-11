package jp.ac.titech.itpro.sdl.photonotes;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private final float PINCH_SENSITIVITY = 2.0f;

    private final float UNDEF = -1.0f;
    private final float PENDING = -2.0f;
    private float previousScaleFactor = UNDEF;
    private float originalImageWidth;
    private float originalImageHeight;

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
        setScaleType(ScaleType.FIT_CENTER);
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
            float minScale = Math.min(getWidth() / originalImageWidth, getHeight() / originalImageHeight);

            if (previousScaleFactor == UNDEF) {
                previousScaleFactor = scaleFactor;
                scaleFactor = minScale / previousScale;
            } else if (previousScaleFactor == PENDING) {
                previousScaleFactor = scaleFactor;
                scaleFactor = (previousScale > minScale) ? 1.0f : minScale / previousScale;
            } else {
                float prop = scaleFactor / previousScaleFactor;
                previousScaleFactor = scaleFactor;
                scaleFactor = 1 + (float) Math.log(prop) / (PINCH_SENSITIVITY);
            }

            float scale = scaleFactor * previousScale;

            if (scale < minScale) {
                // Do nothing.
                return false;
            }

            if (scale > SCALE_MAX) {
                // Do nothing.
                return false;
            }

            matrix.postScale(scaleFactor, scaleFactor, focusX,focusY);

            // repaint
            invalidate();

            return super.onScale(detector);

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setScaleType(ScaleType.MATRIX);
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            previousScaleFactor = PENDING;
            float minScale = Math.min(getWidth() / originalImageWidth, getHeight() / originalImageHeight);
            float previousScale = getMatrixValue(Matrix.MSCALE_Y);

            float epsilon = minScale / 50;
            // |previousScale - minScale| < epsilon (previousScale ~ minScale)
            if (previousScale < minScale + epsilon) {
                setScaleType(ScaleType.FIT_CENTER);
            }
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
            // repaint
            invalidate();

            return super.onScroll(event1, event2, distanceX, distanceY);
        }
    };

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        originalImageWidth = getImageWidth();
        originalImageHeight = getImageHeight();
    }

    private float getImageWidth() {
        Drawable drawable = getDrawable();
        if (drawable == null) return 0.0f;
        return (drawable.getIntrinsicWidth())*getMatrixValue(Matrix.MSCALE_X);
    }

    private float getImageHeight() {
        Drawable drawable = getDrawable();
        if (drawable == null) return 0.0f;
        return (drawable.getIntrinsicHeight())*getMatrixValue(Matrix.MSCALE_Y);
    }

}
