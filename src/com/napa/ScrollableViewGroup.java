/**
 * 
 */
package com.napa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author Administrator
 * 
 */
public class ScrollableViewGroup extends ViewGroup {

        private static final int INVALID_SCREEN = -1;
        private static final int TOUCH_STATE_REST = 0;
        private static final int TOUCH_STATE_SCROLLING = 1;
        private static final int SNAP_VELOCITY = 1000;

        private int mDefaultScreen;
        private int mCurrentScreen;
        private int mNextScreen = INVALID_SCREEN;

        private int mMaximumVelocity;
        private Scroller mScroller;
        private int mTouchState;
        private boolean mFirstLayout = true;
        private float mLastMotionX;
        private float mLastMotionY;
        private int mTouchSlop;
        private boolean mAllowLongPress;
        private VelocityTracker mVelocityTracker;

        private int mPaintFlag = 0;

        private OnCurrentViewChangedListener mOnCurrentViewChangedListener;

        public interface OnCurrentViewChangedListener {
                public void onCurrentViewChanged(View view, int currentview);
        };

        /**
         * @param context
         */
        public ScrollableViewGroup(Context context) {
                super(context);
                initViewGroup();
        }

        /**
         * @param context
         * @param attrs
         */
        public ScrollableViewGroup(Context context, AttributeSet attrs) {
                super(context, attrs);
                initViewGroup();
        }

