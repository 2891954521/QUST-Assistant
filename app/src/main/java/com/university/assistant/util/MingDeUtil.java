package com.university.assistant.util;

public class MingDeUtil{
	
	private enum Control{
		// 外侧
		OUTSIDE,// 内侧
		INSIDE,// 左
	}
	
	public static final String STAIRS = "STAIRS";
	
	public static final String WC = "WC";
	
	public static final String BLOCK = "BLOCK";
	
	public static final String EMPTY = "EMPTY";
	
	private static final String[][] MING_DE_MAP = {
			// 1F 0,1
			{"一号门",EMPTY,EMPTY,"二号门",EMPTY,"弘毅楼","出口",EMPTY,EMPTY,EMPTY,"三号门",EMPTY,"学院楼",EMPTY,"出口",EMPTY,EMPTY,"四号门",EMPTY,EMPTY,"五号门"},
			{STAIRS,BLOCK,BLOCK,STAIRS,BLOCK,BLOCK,STAIRS,BLOCK,BLOCK,BLOCK,STAIRS,BLOCK,BLOCK,BLOCK,STAIRS,BLOCK,BLOCK,STAIRS,BLOCK,BLOCK,STAIRS},
			// 2F 2,3
			{BLOCK,"202",WC,"204","206",BLOCK,"212","214","216","220","222",BLOCK,BLOCK,"228","230","232","234","236","238","240","242"},
			{STAIRS,"201","203",STAIRS,"205",BLOCK,STAIRS,WC,"215","217",STAIRS,BLOCK,BLOCK,"227",STAIRS,"233",WC,STAIRS,"239","241",STAIRS},
			// 3F 4,5
			{"302",WC,"304","306","308","310","312","314","316","320","322","324",WC,"328","330","332","334","336","338","340","342"},
			{STAIRS,"301","303",STAIRS,"305","307",STAIRS,WC,"315","317",STAIRS,"323","325","327",STAIRS,"333",WC,STAIRS,"339","341",STAIRS},
			// 4F 6,7
			{"402",WC,"404","406","408","410","412","414","416","420","422","424",WC,"428","430","432","434","436","438","440","442"},
			{STAIRS,"401","403",STAIRS,"405","407",STAIRS,WC,"415","417",STAIRS,"423","425","427",STAIRS,"433",WC,STAIRS,"439","441",STAIRS},
			// 5F 8,9
			{"502",WC,"504","506","508","510","512","514","516","520","522","524",WC,"528","530","532","534","536","538","540","542"},
			{STAIRS,"501","503",STAIRS,"505","507",STAIRS,WC,"515","517",STAIRS,"523","525","527",STAIRS,"533",WC,STAIRS,"539","541",STAIRS},
			// 6F 10,11
			{"602",WC,"604","606",BLOCK,BLOCK,"612","614","616","620","622","624",WC,"628","630","632","634","636","638","640","642"},
			{STAIRS,"601","603",STAIRS,"605","607",STAIRS,WC,"615","617",STAIRS,"623","625","627",STAIRS,"633",WC,STAIRS,"639","641",STAIRS},
	};
	
	public static String class2class(String start,String destination){
		return find(new Class(start.substring(start.length() - 3)),new Class(destination.substring(destination.length() - 3)));
	}
	
	public static String gate2class(String start,String destination){
		return find(new Stairs(0,findIndex(0,Control.OUTSIDE,start),Control.OUTSIDE),new Class(destination.substring(destination.length() - 3)));
	}
	
