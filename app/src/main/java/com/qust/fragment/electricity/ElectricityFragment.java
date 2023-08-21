package com.qust.fragment.electricity;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.account.RequestCallback;
import com.qust.account.RequestErrorCallback;
import com.qust.account.vpn.VpnViewModel;
import com.qust.assistant.R;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.FileUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.ParamUtil;
import com.qust.assistant.util.SettingUtil;
import com.qust.base.HandlerCode;
import com.qust.base.fragment.BaseFragment;
import com.qust.base.ui.FragmentActivity;
import com.qust.fragment.login.VpnLoginFragment;
import com.qust.widget.BottomDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ElectricityFragment extends BaseFragment implements RequestErrorCallback{
	
	private static final String APP_LIST = "http/77726476706e69737468656265737421f4f10f8d32237c1e7b0c9ce29b5b/web/NetWork/AppList.html";
	private static final String TSM = "http/77726476706e69737468656265737421f4f10f8d32237c1e7b0c9ce29b5b/web/Common/Tsm.html";
	private static final String PAY = "http/77726476706e69737468656265737421f4f10f8d32237c1e7b0c9ce29b5b/web/Elec/PayElecGdc.html";
	
	private static final String CARD_BALANCE = "http/77726476706e69737468656265737421f9b95089342426557a1dc7af96/tp_up/up/subgroup/queryCardBalance";
	
	/*
		POST /User/GetCardInfoByAccountNoParm HTTP/1.1
		Host: 211.87.155.92:8080
		json=true
	 */
	
	private static final Pattern SSO_TICKET_ID_PATTERN = Pattern.compile("id=\"ssoticketid\" value=\"([\\da-zA-Z]+)\"");
	
	private static final Pattern TICKET_PATTERN = Pattern.compile("\\?ticket=([\\da-zA-Z]+)");
	
	
	private static final String[] TITLE = {"电控", "校区", "楼栋", "楼层", "宿舍"};
	
	
	private static final String[][] PARAMS = {
			{"query_applist", 		"applist", 		"aid", 		"", 	"aid"},
			{"query_elec_area", 	"areatab",  	"area", 	"areaname", "area"},
			{"query_elec_building", "buildingtab", 	"building", "building", "buildingid"},
			{"query_elec_floor", 	"floortab", 	"floor", 	"floor", 	"floorid"},
			{"query_elec_room", 	"roomtab", 		"room", 	"room", 	"roomid"},
	};
	
	private static final String[] FUN_NAME = {
			"synjones.onecard.query.applist",
			"synjones.onecard.query.elec.area",
			"synjones.onecard.query.elec.building",
			"synjones.onecard.query.elec.floor",
			"synjones.onecard.query.elec.room",
	};
	
	private MaterialDialog dialog;
	
	private VpnViewModel vpnViewModel;
	
	private TextView accountText, balanceText;
	
	private String account;
	
	private ArrayList<Room> rooms;
	
	private Node currentNode;
	
	private Node nodes;
	
	private ArrayList<Integer> selectList;
	
	private boolean isSSOLogin;
	
	private Handler handler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(@NonNull Message msg){
			switch(msg.what){
				case HandlerCode.TOAST: {
					toast((String)msg.obj);
					break;
				}
				case HandlerCode.DISMISS_TOAST: {
					toast((String)msg.obj);
					dialog.dismiss();
					break;
				}
				case HandlerCode.UPDATE_DIALOG: {
					dialog.setContent((String)msg.obj);
					break;
				}
			}
		}
	};
	
	private BottomDialog bottomDialog;
	
	private RecyclerItemAdapter adapter;
	
	
	public ElectricityFragment(){ }
	
	public ElectricityFragment(boolean isRoot, boolean hasToolBar){
		super(isRoot, hasToolBar);
	}
	
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		
		nodes = new Node();
		selectList = new ArrayList<>();
		
		loadData();
		
		vpnViewModel = VpnViewModel.getInstance(activity);
		
		dialog = DialogUtil.getIndeterminateProgressDialog(activity, "请稍后").build();
		
		accountText = findViewById(R.id.electricity_account);
		balanceText = findViewById(R.id.electricity_balance);
		
		
		adapter = new RecyclerItemAdapter();
		RecyclerView selectedList = findViewById(R.id.electricity_room_list);
		selectedList.setLayoutManager(new LinearLayoutManager(activity));
		selectedList.setAdapter(adapter);
		
		bottomDialog = new BottomDialog(activity,
				findViewById(R.id.electricity_choose_back),
				findViewById(R.id.electricity_choose),
				0.5f
		);
		
		addMenuItem(inflater, R.drawable.ic_add, this::showChooseDialog);
		
		findViewById(R.id.electricity_aid).setOnClickListener(v -> doChoose((TextView)v, 0));
		findViewById(R.id.electricity_area).setOnClickListener(v -> doChoose((TextView)v, 1));
		findViewById(R.id.electricity_building).setOnClickListener(v -> doChoose((TextView)v, 2));
		findViewById(R.id.electricity_floor).setOnClickListener(v -> doChoose((TextView)v, 3));
		findViewById(R.id.electricity_room).setOnClickListener(v -> doChoose((TextView)v, 4));
		
		findViewById(R.id.electricity_cancel).setOnClickListener(v -> bottomDialog.hide());
		findViewById(R.id.electricity_done).setOnClickListener(v -> {
			if(selectList.size() < 5){
				toast("请选择所有选项");
			}else{
				Node node = nodes;
				Room room = new Room();
				room.id = new String[selectList.size()];
				room.name = new String[selectList.size()];
				for(int i = 0; i < room.id.length; i++){
					node = node.child.get(selectList.get(i));
					room.id[i] = node.nodeId;
					room.name[i] = node.name;
				}
				room.roomName = node.name;
				rooms.add(room);
				saveData();
				adapter.notifyItemInserted(rooms.size());
				bottomDialog.hide();
			}

		});
		
		getCardInfo();
	}
	
	private void getCardInfo(){
		dialog.setContent("正在获取信息");
		dialog.show();
		
		post(TSM, "{\"query_card\":{\"idtype\":\"sno\",\"id\":\"" + SettingUtil.get(getString(R.string.VPN_NAME), "") + "\"}}", "synjones.onecard.query.card", (response, html) -> {
			try{
				JSONObject js = new JSONObject(html);
				JSONArray cards = js.getJSONObject("query_card").getJSONArray("card");
				
				if(cards.length() == 0){
					handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, "用户没有绑卡，无法使用该功能"));
					return;
				}else if(cards.length() > 1){
					handler.sendMessage(handler.obtainMessage(HandlerCode.TOAST, "用户有多张卡，默认使用第一张"));
				}
				
				account = cards.getJSONObject(0).getString("account");
				
				try(Response resp = vpnViewModel.postWithOutCheck(CARD_BALANCE, RequestBody.create("{}", MediaType.parse("application/json")))){
					float balance = Float.parseFloat(resp.body().string());
					activity.runOnUiThread(() -> {
						accountText.setText(account);
						balanceText.setText(String.valueOf(balance));
						dialog.dismiss();
					});
				}
			}catch(JSONException e){
				handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, "获取卡信息失败"));
			}
		});
	}
	
	/**
	 * 打开添加宿舍的窗口
	 */
	private void showChooseDialog(View v){
		bottomDialog.show();
		
		if(nodes.child != null) return;
		
		// 加载首级数据
		dialog.setContent("正在获取电控信息");
		dialog.show();
		post(APP_LIST, "{\"query_applist\":{\"apptype\":\"elec\"}}", "synjones.onecard.query.applist", (response, html) -> activity.runOnUiThread(() -> {
			try{
				JSONObject js = new JSONObject(html);
				JSONArray appList = js.getJSONObject("query_applist").getJSONArray("applist");
				ArrayList<Node> nodeAid = new ArrayList<>(appList.length());
				for(int i = 0; i < appList.length(); i++){
					JSONObject obj = appList.getJSONObject(i);
					Node node = new Node();
					node.name = obj.getString("name");
					node.nodeId = obj.getString("aid");
					nodeAid.add(node);
				}
				nodes.child = nodeAid;
			}catch(NullPointerException | JSONException e){
				toast("获取电控信息失败: " + e.getMessage());
			}finally{
				dialog.dismiss();
			}
		}));
	}
	
	/**
	 * 每一级的选择
	 */
	private void doChoose(TextView textView, int index){
		if(index > selectList.size()){
			toast("请先选择上一级");
			return;
		}
		
		// 定位到这一级
		ArrayList<Node> list = nodes.child;
		for(int i = 0; i < index; i++){
			list = list.get(selectList.get(i)).child;
		}
		
		// 获取这一级的所有选项的名字
		int len = list == null ? 0 : list.size();
		String[] name = new String[len];
		for(int i = 0; i < len; i++) name[i] = list.get(i).name;
		
		ArrayList<Node> finalList = list;
		DialogUtil.getListDialog(activity, TITLE[index], name, (dialog, itemView, position, text) -> {
			selectList.subList(index, selectList.size()).clear();
			selectList.add(position);
			textView.setText(text);
			if(selectList.size() < 5){
				currentNode = finalList.get(position);
				if(currentNode.child == null) queryData();
			}
		}).show();
	}
	
	
	/**
	 * 异步查询信息
	 */
	private void queryData(){
		dialog.setContent("正在查询" + TITLE[selectList.size()]);
		dialog.show();

		try{
			Node node = nodes.child.get(selectList.get(0));
			JSONObject query = new JSONObject().put("account", account).put("aid", node.nodeId);
			int index = selectList.size();
			for(int i = 1; i < index; i++){
				node = node.child.get(selectList.get(i));
				query.put(PARAMS[i][2], new JSONObject().put(PARAMS[i][3], node.name).put(PARAMS[i][4], node.nodeId));
			}
			
			post(TSM, new JSONObject().put(PARAMS[index][0], query).toString(), FUN_NAME[selectList.size()], (response, html) -> {
				try{
					JSONObject js = new JSONObject(html);
					String name = PARAMS[index][3];
					String id = PARAMS[index][4];
					JSONArray array = js.getJSONObject(PARAMS[index][0]).getJSONArray(PARAMS[index][1]);
					ArrayList<Node> nodeAid = new ArrayList<>(array.length());
					for(int i = 0; i < array.length(); i++){
						JSONObject obj = array.getJSONObject(i);
						Node node1 = new Node();
						node1.name = obj.getString(name);
						node1.nodeId = obj.getString(id);
						nodeAid.add(node1);
					}
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
						nodeAid.sort(Comparator.comparing(o -> o.name));
					}
					currentNode.child = nodeAid;

				}catch(NullPointerException | JSONException e){
					activity.runOnUiThread(() -> toast("获取" + TITLE[selectList.size()] + "信息失败: " + e.getMessage()));
				}finally{
					activity.runOnUiThread(() -> dialog.dismiss());
				}
			});
		}catch(JSONException e){
			throw new RuntimeException(e);
		}
	}
	
	private void recharge(Room room, int f){
		dialog.setContent("正在充值");
		dialog.show();
		
		new Thread(){
			@Override
			public void run(){
				if(!isSSOLogin){
					try{
						String ticket;
						
						Response response = vpnViewModel.getWithOutCheck("http-7280/77726476706e69737468656265737421a2a610d27f6726012b5dc7f4c8/ias/prelogin?sysid=FWDT");
						Matcher matcher = SSO_TICKET_ID_PATTERN.matcher(response.body().string());
						if(!matcher.find()) throw new IOException("单点登陆失败，无法获取ticket");
						ticket = matcher.group(1);
						response.close();
						
						handler.sendMessage(handler.obtainMessage(HandlerCode.UPDATE_DIALOG, "正在进行单点登录"));
						vpnViewModel.postWithOutCheck("http-8080/77726476706e69737468656265737421a2a610d27f6726012b5dc7f5ca/cassyno/index",
								new FormBody.Builder().add("errorcode", "1").add("continueurl", "").add("ssoticketid", ticket).build()
						).close();
						
						handler.sendMessage(handler.obtainMessage(HandlerCode.UPDATE_DIALOG, "正在登录到电费平台"));
						response = vpnViewModel.postWithOutCheck("http-8080/77726476706e69737468656265737421a2a610d27f6726012b5dc7f5ca/Page/Page",
								new FormBody.Builder().add("flowID", "151").add("type", "1").add("apptype", "4").add("Url", "http%3a%2f%2fdf.qust.edu.cn%2fweb%2fcommon%2fcheckEle.html").build());
						
						Matcher matcher2 = TICKET_PATTERN.matcher(response.body().string());
						if(!matcher2.find()) throw new RuntimeException("登陆失败，无法获取ticket");
						ticket = matcher2.group(1);
						response.close();
						
						vpnViewModel.getWithOutCheck("http/77726476706e69737468656265737421f4f10f8d32237c1e7b0c9ce29b5b/web/common/checkEle.html?ticket=" + ticket).close();
						
						isSSOLogin = true;
					}catch(IOException e){
						onNetworkError(e);
						isSSOLogin = false;
						return;
					}
				}
				
				handler.sendMessage(handler.obtainMessage(HandlerCode.UPDATE_DIALOG, "正在充值"));
				
				try(Response response = vpnViewModel.postWithOutCheck(PAY, new FormBody.Builder().add("acctype", "###").add("json", "true")
						.add("paytype", "1").add("qpwd", "")
						.add("account", account).add("tran", String.valueOf(f))
						.add("aid", room.id[0])
						.add("roomid", room.id[4]).add("room", room.name[4])
						.build())){
					
					JSONObject js = new JSONObject(response.body().string()).getJSONObject("pay_elec_gdc");
					handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, js.getString("errmsg")));
					
				}catch(IOException | JSONException e){
					onNetworkError(e);
				}
			}
		}.start();
	}
	
	/**
	 * 异步POST
	 */
	private void post(String url, String payload, String funName, RequestCallback callback){
		vpnViewModel.post(url,
				new FormBody.Builder()
						.add("json", "true")
						.add("jsondata", payload)
						.add("funname", funName).build(),
				callback, this);
	}
	
	
	private void loadData(){
		try{
			rooms = (ArrayList<Room>)FileUtil.loadData(new File(activity.getExternalFilesDir("elec"), "rooms"));
		}catch(Exception e){
			LogUtil.Log(e, false);
			rooms = new ArrayList<>();
		}
	}
	
	protected void saveData(){
		try{
			FileUtil.saveData(new File(activity.getExternalFilesDir("elec"), "rooms"), rooms);
		}catch(Exception e){
			LogUtil.Log(e, false);
		}
	}
	
	@Override
	protected int getLayoutId(){ return R.layout.fragment_electricity; }
	
	@Override
	public String getName(){ return "电费充值"; }
	
	private class RecyclerItemAdapter extends RecyclerView.Adapter<RoomViewHolder>{
		
		@NonNull
		@Override
		public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			View itemView = LayoutInflater.from(activity).inflate(R.layout.item_electricity_room, parent, false);
			RoomViewHolder holder = new RoomViewHolder(itemView);
			itemView.setOnClickListener(v -> {
			
			});
			return holder;
		}
		
		@Override
		public void onBindViewHolder(@NonNull RoomViewHolder holder, int position){
			holder.onBind(rooms.get(position));
		}
		
		@Override
		public int getItemCount(){
			return rooms.size();
		}
	}
	
	private class RoomViewHolder extends RecyclerView.ViewHolder{
		
		private TextView name, balance;
		
		private View refreshAnim;
		private Room room;
		
		public RoomViewHolder(@NonNull View convertView){
			super(convertView);
			name = convertView.findViewById(R.id.item_electricity_roomName);
			balance = convertView.findViewById(R.id.item_electricity_balance);

			refreshAnim = convertView.findViewById(R.id.item_electricity_refresh_anim);
			convertView.findViewById(R.id.item_electricity_add).setOnClickListener(this::recharge);
			convertView.findViewById(R.id.item_electricity_delete).setOnClickListener(this::delete);
			convertView.findViewById(R.id.item_electricity_refresh).setOnClickListener(this::refresh);
		}
		
		public void onBind(@NonNull Room room){
			this.room = room;
			name.setText(room.roomName);
			balance.setText(String.valueOf(room.balance));
		}
		
		public void delete(View v){
			DialogUtil.getBaseDialog(activity).title("删除").content("确认删除宿舍: " + room.roomName).onPositive((dialog, which) -> {
				int index = getAdapterPosition();
				rooms.remove(index);
				adapter.notifyItemRemoved(index);
				saveData();
			}).show();
		}
		
		public void refresh(View v){
			Animation rotateAnimation  = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setDuration(1000);
			rotateAnimation.setRepeatCount(Animation.INFINITE);
			rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
			refreshAnim.startAnimation(rotateAnimation);
			
			try{
				JSONObject query = new JSONObject().put("account", account).put("aid", room.id[0]).put("extdata", "info1=");
				for(int i = 1; i < room.id.length; i++){
					query.put(PARAMS[i][2], new JSONObject().put(PARAMS[i][3], room.name[i]).put(PARAMS[i][4], room.id[i]));
				}
				post(TSM, new JSONObject().put("query_elec_roominfo", query).toString(), "synjones.onecard.query.elec.roominfo", (response, html) -> {
					float val = Float.NaN;
					try{
						JSONObject js = new JSONObject(html).getJSONObject("query_elec_roominfo");
						Matcher matcher = ParamUtil.FLOAT_PATTERN.matcher(js.getString("errmsg"));
						if(matcher.find()) val = Float.parseFloat(matcher.group());
					}catch(JSONException | NumberFormatException e){
						LogUtil.Log(e, false);
					}
					room.balance = val;
					saveData();
					activity.runOnUiThread(() -> {
						if(Float.isNaN(room.balance)) toast("查询失败");
						balance.setText(String.valueOf(room.balance));
						refreshAnim.clearAnimation();
					});
				});
			}catch(JSONException e){
				toast("查询失败");
				refreshAnim.clearAnimation();
			}
		}
		
		public void recharge(View v){
			DialogUtil.getBaseDialog(activity).title("充值").input("请输入金额，单位（元）", "", false, (dialog, input) -> {
			
			}).onPositive((d, which) -> {
				int f;
				try{
					String text = d.getInputEditText().getText().toString();
					if(ParamUtil.isFloat(text)){
						f = (int)(Float.parseFloat(text) * 100);
					}else{
						throw new NumberFormatException();
					}
				}catch(NumberFormatException e){
					toast("请输入正确的金额");
					return;
				}
				
				d.dismiss();
				
				ElectricityFragment.this.recharge(room, f);
			}).show();
		}
	}
	
	@Override
	public void onNeedLogin(){
		activity.runOnUiThread(() -> {
			dialog.dismiss();
			activity.toastWarning("请先登录");
			FragmentActivity.startActivity(activity, VpnLoginFragment.class);
		});
	}
	
	@Override
	public void onNetworkError(@NonNull Exception e){
		handler.sendMessage(handler.obtainMessage(HandlerCode.DISMISS_TOAST, e.getMessage()));
	}
	
	public static class Room implements Serializable{
		
		private static final long serialVersionUID = 1L;
		
		public String[] id;
		public String[] name;
		
		public String roomName;
		
		public float balance;
		
	}
	
	public static class Node implements Serializable{
		
		private static final long serialVersionUID = 1L;
		
		public String nodeId;
		
		public String name;
		
		public ArrayList<Node> child;
	}
	
}
