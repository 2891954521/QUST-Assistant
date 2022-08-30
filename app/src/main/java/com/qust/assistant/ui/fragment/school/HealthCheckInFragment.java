package com.qust.assistant.ui.fragment.school;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.ui.fragment.BaseFragment;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthCheckInFragment extends BaseFragment{
	
	private static final Pattern PATTERN = Pattern.compile("<input type=\"hidden\" name=\"execution\" value=\"(.*?)\"");
	
	private static final Pattern PK_FIELD = Pattern.compile("id=\"pkField\"(.*?)value=\"(.*?)\"");
	private static final Pattern TABLE_ID = Pattern.compile("id='tableId'(.*?)value='(.*?)'");
	private static final Pattern ALIAS = Pattern.compile("id='alias'(.*?)value='(.*?)'");
	private static final Pattern TABLE_NAME = Pattern.compile("id='tableName'(.*?)value='(.*?)'");
	
	private static final Pattern COOKIE_PATTERN = Pattern.compile("[A-Za-z0-9_]+=[0-9A-Za-z.%]+;");
	
	private static final String[] HEADERS = new String[]{
			"User-Agent", "Mozilla/5.0 (Linux; Android 11; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/94.0.4606.71 Mobile Safari/537.36",
			"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
			"Accept-Encoding", "gzip, deflate",
			"Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
			"Connection", "keep-alive",
	};
	
	private MaterialDialog dialog;
	
	private Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case App.UPDATE_DIALOG:
					dialog.setContent((String)msg.obj);
					break;
				case App.DISMISS_TOAST:
					dialog.dismiss();
					toast((String)msg.obj);
					break;
			}
		}
	};
	
	private TextInputLayout nameText;
	
	private TextInputLayout passwordText;
	
	private TextInputLayout cookieText;
	
	private TextView nsfyjzxgym, nzhychsjcsj;
	
	private String cookie;
	
	public HealthCheckInFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		dialog = new MaterialDialog.Builder(activity).progress(true, 0).content("请稍候").build();
		
		cookie = SettingUtil.getString(SettingUtil.HEALTH_CHECK_COOKIE, null);
		
		nameText = findViewById(R.id.input_name);
		passwordText = findViewById(R.id.input_password);
		
		cookieText = findViewById(R.id.input_cookie);
		cookieText.getEditText().setText(cookie);
		
		nsfyjzxgym = findViewById(R.id.health_check_nsfyjzxgym);
		nzhychsjcsj = findViewById(R.id.health_check_nzhychsjcsj);
		
		nameText.getEditText().setText(SettingUtil.getString(SettingUtil.HEALTH_CHECK_USER, ""));
		passwordText.getEditText().setText(SettingUtil.getString(SettingUtil.HEALTH_CHECK_PASSWORD, ""));
		
		nsfyjzxgym.setText(SettingUtil.getString(SettingUtil.HEALTH_CHECK_NSFYJZXGYM, "已接种第3针（加强针）"));
		nzhychsjcsj.setText(DateUtil.YMD.format(new Date()));
		
		nsfyjzxgym.setOnClickListener(v -> DialogUtil.getListDialog(activity, "是否已接种新冠疫苗", new String[]{
				"未接种", "已接种第一针", "已接种第2针（未满6个月）", "已接种第2针（已满6个月）", "已接种第3针（加强针）", "已接种第3针（安徽智飞）"
		}, (dialog, itemView, position, text) -> {
			nsfyjzxgym.setText(text.toString());
			dialog.dismiss();
		}).show());
		
		nzhychsjcsj.setOnClickListener(v -> {
			final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
			
			final DatePicker picker = new DatePicker(activity);
			picker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
			
			DialogUtil.getBaseDialog(activity)
					.customView(picker,false)
					.onPositive((dialog, which) -> {
						c.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
						nzhychsjcsj.setText(DateUtil.YMD.format(c.getTime()));
						dialog.dismiss();
					}).show();
		});
		
		findViewById(R.id.fragment_health_checkin_login).setOnClickListener(v -> {
			final String user = nameText.getEditText().getText().toString();
			final String password = passwordText.getEditText().getText().toString();
			
			cookie = cookieText.getEditText().getText().toString();
			if(TextUtils.isEmpty(cookie)){
				cookie = null;
				if(TextUtils.isEmpty(user)){
					nameText.setError("请输入学号");
					return;
				}else{
					nameText.setError(null);
				}
				if(TextUtils.isEmpty(password)){
					passwordText.setError("请输入密码");
					return;
				}else{
					passwordText.setError(null);
				}
			}
			new Thread(){
				@Override
				public void run(){
					if(cookie == null){
						String[] tmp = getExecution();
						
						if(tmp == null){
							sendMessage(App.DISMISS_TOAST, "获取Execution失败！");
							return;
						}
						
						cookie = tmp[0];
						String execution = tmp[1];
						
						tmp = getPublicKey(cookie);
						
						if(tmp == null){
							sendMessage(App.DISMISS_TOAST, "获取RSA公钥失败！");
							return;
						}
						
						cookie += tmp[0];
						
						String publicKey = tmp[1];
						
						String rsaPwd = getRsaPassword(password, publicKey);
						
						cookie = login(cookie, execution, user, rsaPwd);
					}
					
					if(cookie == null){
						sendMessage(App.DISMISS_TOAST, "登录失败！");
						return;
					}
					
					SettingUtil.edit().putString(SettingUtil.HEALTH_CHECK_COOKIE, cookie).apply();
					
					if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(password)){
						SettingUtil.edit().putString(SettingUtil.HEALTH_CHECK_USER, user).putString(SettingUtil.HEALTH_CHECK_PASSWORD, password).apply();
					}
					
					String form = getForm(cookie, user);
					
					if(form == null){
						sendMessage(App.DISMISS_TOAST, "获取表单失败！");
						return;
					}else if("".equals(form)){
						sendMessage(App.DISMISS_TOAST, "今日已打卡！");
						return;
					}
					
					String msg = submitForm(cookie, form);
					
					if(msg == null){
						sendMessage(App.DISMISS_TOAST, "打卡成功！");
					}else{
						sendMessage(App.DISMISS_TOAST, msg);
					}
				}
			}.start();
			dialog.show();
		});
	}
	
	/**
	 * 获取 cookie 和 execution
	 */
	@Nullable
	private String[] getExecution(){
		try{

			HttpURLConnection connection = WebUtil.get("https://ydxg.qust.edu.cn/cas/login?service=https%3A%2F%2Fbpm.qust.edu.cn%2Fbpmx%2Fj_spring_cas_security_check", null, HEADERS);
			
			Matcher matcher = COOKIE_PATTERN.matcher(connection.getHeaderField("Set-Cookie"));
			
			if(!matcher.find()) return null;
			
			String cookie = matcher.group();
			
			matcher = PATTERN.matcher(WebUtil.inputStream2string(connection.getInputStream()));
			
			if(!matcher.find()) return null;
			
			return new String[]{ cookie, matcher.group(1) };

		}catch(IOException ignored){
			return null;
		}
	}
	
	/**
	 * 获取 RSA 公钥
	 */
	@Nullable
	private String[] getPublicKey(@NonNull String cookie){
		try{
			
			HttpURLConnection connection = WebUtil.get("https://ydxg.qust.edu.cn/cas/v2/getPubKey", cookie, HEADERS);
			
			String head = connection.getHeaderField("Set-Cookie");
			
			Matcher matcher = COOKIE_PATTERN.matcher(head);
			
			if(!matcher.find()){
				return null;
			}
			
			String response = WebUtil.inputStream2string(connection.getInputStream());

			return new String[]{matcher.group(), new JSONObject(response).getString("modulus")};
			
		}catch(IOException | JSONException ignored){
			return null;
		}
	}
	
	/**
	 * 获取 RSA 加密后的密码
	 */
	@Nullable
	private String getRsaPassword(@NonNull String password, @NonNull String modulus){
		try{
			BigInteger M = new BigInteger(password.getBytes(StandardCharsets.UTF_8));
			BigInteger n = new BigInteger(modulus, 16);
			BigInteger C = new BigInteger("1");
			int e = 65537;
			while(e != 0){
				if((e & 1) == 1){
					C = M.multiply(C).mod(n);
				}
				e = e >> 1;
				M = M.pow(2).mod(n);
			}
			return C.toString(16);
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 登陆
	 */
	@Nullable
	private String login(String cookie, String execution, String username, String rsaPassword){
		try{
			HttpURLConnection connection = WebUtil.post(
					"http://ydxg.qust.edu.cn/cas/login?service=https%3A%2F%2Fbpm.qust.edu.cn%2Fbpmx%2Fj_spring_cas_security_check", cookie,
					"username=" + username + "&password=" + rsaPassword + "&mobileCode=&authcode=&execution="+ URLEncoder.encode(execution, "UTF-8") + "&_eventId=submit", HEADERS);
			
			if(connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP){
				return null;
			}
			
			connection = WebUtil.get(connection.getHeaderField("Location"), null, HEADERS);
			
			Map<String, List<String>> header = connection.getHeaderFields();
			
			if(!header.containsKey("Set-Cookie")){
				return null;
			}
			
			StringBuilder newCookie = new StringBuilder();
			
			for(String head : header.get("Set-Cookie")){
				Matcher matcher = COOKIE_PATTERN.matcher(head);
				if(matcher.find()){
					newCookie.append(matcher.group());
				}
			}
			
			return newCookie.length() != 0 ? newCookie.toString() : null;
			
		}catch(IOException ignored){
			return null;
		}
	}
	
	@Nullable
	private JSONObject getHistoryData(@NonNull String cookie, @NonNull String username){
		try{
			return new JSONObject(WebUtil.doPost("https://bpm.qust.edu.cn/bpmx/platform/bpm/bpmFormQuery/doQuery.ht",
					cookie, "page=1&pagesize=1&alias=cxxsjkdkzhytjl&querydata=%7BF_XH%3A%22" + username + "%22%7D", HEADERS)
			).getJSONArray("list").getJSONObject(0);
		}catch(IOException | JSONException ignore){
			return null;
		}
	}
	
	@Nullable
	private JSONObject getInformation(@NonNull String cookie,@NonNull String username){
		try{
			return new JSONObject(WebUtil.doPost("https://bpm.qust.edu.cn/bpmx/platform/bpm/bpmFormQuery/doQuery.ht",
					cookie, "page=1&pagesize=1&alias=cxxsxx&querydata=%7BXH%3A%22" + username + "%22%7D", HEADERS))
					.getJSONArray("list").getJSONObject(0);
		}catch(IOException | JSONException ignore){
			return null;
		}
	}
	
	
	@Nullable
	private String getForm(String cookie, String username){
		
		try{
			JSONObject js = new JSONObject(WebUtil.doPost("https://bpm.qust.edu.cn/bpmx/platform/bpm/bpmFormQuery/doQuery.ht",
					cookie, "page=1&pagesize=1&alias=cxxsjtsfyjdk&querydata=%7B%22F_XH%22%3A+%22" + username + "%22%2C+%22F_TJSJ%22%3A+%22" + DateUtil.YMD.format(new Date()) + "%22%7D", HEADERS));
			
			if(js.getJSONArray("list").length() > 0){
				return "";
			}
			
			JSONObject info = getInformation(cookie, username);
			
			JSONObject history = getHistoryData(cookie, username);
			
			String response = WebUtil.doGet("https://bpm.qust.edu.cn/bpmx/platform/form/bpmDataTemplate/editData_xsjkdk.ht", cookie, HEADERS);
			
			String alias = "";
			
			String tableId = "";
			
			String pkField = "";
			
			String tableName = "";
			
			Matcher matcher = ALIAS.matcher(response);
			if(matcher.find()) alias = matcher.group(2).trim();
			
			matcher = TABLE_ID.matcher(response);
			if(matcher.find()) tableId = matcher.group(2).trim();
			
			matcher = PK_FIELD.matcher(response);
			if(matcher.find()) pkField = matcher.group(2).trim();
			
			matcher = TABLE_NAME.matcher(response);
			if(matcher.find()) tableName = matcher.group(2).trim();
			
			String time = DateUtil.YMD_HMS.format(new Date());
			
			return new StringBuilder()
					.append("m:xsjkdk:xm=").append(info.getString("XM"))
					.append("&m:xsjkdk:xh=").append(info.getString("XH"))
					.append("&m:xsjkdk:xy=").append(info.getString("XYMC"))
					.append("&m:xsjkdk:bj=").append(info.getString("BJMC"))
					.append("&m:xsjkdk:zy=").append(info.getString("ZYMC"))
					.append("&m:xsjkdk:nj=").append(info.getString("NJ"))
					.append("&m:xsjkdk:sjh=").append(info.getString("SJHM"))
					.append("&m:xsjkdk:tjsj=").append(time)
					.append("&m:xsjkdk:jssj=").append(time)
					.append("&m:xsjkdk:dqszdz=").append(history.getString("F_DQSZDZ"))
					.append("&m:xsjkdk:jrtw=").append(history.getString("F_JRTW"))
					.append("&m:xsjkdk:jrstzk=").append(history.getString("F_JRSTZK"))
					.append("&m:xsjkdk:stzkqt=").append(history.getString("F_STZKQT"))
					.append("&m:xsjkdk:gfxqyjcs=").append(history.getString("F_GFXQYJCS"))
					.append("&m:xsjkdk:ysbrjcs=").append(history.getString("F_YSBRJCS"))
					.append("&m:xsjkdk:ysbl=").append(history.getString("F_YSBL"))
					.append("&m:xsjkdk:yxgl=").append(history.getString("F_YXGL"))
					.append("&m:xsjkdk:jkmys=").append(history.getString("F_JKMYS"))
					.append("&m:xsjkdk:nlqsfybb=").append("未离青")
					.append("&m:xsjkdk:nsfyjzxgym=").append(nsfyjzxgym.getText().toString())
					.append("&m:xsjkdk:nzhychsjcsj=").append(nzhychsjcsj.getText().toString())
					.append("&m:xsjkdk:brcn=").append(history.getString("F_BRCN"))
					
					.append("&alias=").append(alias)
					.append("&tableId=").append(tableId)
					.append("&pkField=").append(pkField)
					.append("&tableName=").append(tableName)
					
					.append("&formData={\"main\":{\"fields\":{\"xm\":\"").append(info.getString("XM"))
					.append("\",\"xh\":\"").append(info.getString("XH"))
					.append("\",\"xy\":\"").append(info.getString("XYMC"))
					.append("\",\"bj\":\"").append(info.getString("BJMC"))
					.append("\",\"zy\":\"").append(info.getString("ZYMC"))
					.append("\",\"nj\":\"").append(info.getString("NJ"))
					.append("\",\"sjh\":\"").append(info.getString("SJHM"))
					.append("\",\"xydm\":\"").append(info.getString("XYDM"))
					.append("\",\"zydm\":\"").append(info.getString("ZYDM"))
					.append("\",\"bjdm\":\"").append(info.getString("BJDM"))
					.append("\",\"tjsj\":\"").append(time)
					.append("\",\"jssj\":\"").append(time)
					.append("\",\"dqszdz\":\"").append(history.getString("F_DQSZDZ"))
					.append("\",\"stzkqt\":\"").append(history.getString("F_STZKQT"))
					.append("\",\"jrtw\":\"").append(history.getString("F_JRTW"))
					.append("\",\"jrstzk\":\"").append(history.getString("F_JRSTZK"))
					.append("\",\"gfxqyjcs\":\"").append(history.getString("F_GFXQYJCS"))
					.append("\",\"ysbrjcs\":\"").append(history.getString("F_YSBRJCS"))
					.append("\",\"ysbl\":\"").append(history.getString("F_YSBL"))
					.append("\",\"yxgl\":\"").append(history.getString("F_YXGL"))
					.append("\",\"jkmys\":\"").append(history.getString("F_JKMYS"))
					.append("\",\"nlqsfybb\":\"").append("未离青")
					.append("\",\"nsfyjzxgym\":\"").append(nsfyjzxgym.getText().toString())
					.append("\",\"nzhychsjcsj\":\"").append(nzhychsjcsj.getText().toString())
					.append("\",\"brcn\":\"").append(history.getString("F_BRCN"))
					.append("\"}},\"sub\":[],\"opinion\":[]}")
					.toString();
		}catch(JSONException | IOException ignored){
			return null;
		}
	}
	
	
	/**
	 * 提交表单
	 * @return 提交成功为 null
	 */
	@Nullable
	private String submitForm(String cookie, String form){
		try{
			JSONObject js = new JSONObject(WebUtil.doPost("https://bpm.qust.edu.cn/bpmx/platform/form/bpmFormHandler/save.ht", cookie, form, HEADERS));
			if(js.getInt("result") == 1){
				return null;
			}else{
				return js.getString("message");
			}
		}catch(IOException | JSONException e){
			return "提交失败！" + e.getMessage();
		}

	}
	

	protected final void sendMessage(int code, String msg){
		handler.sendMessage(handler.obtainMessage(code, msg));
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.fragment_health_checkin;
	}
	
	@Override
	protected String getName(){
		return "健康打卡（已适配）";
	}
}