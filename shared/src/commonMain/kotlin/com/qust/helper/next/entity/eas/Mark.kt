package com.qust.helper.next.entity.eas

import androidx.compose.runtime.Immutable
import com.qust.helper.next.common.json.get
import com.qust.helper.next.utils.DateUtils
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Immutable
data class Mark(
	val id: Int = 0,

	val kchId: String = "",
	val name: String = "",
	val type: String = "",

	val credit: String = "",
	val gpa: String = "",
	val mark: Float = 0F,

	val items: List<Item> = emptyList(),

	val term: Int = 0,
	val time: LocalDateTime = DateUtils.currentTime(),
	val isNew: Int = 1,
) {

	@Serializable
	data class Item(val name: String, val mark: String)

	class Builder(private val js: JsonObject) {

		val kchId: String = js["kch_id", ""]
		val name: String = js["kcmc", ""].trim()
		val type: String = if(js.containsKey("ksxz")) js["ksxz", ""] else "正常考试"
		val credit: String = js["xf", "0"]
		var gpa: String = ""
		var mark: Float = if(js.containsKey("cj")) js["cj", "0.0"].toFloatOrNull() ?: 0F else 0F
		val time: LocalDateTime = try {
			if(js.containsKey("tjsj")) DateUtils.YMD_HMS.parse(js["tjsj", ""]) else DateUtils.currentTime()
		} catch(_: Exception) {
			DateUtils.currentTime()
		}

		val items: ArrayList<Item> = ArrayList(4)

		init {
			calcGpa()
		}

		fun addItemMark(js: JsonObject) {
			if(!js.containsKey("xmblmc")) return
			val itemName = js["xmblmc", ""]
			if("总评" == itemName) {
				if(mark == 0F && js.containsKey("xmcj")) {
					mark = js["xmcj", ""].toFloatOrNull() ?: 0F
					calcGpa()
				}
			} else {
				items.add(Item(itemName, js["xmcj", ""]))
			}
		}

		private fun calcGpa() {
			gpa = if(mark < 60) "0"
			else if("正常考试" == type) {
				((mark.toInt() - 50) / 10F).toString()
			} else "1"
		}

		fun build(term: Int, new: Boolean = false): Mark = Mark(
			kchId = kchId,
			name = name,
			type = type,
			credit = credit,
			gpa = gpa,
			mark = mark,
			items = items.toList(),
			term = term,
			time = time,
			isNew = if(new) 1 else 0,
		)
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(other !is Mark) return false

		if(mark != other.mark) return false
		if(term != other.term) return false
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
		result = 31 * result + term
		result = 31 * result + kchId.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + credit.hashCode()
		result = 31 * result + gpa.hashCode()
		result = 31 * result + time.hashCode()
		return result
	}
}
