package com.qust.assistant.widget.swipe;

import com.billy.android.swipe.consumer.DrawerConsumer;

import static android.view.View.VISIBLE;

public class MainDrawer extends DrawerConsumer{
	
	@Override
	protected void orderChildren() {

		if (mScrimView != null) {
			mScrimView.bringToFront();
		}
		if (mCurDrawerView != null) {
			mCurDrawerView.bringToFront();
		}
	}
	
	@Override
	protected void layoutScrimView() {
		if (mScrimView != null && mScrimView.getVisibility() == VISIBLE) {
			int l = 0, r = mWidth, t = 0, b = mHeight;
			switch (mDirection) {
				case DIRECTION_LEFT:    l = mCurDisplayDistanceX - 120;  break;
				case DIRECTION_RIGHT:   r = r + mCurDisplayDistanceX + 120;  break;
				case DIRECTION_TOP:     t = mCurDisplayDistanceY;  break;
				case DIRECTION_BOTTOM:  b = b + mCurDisplayDistanceY;  break;
				default:
			}
			mScrimView.layout(l, t, r, b);
			mScrimView.setProgress(mProgress);
		}
	}
	
	
}
