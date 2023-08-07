package com.qust.base.ui;

import com.qust.base.fragment.BaseFragment;

/**
 * Fragment的接口
 */
public interface FragmentAble{
	
	/**
	 * 添加一个新的Fragment
	 */
	void startNewFragment(Class<? extends BaseFragment> newFragment);
	
	/**
	 * 移除当前Fragment (相当于返回)
	 */
	void closeFragment();
	
}
