package com.qust.assistant.ui.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qust.assistant.R;
import com.qust.assistant.accounts.Account;
import com.qust.assistant.sql.AccountsData;
import com.qust.assistant.ui.BaseAnimActivity;
import com.qust.assistant.util.DateUtil;

import androidx.annotation.Nullable;

public class AccountsActivity extends BaseAnimActivity{
	
	private AccountsData accountsData;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		AccountsData.init(this);
		
		accountsData = AccountsData.getInstance();
		
		setContentView(R.layout.activity_accounts);
		
		// findViewById(R.id.activity_account_add).setOnClickListener(v -> startActivity(new Intent(AccountsActivity.this, RecordActivity.class)));
		
		ListView listView = findViewById(R.id.activity_account_list);
		
		initToolBar(null);
		initSliding(null, null);
	}
	
	
	private class AccountAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return accountsData.day.size(); }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(AccountsActivity.this).inflate(R.layout.item_account, null);
			}
			
			int i = position == 0 ? 0 : accountsData.day.get(position - 1);
			
			Account account = accountsData.accounts.get(i);
			
			((TextView)convertView.findViewById(R.id.item_account_date)).setText(DateUtil.YMD.format(account.date));
			
			LinearLayout content = convertView.findViewById(R.id.activity_account_content);
			
			content.removeAllViews();
			
			for(;i<accountsData.day.get(position);i++){
				LinearLayout item = (LinearLayout)LayoutInflater.from(AccountsActivity.this).inflate(R.layout.item_account_item, null);
				account = accountsData.accounts.get(i);
				// ((TextView)convertView.findViewById(R.id.item_account_image)).setText(exam.place);
				((TextView)convertView.findViewById(R.id.item_account_name)).setText(String.valueOf(account.type));
				((TextView)convertView.findViewById(R.id.item_account_value)).setText(String.valueOf(account.value));
				content.addView(item);
			}
			return convertView;
		}
	}
}
