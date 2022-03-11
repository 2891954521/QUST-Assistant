package com.qust.assistant.ui.fragment.accounts;

import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;

public class RecordFragment extends BaseFragment{
	
	public RecordFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_home;
	}
	
	@Override
	public String getName(){
		return "记账";
	}
}
