package com.qust.assistant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.core.view.MotionEventCompat;

public class VerticalSlidingLayout extends FrameLayout{
    
    // 无效的点
    private static final int INVALID_POINTER = -1;
    
    // 滑动动画执行的时间 ms
    private static final int MAX_SETTLE_DURATION = 400;
    
    // 最小滑动距离，结合加速度来判断需要滑动的方向 dips
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    
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
    private float mInitialMotionY;
    
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
    private int mPiy;

    // 用于绘制阴影时的梯度变化
    private float mRatio;
    // 绘制阴影背景的画笔
    private Paint mShadowPaint;
    
    public VerticalSlidingLayout(Context context){
        this(context,null);
    }

    public VerticalSlidingLayout(Context context,AttributeSet attrs){
        this(context,attrs,0);
    }

    public VerticalSlidingLayout(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        setWillNotDraw(false);
        
        mScroller = new Scroller(context,t -> (float)(Math.pow(t - 1.0f, 5) + 1.0f));
        
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(0xff000000);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinMunVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxMunVelocity = configuration.getScaledMaximumFlingVelocity();

        float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int)(MIN_DISTANCE_FOR_FLING * density);
    
        post(() -> setCurrentItemInternal(true,0));
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
                mLastMotionX = MotionEventCompat.getX(ev,index);
                mLastMotionY = mInitialMotionY = MotionEventCompat.getY(ev,index);
                mIsUnableToDrag = false;
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
        final int action = event.getAction();
        if(mVelocityTracker==null)mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
        switch(action){
            case MotionEvent.ACTION_DOWN:
                // 按下则结束滚动
                completeScroll();
                int index = MotionEventCompat.getActionIndex(event);
                mActivePointerId = MotionEventCompat.getPointerId(event,index);
                mLastMotionY = mInitialMotionY = event.getY();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                // 有多个点按下的时候，取最后一个按下的点为有效点
                final int indexx = MotionEventCompat.getActionIndex(event);
                mLastMotionY = MotionEventCompat.getY(event,indexx);
                mActivePointerId = MotionEventCompat.getPointerId(event,indexx);
                break;
            case MotionEvent.ACTION_MOVE:
                if(!mIsBeingDragged){
                    determineDrag(event);
                    if(mIsUnableToDrag) return false;
                }else{
    
                    // 如果已经是滑动状态，则根据手势滑动，而改变View 的位置
                    // 以下代码用来判断和执行View 的滑动
                    final int activePointerIndex = getPointerIndex(event,mActivePointerId);
                    if(mActivePointerId==INVALID_POINTER)break;
                    final float y = MotionEventCompat.getY(event,activePointerIndex);
                    final float deltaY = mLastMotionY - y;
                    mLastMotionY = y;
                    float oldScrollY = getScrollY();
                    float scrollY = oldScrollY + deltaY;
                    
                    //  防止位置越界
                    if(scrollY>0)scrollY = 0;
                    else if(scrollY<-getHeight())scrollY = -getHeight();
                    
                    mLastMotionY += scrollY - (int)scrollY;
                    scrollTo(getScrollX(),(int)scrollY);
                }
                break;
            case MotionEvent.ACTION_UP:
                // 如果已经是滑动状态，抬起手指，需要判断滚动的位置
                if(mIsBeingDragged){
                    mIsBeingDragged = false;
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000,mMaxMunVelocity);
                    int initialVelocity = (int)velocityTracker.getYVelocity(mActivePointerId);
                    final float pageOffset = (float)(-getScrollY()) / getHeight();
                    final int activePointerIndex = getPointerIndex(event,mActivePointerId);
                    if(mActivePointerId!=INVALID_POINTER){
                        final float y = MotionEventCompat.getY(event,activePointerIndex);
                        final int totalDelta = (int)(y - mInitialMotionY);
                        // 这里判断是否滚动到下一页，还是滚回原位置
                        setCurrentItemInternal(needPageChange(pageOffset,initialVelocity,totalDelta),initialVelocity);
                    }else setCurrentItemInternal(false,initialVelocity);
                    mActivePointerId = INVALID_POINTER;
                }else{
                    scrollTo(getScrollX(),getScrollY());
                }
                endToDrag();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                int pointerIndex = getPointerIndex(event,mActivePointerId);
                if(mActivePointerId==INVALID_POINTER) break;
                mLastMotionY = MotionEventCompat.getY(event,pointerIndex);
                break;
            default:
        }
        return true;
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
            mLastMotionY = MotionEventCompat.getY(ev,newPointerIndex);
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
        if(yDiff>mTouchSlop&&yDiff>xDiff){
            startDrag();
            mLastMotionX = x;
            mLastMotionY = y;
        }else if(yDiff>mTouchSlop) mIsUnableToDrag = true;
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
        final int destY = getDestScrollY(isBack);
        if(mListener!=null)mListener.isBack(isBack);
        // 执行滑动滚动
        if(getChildCount()==0)return;
        int dx = - getScrollX();
        int dy = destY - getScrollY();
        if(dx==0&&dy==0){
            completeScroll();
            // 这里为了解决一个bug，当用手指触摸滑到看不见的时候再用力滑动，如果不做此操作，那么不会回调 position = 1
            scrollTo(getScrollX(),getScrollY());
            return;
        }
        mScrolling = true;
        final int height = getHeight();
        final int halfHeight = height / 2;
        final float distanceRatio = Math.min(1f,1f * Math.abs(dy) / height);
        final float distance = halfHeight + halfHeight * distanceInfluenceForSnapDuration(distanceRatio);
        
        int duration;
        if(velocity==0)duration = 0;
        else duration = Math.min(3 * Math.round(1000 * Math.abs(distance / velocity)),MAX_SETTLE_DURATION);
        
        // 开始自动滚动到指定的位置
        mScroller.startScroll(getScrollX(),getScrollY(),dx,dy,duration);
        invalidate();
    }
    // 根据当前页面来获取需要滚动的目的位置
    public int getDestScrollY(boolean page){
        return page?-getBottom():getTop();
    }
    
    // 通过偏移位置和加速度来确定需要滚动的页
    private boolean needPageChange(float pageOffset,int velocity,int deltaY){
        // 这里判断是否需要滚动到下一页
        if(Math.abs(deltaY)>mFlingDistance&&Math.abs(velocity)>mMinMunVelocity){
            if(velocity>0&&deltaY>0) return true;
            else if(velocity<0&&deltaY<0) return false;
        }else return Math.round(pageOffset)==1;// 根据距离来判断滚动的页码
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
        final int heightWithMargin = getHeight();
        // 获取当前页面
        int position = Math.abs(y) / heightWithMargin;
        // 获取当前滑动的距离
        final int offsetPixels = Math.abs(y) % heightWithMargin;
        // 通过滑动的距离来获取梯度值
        final float offset = (float)offsetPixels / heightWithMargin;
        // 这里需要做特殊处理，因为只有一个页面
        position = mIsBeingDragged?0:position;
        mRatio = offset;
        mPiy = offsetPixels;
        invalidate();
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
        // 绘制偏移的背影颜色
        mShadowPaint.setAlpha((int)((1 - mRatio) * 180));
        canvas.drawRect(0,0,getWidth(),-mPiy,mShadowPaint);
        // 绘制边缘的阴影
        canvas.save();
        // 恢复画布的状态
        canvas.restore();
    }

    public void setBack(boolean isBack){
        setCurrentItemInternal(isBack,100);
    }
    
    public void setBackListener(OnPageChangeListener _listener){
        mListener = _listener;
    }
    
    public interface OnPageChangeListener{
        // 当前页面
        void isBack(boolean isBack);
    }
    
}