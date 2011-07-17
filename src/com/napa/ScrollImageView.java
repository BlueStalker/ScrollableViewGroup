package com.napa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ScrollImageView extends View {
 private final int DEFAULT_PADDING = 10;
 private Display mDisplay;
 private Bitmap mImage;

 /* Current x and y of the touch */
 private float mCurrentX = 0;
 private float mCurrentY = 0;

 private float mTotalX = 0;
 private float mTotalY = 0;
 
 /* The touch distance change from the current touch */
 private float mDeltaX = 0;
 private float mDeltaY = 0;

 int mDisplayWidth;
 int mDisplayHeight;
 int mPadding;

 public ScrollImageView(Context context) {
 super(context);
 initScrollImageView(context);
 }
 public ScrollImageView(Context context, AttributeSet attributeSet) {
 super(context);
 initScrollImageView(context);
 }
 
 private void initScrollImageView(Context context) {
 mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 mPadding = DEFAULT_PADDING;
 }
 
 @Override
 protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 int width = measureDim(widthMeasureSpec, mDisplay.getWidth());
 int height = measureDim(heightMeasureSpec, mDisplay.getHeight());
 setMeasuredDimension(width, height);
 }
 
 private int measureDim(int measureSpec, int size) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = size;
            if (specMode == MeasureSpec.AT_MOST) {
               result = Math.min(result, specSize);
            }
        }
        return result;
    }
 
 public Bitmap getImage() {
 return mImage;
 }
 
 public void setImage(Bitmap image) {
 mImage = image;
 }

 public int getPadding() {
 return mPadding;
 }

 public void setPadding(int padding) {
 this.mPadding = padding;
 }

 @Override
 public boolean onTouchEvent(MotionEvent event) {
 if (event.getAction() == MotionEvent.ACTION_DOWN) {
 mCurrentX = event.getRawX();
 mCurrentY = event.getRawY();
 } 
 else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 float x = event.getRawX();
 float y = event.getRawY();

 // Update how much the touch moved
 mDeltaX = x - mCurrentX;
 mDeltaY = y - mCurrentY;

 mCurrentX = x;
 mCurrentY = y;

 invalidate();
 }
 // Consume event
 return true;
 }

 @Override
 protected void onDraw(Canvas canvas) {
 if (mImage == null) {
 return;
 }

 float newTotalX = mTotalX + mDeltaX;
 // Don't scroll off the left or right edges of the bitmap.
 if (mPadding > newTotalX && newTotalX > getMeasuredWidth() - mImage.getWidth() - mPadding)
 mTotalX += mDeltaX;

 float newTotalY = mTotalY + mDeltaY;
 // Don't scroll off the top or bottom edges of the bitmap.
 if (mPadding > newTotalY && newTotalY > getMeasuredHeight() - mImage.getHeight() - mPadding)
 mTotalY += mDeltaY;
 
 Paint paint = new Paint();
 canvas.drawBitmap(mImage, mTotalX, mTotalY, paint);
 }
}