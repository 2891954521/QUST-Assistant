package com.university.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.university.assistant.R;

public class SlidingLayout extends FrameLayout{
    
    // 无效的点
    private static final int INVALID_POINTER = -1;
    
    // 滑动动画执行的时间 ms
    private static final int MAX_SETTLE_DURATION = 400;
    
    // 最小滑动距离，结合加速度来判断需要滑动的方向 dips
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    
    // 页面边缘阴影宽度值
    private static final int SHADOW_WIDTH = 6;
    
    // 是否开始滑动
    private boolean mIsBeingDragged;
    // 是否停止滑动
    private boolean mIsUnableToDrag;
    // 判断是否已经在滚动
    private boolean mScrolling;

    private Scroller mScroller;

    protected VelocityTracker mVelocityTracker;
    // 共外面调用的监听事件
    private OnPageChangeListener mListener;
    // 记录上一次手指触摸的点
    private float mLastMotionX,mLastMotionY;
    // 记录最初触摸的点
    private float mInitialMotionX;
    // 当前活动的点Id,有效的点的Id
    protected int mActivePointerId = INVALID_POINTER;
    // 最小加速度的值
    private int mMinMunVelocity;
    // 最大加速度的值
    private int mMaxMunVelocity;
    // 滑动的距离
    private int mFlingDistance;
    // 开始滑动的标志距离
    private int mTouchSlop;
    // 页面滑动的距离
    private int mPix;
    // 是否需要绘制阴影效果
    private boolean mShouldDraw = true;
    // 用于绘制阴影时的梯度变化
    private float mRatio;
    // 绘制阴影背景的画笔
    private Paint mShadowPaint;
    // 页面边缘的阴影图
    private Drawable mLeftShadow;
    // 页面边缘阴影的宽度
    private int mShadowWidth;
    //用于定制自己的动画效果的接口
    private OnAnimListener mAnimListener;
    // 标记屏蔽的View
    private View disallowView;
    
    public SlidingLayout(Context context){
        this(context,null);
    }

    public SlidingLayout(Context context,AttributeSet attrs){
        this(context,attrs,0);
    }

