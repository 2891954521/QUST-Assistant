package com.qust.helper.entity.eas

import com.qust.helper.utils.DateUtils
import com.qust.helper.utils.JsonUtils.get
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Mark(
	val id: Int = 0,

	val kchId: String,
	val name: String,
	val type: String,

	val credit: String,
	val gpa: String,
	val mark: Float,

	val items: Array<Item> = emptyArray(),

	val index: Int,
	val time: LocalDateTime,
	val isNew: Int,
){

	companion object {
		val EMPTY_MARK = Mark(
			kchId = "",
			name = "",
			type = "",
			credit = "",
			gpa = "",
			mark = 0F,
			items = arrayOf(Item("成绩未公布或未查询过", "无")),
			index = 0,
			time = DateUtils.currentTime(),
			isNew = 0
		)
	}

	@Serializable
	data class Item(val name: String, val mark: String)


	class Builder {

		val kchId: String
		val name: String
		val type: String

		val credit: String
		var gpa: String = ""
		var mark: Float

		val time: LocalDateTime

		val items: ArrayList<Item> = ArrayList(4)

		constructor(js: JsonObject){
			kchId = js["kch_id", ""]
			name = js["kcmc", ""].trim()

			type = if(js.containsKey("ksxz")) js["ksxz", ""] else "正常考试"

			credit = js["xf", "0"]
			mark = if(js.containsKey("cj")) js["cj", "0.0"].toFloatOrNull() ?: 0F else 0F

			time = try {
				if(js.containsKey("tjsj")) DateUtils.YMD_HMS.parse(js["tjsj", ""]) else DateUtils.currentTime()
			} catch(_: Exception){
				DateUtils.currentTime()
			}

			calcGpa()
		}

		/**
		 * 添加一条成绩明细
		 */
		fun addItemMark(js: JsonObject) {
			if(!js.containsKey("xmblmc")) return
			val itemName = js["xmblmc", ""]
			if("总评" == itemName){
				if(mark == 0F && js.containsKey("xmcj")){
					mark = js["xmcj", ""].toFloatOrNull() ?: 0F
					calcGpa()
				}
			}else{
				items.add(Item(itemName, js["xmcj", ""]))
			}
		}

		private fun calcGpa(){
			gpa = if(mark < 60) "0"
			else if("正常考试" == type){
				((mark.toInt() - 50) / 10F).toString()
			}
			else "1"
		}

		fun build(index: Int = 0, new: Boolean = false): Mark{
			return Mark(
				kchId = kchId,
				name = name,
				type = type,
				credit = credit,
				gpa = gpa,
				mark = mark,
				items = items.toTypedArray(),
				index = index,
				time = time,
				isNew = if(new) 1 else 0
			)
		}
	}


	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(javaClass != other?.javaClass) return false

		other as Mark

		if(mark != other.mark) return false
		if(index != other.index) return false
		if(kchId != other.kchId) return false
		if(name != other.name) return false
		if(type != other.type) return false
		if(credit != other.credit) return false
		if(gpa != other.gpa) return false
		if(time != other.time) return false

		return true
	}

	override fun hashCode(): Int {
		var result = mark.hashCode()
		result = 31 * result + index
		result = 31 * result + kchId.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + credit.hashCode()
		result = 31 * result + gpa.hashCode()
		result = 31 * result + time.hashCode()
		return result
	}
}