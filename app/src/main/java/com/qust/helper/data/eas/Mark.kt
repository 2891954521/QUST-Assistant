package com.qust.helper.data.eas

import org.json.JSONObject
import java.util.Locale
import kotlinx.serialization.Serializable

/**
 * 课程成绩
 * @param name   课程名称
 * @param credit 学分
 * @param type   考试类型
 * @param mark   成绩
 * @param items  成绩明细名称
 * @param itemMarks 成绩明细分数
 */
@Serializable
class Mark(
	val name: String = "",
	val credit: String = "",
	val type: String = "",
	var mark: Float = 0F,
	var gpa: String = "",
	val items: ArrayList<String> = ArrayList(4),
	val itemMarks: ArrayList<String> = ArrayList(4)
) {

	init {
		gpa =
			if(mark < 60) "0"
			else if("正常考试" == type) String.format(Locale.CHINA, "%.2f",mark / 10 - 5)
			else "1"
	}

	/**
	 * 添加一条成绩明细
	 */
	fun addItemMark(js: JSONObject) {
		if(!js.has("xmblmc")) return
		val itemName = js.getString("xmblmc")
		if("总评" == itemName){
			if(mark == 0F && js.has("xmcj")){
				mark = js.getString("xmcj").toFloatOrNull() ?: 0F
				gpa =
					if(mark < 60) "0"
					else if("正常考试" == type) String.format(Locale.CHINA, "%.2f",mark / 10 - 5)
					else "1"
			}
		}else{
			items.add(itemName)
			itemMarks.add(if(js.has("xmcj")) js.getString("xmcj") else "")
		}
	}

	companion object {
		/**
		 * 解析 js 为 mark 对象
		 */
		fun createFromJson(js: JSONObject): Mark {
			return Mark(
				name = js.getString("kcmc").trim(),
				credit = js.getString("xf"),
				type = if(js.has("ksxz")) js.getString("ksxz") else "正常考试",
				mark = if(js.has("cj")) js.getString("cj").toFloatOrNull() ?: 0F else 0F,
			)
		}
	}
}