    public SlidingLayout(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        setWillNotDraw(false);
        
        mScroller = new Scroller(context,new Interpolator(){
            @Override
            public float getInterpolation(float t){
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        });
        
        mLeftShadow = ResourcesCompat.getDrawable(getResources(),R.drawable.shadow_bottom,null);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(0xff000000);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinMunVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxMunVelocity = configuration.getScaledMaximumFlingVelocity();

        float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int)(MIN_DISTANCE_FOR_FLING * density);
        mShadowWidth = (int)(SHADOW_WIDTH * density);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        // 设置 drawable 的大小范围
        mLeftShadow.setBounds(0,0,mShadowWidth,getHeight());
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        final int action = ev.getAction();
        if(action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_UP){
            endToDrag();
            return false;
        }
        if(mIsUnableToDrag)return false;
        switch(action){
            case MotionEvent.ACTION_DOWN:
                // 计算 x，y 的距离
                int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev,index);
                if(mActivePointerId==INVALID_POINTER) break;
                mLastMotionX = mInitialMotionX = MotionEventCompat.getX(ev,index);
                mLastMotionY = MotionEventCompat.getY(ev,index);
                // 这里判断，如果这个触摸区域是允许滑动拦截的，则拦截事件
                if(isDisallowTouch(ev)) mIsUnableToDrag = true;
                else{
                    mIsBeingDragged = false;
                    mIsUnableToDrag = false;
                }
                if(!mScroller.isFinished()) startDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                // 继续判断是否需要拦截
                determineDrag(ev);
                break;
            case MotionEvent.ACTION_UP: break;
            case MotionEvent.ACTION_POINTER_UP:
                // 这里做了对多点触摸的处理，当有多个手指触摸的时候依然能正确的滑动
                onSecondaryPointerUp(ev);
                break;
            default:
        }
        if(!mIsBeingDragged){
            if(mVelocityTracker==null) mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.addMovement(ev);
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        // if(!mIsBeingDragged /*&&isDisallowTouch(event)*/ ) return false;
        final int action = event.getAction();
        if(mVelocityTracker==null)mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
        switch(action){
            case MotionEvent.ACTION_DOWN:
                // 按下则结束滚动
                completeScroll();
                int index = MotionEventCompat.getActionIndex(event);
                mActivePointerId = MotionEventCompat.getPointerId(event,index);
                mLastMotionX = mInitialMotionX = event.getX();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                /*有多个点按下的时候，取最后一个按下的点为有效点*/
                final int indexx = MotionEventCompat.getActionIndex(event);
                mLastMotionX = MotionEventCompat.getX(event,indexx);
                mActivePointerId = MotionEventCompat.getPointerId(event,indexx);
                break;
            case MotionEvent.ACTION_MOVE:
                if(!mIsBeingDragged){
                    determineDrag(event);
                    if(mIsUnableToDrag) return false;
                }
                // 如果已经是滑动状态，则根据手势滑动，而改变View 的位置
                if(mIsBeingDragged){
                    // 以下代码用来判断和执行View 的滑动
                    final int activePointerIndex = getPointerIndex(event,mActivePointerId);
                    if(mActivePointerId==INVALID_POINTER)break;
                    final float x = MotionEventCompat.getX(event,activePointerIndex);
                    final float deltaX = mLastMotionX - x;
                    mLastMotionX = x;
                    float oldScrollX = getScrollX();
                    float scrollX = oldScrollX + deltaX;
                    final float leftBound = getLeftBound();
                    final float rightBound = getRightBound();
                    if(scrollX<leftBound)scrollX = leftBound;
                    else if(scrollX>rightBound)scrollX = rightBound;
                    mLastMotionX += scrollX - (int)scrollX;
                    scrollTo((int)scrollX,getScrollY());
                }
                break;
            case MotionEvent.ACTION_UP:
                // 如果已经是滑动状态，抬起手指，需要判断滚动的位置
                if(mIsBeingDragged){
                    mIsBeingDragged = false;
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000,mMaxMunVelocity);
                    int initialVelocity = (int)velocityTracker.getXVelocity(mActivePointerId);
                    final float pageOffset = (float)(-getScrollX()) / getContentWidth();
                    final int activePointerIndex = getPointerIndex(event,mActivePointerId);
                    if(mActivePointerId!=INVALID_POINTER){
                        final float x = MotionEventCompat.getX(event,activePointerIndex);
                        final int totalDelta = (int)(x - mInitialMotionX);
                        // 这里判断是否滚动到下一页，还是滚回原位置
                        setCurrentItemInternal(needPageChange(pageOffset,initialVelocity,totalDelta),initialVelocity);
                    }else setCurrentItemInternal(false,initialVelocity);
                    mActivePointerId = INVALID_POINTER;
                }else{
                    // setCurrentItemInternal(0, true, 0);
                    scrollTo(getScrollX(),getScrollY());
                }
                endToDrag();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                int pointerIndex = getPointerIndex(event,mActivePointerId);
                if(mActivePointerId==INVALID_POINTER) break;
                mLastMotionX = MotionEventCompat.getX(event,pointerIndex);
                break;
            default:
        }
        return true;
    }

    private float getLeftBound(){
        return -getWidth();
    }

    private float getRightBound(){
        return 0;
    }

    private int getContentWidth(){
        return getWidth();
    }
    
    // 通过事件和点的 id 来获取点的索引
    private int getPointerIndex(MotionEvent ev,int id){
        int activePointerIndex = MotionEventCompat.findPointerIndex(ev,id);
        if(activePointerIndex==-1) mActivePointerId = INVALID_POINTER;
        return activePointerIndex;
    }
    
    // 结束拖拽
    private void endToDrag(){
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        mActivePointerId = INVALID_POINTER;
        if(mVelocityTracker!=null){
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    // 可以拖拽
    private void startDrag(){
        mIsBeingDragged = true;
    }
    
    // 这里是多多点触控的控制
    private void onSecondaryPointerUp(MotionEvent ev){
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev,pointerIndex);
        if(pointerId==mActivePointerId){
            final int newPointerIndex = pointerIndex==0?1:0;
            mLastMotionX = MotionEventCompat.getX(ev,newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev,newPointerIndex);
            if(mVelocityTracker!=null){
                mVelocityTracker.clear();
            }
        }
    }
    
    // 决定是否可以拖拽
    private void determineDrag(MotionEvent event){
        // 这么一大串代码只有一个目的，就是用来获取和判断手指触摸的位置
        int pointIndex = MotionEventCompat.getActionIndex(event);
        int pointId = MotionEventCompat.getPointerId(event,pointIndex);
        if(pointId==INVALID_POINTER) return;
        final float x = MotionEventCompat.getX(event,pointIndex);
        final float y = MotionEventCompat.getY(event,pointIndex);
        final float dx = x - mLastMotionX;
        final float dy = y - mLastMotionY;
        final float xDiff = Math.abs(dx);
        final float yDiff = Math.abs(dy);
        // 如果滑动的距离大于我们规定的默认位置，并且水平滑动的幅度大于垂直滑动的幅度，则说明可以滑动此View
        if(xDiff>mTouchSlop&&xDiff>yDiff&&thisSlideAllowed(dx)){
            startDrag();
            mLastMotionX = x;
            mLastMotionY = y;
        }else if(xDiff>mTouchSlop) mIsUnableToDrag = true;
        
    }
    
    // 触摸区域不允许拖拽或者存在滑动冲突则屏蔽掉该区域
    private boolean isDisallowTouch(MotionEvent ev){
        if(disallowView==null)return false;
        int[] location = new int[2];
        disallowView.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getRawX() < x || ev.getRawX() > (x + disallowView.getWidth()) ||ev.getRawY() < y || ev.getRawY() > (y + disallowView.getHeight()));
    }
    
    // 如果手势是向右滑动返回为 true
    private boolean thisSlideAllowed(float dx){
        return dx>0;
    }

    @Override
    public void computeScroll(){
        if(!mScroller.isFinished()){
            if(mScroller.computeScrollOffset()){
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                if(oldX!=x||oldY!=y)scrollTo(x,y);
                invalidate();
                return;
            }
        }
        completeScroll();
    }
    
    private void setCurrentItemInternal(boolean isBack,int velocity){
        // 根据是否返回判断应该滑动的距离
        final int destX = getDestScrollX(isBack);
        if(mListener!=null)mListener.isBack(isBack);
        // 执行滑动滚动
        if(getChildCount()==0)return;
        int dx = destX - getScrollX();
        int dy = -getScrollY();
        if(dx==0&&dy==0){
            completeScroll();
            // 这里为了解决一个bug，当用手指触摸滑到看不见的时候再用力滑动，如果不做此操作，那么不会回调 position = 1
            scrollTo(getScrollX(),getScrollY());
            return;
        }
        mScrolling = true;
        final int width = getContentWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f,1f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);
        int duration = MAX_SETTLE_DURATION;
        velocity = Math.abs(velocity);
        if(velocity>0) duration = 3 * Math.round(1000 * Math.abs(distance / velocity));
        //else{
            // final float pageDelta = (float)Math.abs(dx) / width;
            // duration = (int)((pageDelta + 1) * 100);
            // duration = MAX_SETTLE_DURATION;
        //}
        duration = Math.min(duration,MAX_SETTLE_DURATION);
        // 开始自动滚动到指定的位置
        mScroller.startScroll(getScrollX(),getScrollY(),dx,dy,duration);
        invalidate();
    }
    // 根据当前页面来获取需要滚动的目的位置
    public int getDestScrollX(boolean page){
        return page?-getRight():getLeft();
    }
    
    // 通过偏移位置和加速度来确定需要滚动的页
    private boolean needPageChange(float pageOffset,int velocity,int deltaX){
        // 这里判断是否需要滚动到下一页
        if(Math.abs(deltaX)>mFlingDistance&&Math.abs(velocity)>mMinMunVelocity){
            if(velocity>0&&deltaX>0) return true;
            else if(velocity<0&&deltaX<0) return false;
        }else return Math.round(pageOffset)==1;     // 根据距离来判断滚动的页码
        return false;
    }

    private void completeScroll(){
        boolean needPopulate = mScrolling;
        if(needPopulate)mScroller.abortAnimation();
        mScrolling = false;
    }

    @Override
    public void scrollTo(int x,int y){
        super.scrollTo(x,y);
        // 设置回调事件的值
        final int widthWithMargin = getWidth();
        // 获取当前页面
        int position = Math.abs(x) / widthWithMargin;
        // 获取当前滑动的距离
        final int offsetPixels = Math.abs(x) % widthWithMargin;
        // 通过滑动的距离来获取梯度值
        final float offset = (float)offsetPixels / widthWithMargin;
        // 这里需要做特殊处理，因为只有一个页面
        position = mIsBeingDragged?0:position;
        if(mListener!=null)mListener.onPageScrolled(position,offset,offsetPixels);
        if(mAnimListener!=null)mAnimListener.onAnimationSet(this,offset,offsetPixels);
        if(mShouldDraw){
            mRatio = offset;
            mPix = offsetPixels;
            invalidate();
        }
    }

    private float distanceInfluenceForSnapDuration(float f){
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float)Math.sin(f);
    }
    
    // 阴影绘制相关
    @Override
    protected void dispatchDraw(Canvas canvas){
        super.dispatchDraw(canvas);
        if(mShouldDraw){
            // 绘制偏移的背影颜色
            mShadowPaint.setAlpha((int)((1 - mRatio) * 180));
            canvas.drawRect(-mPix,0,0,getHeight(),mShadowPaint);
            // 绘制边缘的阴影
            canvas.save();
            // 让画布平移一定距离
            canvas.translate(-mShadowWidth,0);
            // 绘制Drawable
            mLeftShadow.draw(canvas);
            // 恢复画布的状态
            canvas.restore();
        }
    }
    
    // 设置一个点击后不触发滑动返回的View
    public void setDisallowView(View _disallowView){ disallowView = _disallowView; }
    
    // 设置自己定义的动画
    public void setOnAnimListener(OnAnimListener listener){
        if(listener==null)mAnimListener = new SimpleAnimImpl();
        else mAnimListener = listener;
        mShouldDraw = false;
    }
    
    public void setOnPageChangeListener(OnPageChangeListener listener){
        mListener = listener;
    }
    
    public interface OnAnimListener{
        void onAnimationSet(View view,float offSet,int offSetPix);
    }

    public interface OnPageChangeListener{
        // 滑动页面滑动状态，当前页和页面的偏移梯度，页面的偏移位置
        void onPageScrolled(int position,float positionOffset,int positionOffsetPixels);
        // 当前页面
        void isBack(boolean isBack);
    }
    
    // 默认的动画效果
    public static class SimpleAnimImpl implements OnAnimListener{
        private final int MAX_ANGLE = 25;
        @Override
        public void onAnimationSet(View view,float offSet,int offSetPix){
            view.setPivotX(view.getWidth() / 2.0F);
            view.setPivotY(view.getHeight());
            View.ROTATION.set(view,MAX_ANGLE * offSet);
        }
    }

}