	private static String find(Place place,Place destination){
		
		if(place.floor==-1 || destination.floor==-1){
			return "楼层不存在!";
		}
		
		if(place.index==-1 || destination.index==-1){
			return "房间不存在!";
		}
		
		String s = "";
		
		if(destination.floor==2){
			// 都在二楼的情况
			if(place.floor==2){
				if(isConnected(place,destination)){
					// 两教室可以直达
					s = move(s,place.index,place.side,destination);
				}else{
					// 两教室无法直达，从三楼绕
					Stairs stairs = findStairs(place);
					
					if(stairs==null) return s;
					
					if(place.index==stairs.index){
						s += "出门进楼梯,";
					}else{
						if(place.index >= stairs.index){
							if(place.side==Control.OUTSIDE) s += "出门右转,";
							else s += "出门左转,";
						}else{
							if(place.side==Control.OUTSIDE) s += "出门左转,";
							else s += "出门右转,";
						}
						s += "直走过" + Math.abs(place.index - stairs.index) + "个教室到楼梯口,";
					}
					s += "上一楼,";
					stairs.floor = 3;
					s = goto2floor(s,stairs,destination);
				}
			}else{
				s = goto2floor(s,place,destination);
			}
			return s;
		}
		
		Control side = place.side;
		
		int currentIndex = place.index;
		
		// 不在同一层先移动到同一层
		if(place.floor!=destination.floor){
			
			if(side==Control.OUTSIDE && STAIRS.equals(MING_DE_MAP[place.floor + 1][currentIndex])){
				s += "出门进楼梯,";
			}else if(side==Control.INSIDE && STAIRS.equals(MING_DE_MAP[place.floor][currentIndex])){
				s += "出门进楼梯,";
			}else if(currentIndex >= destination.index){
				if(side==Control.OUTSIDE) s += "出门右转,";
				else s += "出门左转,";
				// 找楼梯
				for(int i = currentIndex;i>-1;i--){
					if(STAIRS.equals(MING_DE_MAP[place.floor][i])){
						side = Control.OUTSIDE;
						currentIndex = i;
						if(place.floor==0){
							s += "直走到" + MING_DE_MAP[0][i] + ",";
						}else s += "直走过" + Math.abs(place.index - i) + "个教室,右转到楼梯口,";
						break;
					}else if(STAIRS.equals(MING_DE_MAP[place.floor + 1][i])){
						side = Control.INSIDE;
						currentIndex = i;
						if(place.floor==0){
							s += "直走到" + MING_DE_MAP[0][i] + ",";
						}else s += "直走过" + Math.abs(place.index - i) + "个教室,左转到楼梯口,";
						break;
					}
				}
			}else{
				if(side==Control.OUTSIDE) s += "出门左转,";
				else s += "出门右转,";
				for(int i = currentIndex;i<MING_DE_MAP[place.floor].length;i++){
					if(STAIRS.equals(MING_DE_MAP[place.floor][i])){
						side = Control.OUTSIDE;
						currentIndex = i;
						if(place.floor==0){
							s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
						}else s += "直走过" + Math.abs(place.index - i) + "个教室,左转到楼梯口,";
						break;
					}else if(STAIRS.equals(MING_DE_MAP[place.floor + 1][i])){
						side = Control.INSIDE;
						currentIndex = i;
						if(place.floor==0){
							s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
						}else s += "直走过" + Math.abs(place.index - i) + "个教室,右转到楼梯口,";
						break;
					}
				}
			}
			
			if(place.floor>destination.floor){
				s += "下" + (place.floor - destination.floor) / 2 + "层,";
			}else{
				s += "上" + (destination.floor - place.floor) / 2 + "层,";
			}
		}
		
		s = move(s,currentIndex,side,destination);
		
		return s;
	}
	
	// 从一个非二楼的点导航到二楼某地
	private static String goto2floor(String s,Place place,Place destination){
		
		Stairs stairs = findStairs(destination);
		
		if(stairs==null) return s;
		
		int currentIndex = place.index;
		
		if(currentIndex==stairs.index){
			
			s += "进楼梯,";
			
		}else{
			if(currentIndex >= stairs.index){
				if(place.side==Control.OUTSIDE) s += "出门右转,";
				else s += "出门左转,";
			}else{
				if(place.side==Control.OUTSIDE) s += "出门左转,";
				else s += "出门右转,";
			}
			if(place.floor==0){
				s += "直走到" + MING_DE_MAP[0][stairs.index] + ",";
			}else{
				s += "直走过" + Math.abs(currentIndex - stairs.index) + "个教室到楼梯口,";
			}
			
			currentIndex = stairs.index;
		}
		
		if(place.floor>2){
			s += "下" + (place.floor - destination.floor) / 2 + "层,";
		}else{
			s += "上" + (destination.floor - place.floor) / 2 + "层,";
		}
		
		s = move(s,currentIndex,stairs.side,destination);
		return s;
	}
	
