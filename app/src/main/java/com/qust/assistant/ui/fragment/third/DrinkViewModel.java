package com.qust.assistant.ui.fragment.third;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.qust.assistant.App;

public class DrinkViewModel extends AndroidViewModel{
	
	private final MutableLiveData<DrinkData> userData;
	
	private final MutableLiveData<String> drinkCode;
	
	private final MutableLiveData<Boolean> loginSuccess;
	
	public static DrinkViewModel getInstance(@NonNull Context context){
		return ((App)context.getApplicationContext()).drinkViewModel;
	}
	
	public DrinkViewModel(@NonNull Application application){
		super(application);
		
		userData = new MutableLiveData<>();
		userData.setValue(DrinkData.getUserData(application));
		
		drinkCode = new MutableLiveData<>();
		drinkCode.setValue(DrinkData.getDrinkCode(application));
		
		loginSuccess = new MutableLiveData<>();
	}
	
	public MutableLiveData<DrinkData> getUserLiveData(){
		return userData;
	}
	
	public MutableLiveData<String> getDrinkCodeLiveData(){
		return drinkCode;
	}
	
	public MutableLiveData<Boolean> getLoginSuccessLiveData(){
		return loginSuccess;
	}
	
	public DrinkData getUserData(){
		return userData.getValue();
	}
	
	public String getDrinkCode(){
		return drinkCode.getValue();
	}
	
	public void updateUserData(DrinkData data){
		userData.postValue(data);
		DrinkData.saveUserData(getApplication(), data);
	}
	
	public void updateDrinkCode(String code){
		drinkCode.postValue(code);
		DrinkData.saveDrinkCode(getApplication(), code);
	}
	
	public void login(DrinkData data){
		userData.postValue(data);
		loginSuccess.postValue(true);
		DrinkData.saveUserData(getApplication(), data);
	}
	
	
}