        /**
         * @param context
         * @param attrs
         * @param defStyle
         */
        public ScrollableViewGroup(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
                initViewGroup();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
         */
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int childLeft = 0;
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                        final View child = getChildAt(i);
                        if (child.getVisibility() != View.GONE) {
                                final int childWidth = child.getMeasuredWidth();
                                child.layout(childLeft, 0, childLeft + childWidth, child
                                                .getMeasuredHeight());
                                childLeft += childWidth;
                        }
                }
        }

        /**
         * Initializes various states for this viewgroup.
         */
        private void initViewGroup() {
                mScroller = new Scroller(getContext());
                mCurrentScreen = mDefaultScreen;
                final ViewConfiguration configuration = ViewConfiguration
                                .get(getContext());
                mTouchSlop = configuration.getScaledTouchSlop();

                // mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        }

        public boolean isDefaultViewShowing() {
                return mCurrentScreen == mDefaultScreen;
        }

        public int getCurrentView() {
                return mCurrentScreen;
        }

        public void setCurrentView(int currentView) {
                // snapToScreen(currentView);
                mCurrentScreen = Math
                                .max(0, Math.min(currentView, getChildCount() - 1));
                scrollTo(mCurrentScreen * getWidth(), 0);
                if (mOnCurrentViewChangedListener != null)
                        mOnCurrentViewChangedListener.onCurrentViewChanged(this,
                                        mCurrentScreen);
                invalidate();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.View#computeScroll()
         */
        @Override
        public void computeScroll() {
        //	Log.d("Curt", "computeScroll");
                if (mScroller.computeScrollOffset()) {
                        // FIXED 卷动无法触发下次的computeScroll;
                        final int currx = mScroller.getCurrX(), curry = mScroller
                                        .getCurrY(), scrx = getScrollX(), scry = getScrollY();

                        if (currx != scrx || curry != scry)
                                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                        else
                                invalidate();
                } else if (mNextScreen != INVALID_SCREEN) {
                        mCurrentScreen = Math.max(0, Math.min(mNextScreen,
                                        getChildCount() - 1));
                        mNextScreen = INVALID_SCREEN;
                        mPaintFlag = 0;
                        clearChildrenCache();
                        final int scrx = getScrollX(), scry = getScrollY(), mCurrentScrollX = mCurrentScreen
                                        * getWidth();
                        if (scrx != mCurrentScrollX)
                                scrollTo(mCurrentScrollX, scry);
                        if (mOnCurrentViewChangedListener != null)
                                mOnCurrentViewChangedListener.onCurrentViewChanged(this,
                                                mCurrentScreen);
                }
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
         */
        @Override
        protected void dispatchDraw(Canvas canvas) {
                boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING
                                && mNextScreen == INVALID_SCREEN;
                // If we are not scrolling or flinging, draw only the current screen
                if (fastDraw) {
                        drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
                } else {
                        final long drawingTime = getDrawingTime();
                        // If we are flinging, draw only the current screen and the target
                        // screen
                        if (mNextScreen >= 0
                                        && mNextScreen < getChildCount()
                                        && (Math.abs(mCurrentScreen - mNextScreen) == 1 || mPaintFlag != 0)) {
                                final View viewCurrent = getChildAt(mCurrentScreen), viewNext = getChildAt(mNextScreen);

                                drawChild(canvas, viewCurrent, drawingTime);
                                if (mPaintFlag == 0) {
                                        drawChild(canvas, viewNext, drawingTime);
                                } else {
                                        Paint paint = new Paint();
                                        if (mPaintFlag < 0) {
                                                canvas.drawBitmap(viewNext.getDrawingCache(), -viewNext
                                                                .getWidth(), viewNext.getTop(), paint);
                                        } else {
                                                canvas.drawBitmap(viewNext.getDrawingCache(),
                                                                getWidth() * getChildCount(),
                                                                viewNext.getTop(), paint);
                                        }
                                }
                        } else {
                                // If we are scrolling, draw all of our children
                                final int count = getChildCount();
                                for (int i = 0; i < count; i++) {
                                        drawChild(canvas, getChildAt(i), drawingTime);
                                }

                                if (mPaintFlag != 0) {
                                        final View viewNext;
                                        Paint paint = new Paint();
                                        if (mPaintFlag < 0) {
                                                viewNext = getChildAt(getChildCount() - 1);
                                           //     Log.d("Curt", "dispatchDraw: " + viewNext.getDrawingCache());
                                                if (viewNext.getDrawingCache() == null) {
                                                	viewNext.buildDrawingCache();
                                                }
                                                canvas.drawBitmap(viewNext.getDrawingCache(), -viewNext
                                                                .getWidth(), viewNext.getTop(), paint);
                                        } else {
                                                viewNext = getChildAt(0);
                                                canvas.drawBitmap(viewNext.getDrawingCache(),
                                                                getWidth() * getChildCount(),
                                                                viewNext.getTop(), paint);
                                        }
                                }
                        }
                }
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.View#onMeasure(int, int)
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                final int width = MeasureSpec.getSize(widthMeasureSpec);
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                        getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
                }

                if (mFirstLayout) {
                        scrollTo(mCurrentScreen * width, 0);
                        mFirstLayout = false;
                }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * android.view.ViewGroup#requestChildRectangleOnScreen(android.view.View,
         * android.graphics.Rect, boolean)
         */
        @Override
        public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
                        boolean immediate) {
                int screen = indexOfChild(child);
                if (screen != mCurrentScreen || !mScroller.isFinished()) {
                        snapToScreen(screen);
                        return true;
                }
                return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.ViewGroup#onRequestFocusInDescendants(int,
         * android.graphics.Rect)
         */
        @Override
        protected boolean onRequestFocusInDescendants(int direction,
                        Rect previouslyFocusedRect) {
                int focusableScreen;
                if (mNextScreen != INVALID_SCREEN) {
                        focusableScreen = mNextScreen;
                } else {
                        focusableScreen = mCurrentScreen;
                }
         //       Log.d("Curt", "onRequestFocusInDescendants: " + focusableScreen);
                boolean log = getChildAt(focusableScreen).requestFocus(direction,
                                previouslyFocusedRect);
         //       Log.d("Curt", "onRequestFocusInDescendants" + log);
                return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.ViewGroup#dispatchUnhandledMove(android.view.View, int)
         */
        @Override
        public boolean dispatchUnhandledMove(View focused, int direction) {
                if (direction == View.FOCUS_LEFT) {
                        if (getCurrentView() > 0) {
                                snapToScreen(getCurrentView() - 1);
                                return true;
                        }
                } else if (direction == View.FOCUS_RIGHT) {
                        if (getCurrentView() < getChildCount() - 1) {
                                snapToScreen(getCurrentView() + 1);
                                return true;
                        }
                }
                return super.dispatchUnhandledMove(focused, direction);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
        //	    Log.d("Curt", "onInterceptTouchEvent");
                final int action = ev.getAction();
                if ((action == MotionEvent.ACTION_MOVE)
                                && (mTouchState != TOUCH_STATE_REST)) {
                        return true;
                }

                final float x = ev.getX();
                final float y = ev.getY();

                switch (action) {
                case MotionEvent.ACTION_MOVE:
                        final int xDiff = (int) Math.abs(x - mLastMotionX);
                        final int yDiff = (int) Math.abs(y - mLastMotionY);

                        final int touchSlop = mTouchSlop;
                        boolean xMoved = xDiff > touchSlop;
                        boolean yMoved = yDiff > touchSlop;

                        if (xMoved || yMoved) {

                                if (xMoved) {
                                        // Scroll if the user moved far enough along the X axis
                                        mTouchState = TOUCH_STATE_SCROLLING;
                                        enableChildrenCache();
                                }
                                // Either way, cancel any pending longpress
                                if (mAllowLongPress) {
                                        mAllowLongPress = false;
                                        // Try canceling the long press. It could also have been
                                        // scheduled
                                        // by a distant descendant, so use the mAllowLongPress flag
                                        // to block
                                        // everything
                                        final View currentScreen = getChildAt(mCurrentScreen);
                                        currentScreen.cancelLongPress();
                                }
                        }
                        break;

                case MotionEvent.ACTION_DOWN:
                        // Remember location of down touch
                        mLastMotionX = x;
                        mLastMotionY = y;
                        mAllowLongPress = true;

                        /*
                         * If being flinged and user touches the screen, initiate drag;
                         * otherwise don't. mScroller.isFinished should be false when being
                         * flinged.
                         */
                        mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                                        : TOUCH_STATE_SCROLLING;
                        break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                        mTouchState = TOUCH_STATE_REST;
                        mAllowLongPress = false;
                        break;
                }
                /*
                 * The only time we want to intercept motion events is if we are in the
                 * drag mode.
                 */
                return mTouchState != TOUCH_STATE_REST;
        }

        void enableChildrenCache() {
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                        final View layout = getChildAt(i);
                        layout.setDrawingCacheEnabled(true);
                        if (layout instanceof ViewGroup) {
                                ((ViewGroup) layout).setAlwaysDrawnWithCacheEnabled(true);
                        }
                }
        }

        void clearChildrenCache() {
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                        final View layout = getChildAt(i);
                        if (layout instanceof ViewGroup) {
                                ((ViewGroup) layout).setAlwaysDrawnWithCacheEnabled(false);
                        }
                }
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.view.View#onTouchEvent(android.view.MotionEvent)
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
        	
                if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);

                final int action = event.getAction();
                final float x = event.getX();

                switch (action) {
                case MotionEvent.ACTION_DOWN:
                        /*
                         * If being flinged and user touches, stop the fling. isFinished
                         * will be false if being flinged.
                         */
                        if (!mScroller.isFinished()) {
                                mScroller.abortAnimation();
                        }

                        // Remember where the motion event started
                        mLastMotionX = x;
                        mTouchState = TOUCH_STATE_SCROLLING;
                        break;
                case MotionEvent.ACTION_MOVE:
               // 	Log.d("Curt", "move : " + mTouchState);
                        if (mTouchState == TOUCH_STATE_SCROLLING) {
                                // Scroll to follow the motion event
                                final int deltaX = (int) (mLastMotionX - x);
                                mLastMotionX = x;

                                if (deltaX < 0) {
                                        /*
                                         * if (getScrollX() > 0) { scrollBy(Math.max(-getScrollX(),
                                         * deltaX), 0); }
                                         */
                                        if (getScrollX() <= 0) {
                                                mPaintFlag = -1;
                                        }
                                        scrollBy(deltaX, 0);
                                } else if (deltaX > 0) {
                                        int availableToScroll = getChildAt(getChildCount() - 1)
                                                        .getRight()
                                                        - getScrollX() - getWidth();
                                        /*
                                         * if (availableToScroll > 0) {
                                         * scrollBy(Math.min(availableToScroll, deltaX), 0); }
                                         */
                                        if (availableToScroll <= 0) {
                                                mPaintFlag = 1;
                                                availableToScroll += getWidth() << 1;
                                        }
                                        if (availableToScroll > 0)
                                                scrollBy(Math.min(availableToScroll, deltaX), 0);
                                }
                        }
                        break;
                case MotionEvent.ACTION_UP:
                        if (mTouchState == TOUCH_STATE_SCROLLING) {
                                final VelocityTracker velocityTracker = mVelocityTracker;
                                // velocityTracker.computeCurrentVelocity(1000,
                                // mMaximumVelocity);
                                int velocityX = (int) velocityTracker.getXVelocity();

                                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                                        // Fling hard enough to move left
                                        snapToScreen(mCurrentScreen - 1);
                                } else if (velocityX < -SNAP_VELOCITY
                                                && mCurrentScreen < getChildCount() - 1) {
                                        // Fling hard enough to move right
                                        snapToScreen(mCurrentScreen + 1);
                                } else {
                                        snapToDestination();
                                }

                                if (mVelocityTracker != null) {
                                        mVelocityTracker.recycle();
                                        mVelocityTracker = null;
                                }
                        }
                        mTouchState = TOUCH_STATE_REST;
                        break;
                case MotionEvent.ACTION_CANCEL:
                //	Log.d("Curt", "cancel");
                        mTouchState = TOUCH_STATE_REST;
                }

                return true;
        }

        private void snapToDestination() {
                final int screenWidth = getWidth();
                final int scrollWidth = getScrollX() + (screenWidth >> 1);
                final int viewCount = getChildCount();
                final int whichScreen;
                if (scrollWidth < 0)
                        whichScreen = -1;
                else if (scrollWidth > screenWidth * viewCount)
                        whichScreen = viewCount;
                else
                        whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
                snapToScreen(whichScreen);
        }

        public void snapToScreen(int whichScreen) {
        	    // Log.d("Curt", "snapToScreen");
        	    if (whichScreen != mCurrentScreen) {
        	    	View child = getChildAt(mCurrentScreen);
        	    	if (child instanceof ImageViewTouch) {
        	    		ImageViewTouch touch = (ImageViewTouch) child;
        	    		if (touch.isSmaller) {
        	    		    touch.zoomTo(1.0f);
        	    		    touch.isSmaller = false;
        	    		}
        	    	}
        	    } else {
        	    	View child = getChildAt(mCurrentScreen);
        	    	if (child instanceof ImageViewTouch) {
        	    		ImageViewTouch touch = (ImageViewTouch) child;
        	    		if (touch.isSmaller) {
        	    		    touch.zoomTo(1.0f);
        	    		    touch.isSmaller = false;
        	    		}
        	    	}
        	    }
                if (!mScroller.isFinished())
                        return;
                enableChildrenCache();
                final int viewCount = getChildCount() - 1;

                final int oldWhichScreen = whichScreen;

                if (whichScreen < 0) {
                        whichScreen = viewCount;
                        mPaintFlag = -1; // next screen should be painted before current
                } else if (whichScreen > viewCount) {
                        whichScreen = 0;
                        mPaintFlag = 1;
                } else
                        mPaintFlag = 0;

                // whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() -
                // 1));
                boolean changingScreens = whichScreen != mCurrentScreen;

                mNextScreen = whichScreen;

                
                View focusedChild = getFocusedChild();
                Log.d("Curt", "snapToScreen: Child is: " + focusedChild);
                if (focusedChild != null && changingScreens
                                && focusedChild == getChildAt(mCurrentScreen)) {
                        focusedChild.clearFocus();
                }
                //getChildAt(mCurrentScreen).requestFocus();
                //Log.d("Curt", "snapToScreen: " + mCurrentScreen);
                final int newX = oldWhichScreen * getWidth();
                final int delta = newX - getScrollX();
                mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);

                invalidate();
                
                /*
                 * if (!mScroller.isFinished()) return; enableChildrenCache();
                 * 
                 * whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() -
                 * 1)); boolean changingScreens = whichScreen != mCurrentScreen;
                 * 
                 * mNextScreen = whichScreen;
                 * 
                 * View focusedChild = getFocusedChild(); if (focusedChild != null &&
                 * changingScreens && focusedChild == getChildAt(mCurrentScreen)) {
                 * focusedChild.clearFocus(); }
                 * 
                 * final int newX = whichScreen * getWidth(); final int delta = newX -
                 * getScrollX(); mScroller.startScroll(getScrollX(), 0, delta, 0,
                 * Math.abs(delta) * 2);
                 * 
                 * invalidate();
                 */}

        public void scrollLeft() {
                if (mNextScreen == INVALID_SCREEN && mCurrentScreen > 0
                                && mScroller.isFinished()) {
                        snapToScreen(mCurrentScreen - 1);
                }
        }

        public void scrollRight() {
                if (mNextScreen == INVALID_SCREEN
                                && mCurrentScreen < getChildCount() - 1
                                && mScroller.isFinished()) {
                        snapToScreen(mCurrentScreen + 1);
                }
        }

        public void moveToDefaultScreen() {
                snapToScreen(mDefaultScreen);
                getChildAt(mDefaultScreen).requestFocus();
        }

        public boolean isScrollFinish() {
                return mTouchState != TOUCH_STATE_SCROLLING
                                && mNextScreen == INVALID_SCREEN;
                // return mScroller.isFinished();
        }

        public OnCurrentViewChangedListener getOnCurrentViewChangedListener() {
                return mOnCurrentViewChangedListener;
        }

        public void setOnCurrentViewChangedListener(
                        OnCurrentViewChangedListener mOnCurrentViewChangedListener) {
                this.mOnCurrentViewChangedListener = mOnCurrentViewChangedListener;
        }
}