	// 检查同层两点是否连同
	private static boolean isConnected(Place place,Place destination){
		if(place.index==destination.index){
			return true;
		}else if(place.index<destination.index){
			for(int i = place.index;i<destination.index;i++){
				if(BLOCK.equals(MING_DE_MAP[place.floor][i]) && BLOCK.equals(MING_DE_MAP[place.floor + 1][i])){
					return false;
				}
			}
		}else{
			for(int i = destination.index;i<place.index;i++){
				if(BLOCK.equals(MING_DE_MAP[place.floor][i]) && BLOCK.equals(MING_DE_MAP[place.floor + 1][i])){
					return false;
				}
			}
		}
		return true;
	}
	
	// 同层移动
	private static String move(String s,int index,Control side,Place destination){
		if(index==destination.index){
			s += "对面就是";
		}else if(index>destination.index){
			if(side==Control.OUTSIDE) s += "右转,";
			else s += "左转,";
			// 找房间
			for(int i = index;i>-1;i--){
				if(destination.name.equals(MING_DE_MAP[destination.floor][i])){
					if(destination.floor==0){
						s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
					}else s += "直走过" + (index - i) + "个教室,右转,";
					break;
				}else if(destination.name.equals(MING_DE_MAP[destination.floor + 1][i])){
					if(destination.floor==0){
						s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
					}else s += "直走过" + (index - i) + "个教室,左转,";
					break;
				}
			}
		}else{
			if(side==Control.OUTSIDE) s += "左转,";
			else s += "右转,";
			for(int i = index;i<MING_DE_MAP[destination.floor].length;i++){
				if(destination.name.equals(MING_DE_MAP[destination.floor][i])){
					if(destination.floor==0){
						s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
					}else s += "直走过" + (i - index) + "个教室,左转";
					break;
				}else if(destination.name.equals(MING_DE_MAP[destination.floor + 1][i])){
					if(destination.floor==0){
						s += "直走到" + MING_DE_MAP[0][destination.index] + ",";
					}else s += "直走过" + (i - index) + "个教室,右转";
					break;
				}
			}
		}
		return s;
	}
	
	// 给定一个地点找最近的楼梯
	private static Stairs findStairs(Place place){
		int i = 0;
		int b = 0;
		while(b!=2){
			b = 0;
			if(place.index - i>-1){
				if(STAIRS.equals(MING_DE_MAP[place.floor][place.index - i])){
					return new Stairs(place.floor,place.index - i,Control.OUTSIDE);
				}else if(STAIRS.equals(MING_DE_MAP[place.floor + 1][place.index - i])){
					return new Stairs(place.floor,place.index - i,Control.INSIDE);
				}
			}else b++;
			
			if(place.index + i<MING_DE_MAP[place.floor].length){
				if(STAIRS.equals(MING_DE_MAP[place.floor][place.index + i])){
					return new Stairs(place.floor,place.index + i,Control.OUTSIDE);
				}else if(STAIRS.equals(MING_DE_MAP[place.floor + 1][place.index + i])){
					return new Stairs(place.floor,place.index + i,Control.INSIDE);
				}
			}else b++;
			
			i++;
		}
		return null;
	}
	
	// 根据名称查找位置索引
	private static int findIndex(int floor,Control control,String place){
		if(control==Control.INSIDE) floor += 1;
		for(int i = 0;i<MING_DE_MAP[floor].length;i++){
			if(MING_DE_MAP[floor][i].equals(place)){
				return i;
			}
		}
		return -1;
	}
	
	// 教室类
	private static class Class extends Place{
		public Class(String name){
			this.name = name;
			floor = 2 * (name.charAt(0) - 48) - 2;
			if(floor<0 || floor>MING_DE_MAP.length) floor = -1;
			side = Control.INSIDE;
			index = findIndex(floor,side,name);
			if(index==-1){
				side = Control.OUTSIDE;
				index = findIndex(floor,side,name);
			}
		}
	}
	
	// 楼梯类
	private static class Stairs extends Place{
		public Stairs(int floor,int index,Control side){
			this.name = STAIRS;
			this.floor = floor;
			this.index = index;
			this.side = side;
		}
	}
	
	// 地点类
	private static class Place{
		
		public int floor;
		public int index;
		
		public String name;
		
		public Control side;
		
		public Place(){
			floor = -1;
			index = -1;
			name = "";
		}
	}
	
}
