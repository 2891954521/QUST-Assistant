package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.eas.Mark
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.MarkModel
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK

class QueryMarkViewModel : BaseEasViewModel() {

	var marks = emptyList<Mark>().toMutableStateList()

	var update by mutableStateOf(false)

	private val marksData: MutableList<MutableList<Mark>?> = MutableList(Strings.ARRAY_TERM_NAME.size) { null }

	private var sortBy: Int = 0
	private var sortType: Int = 1

	fun getTerm(): String {
		val index = pickYear.intValue
		return Strings.ARRAY_TERM_NAME[index]
	}

	fun query() {
		request({
			val index = pickYear.intValue
			val pair = getPickTerm()
			val resultData = sort(MarkModel.queryMarks(EasAccount, index, pair.first, pair.second).toMutableList())

			if(marksData[index] == null) {
				marksData[index] = MarkModel.getMarksByIndex(index).toMutableList()
			}

			val origData = marksData[index] ?: mutableListOf()

			if(origData.isEmpty()) {
				MarkModel.insertAll(resultData)
				setMark(index, resultData)
				toastOK("查询完成")
				return@request
			}

			val origin = origData.toSet()
			val result = resultData.toSet()

			val old = origin subtract result
			val new = result subtract origin
			val update = origin intersect result
			if(new.isNotEmpty() || update.isNotEmpty()) {
				MarkModel.updateAll(update.toList())
				MarkModel.insertAll(new.toList())
				setMark(index, (old + update + new).toMutableList())
				toastOK("查询完成，查询到 ${new.size} 门新成绩")
			}else{
				toastOK("查询完成，未查询到新成绩")
			}
		}, {
			it.printStackTrace()
			toastError("查询失败：${it.message}")
		})
	}

	fun selectData(index: Int) {
		val data = marksData[index]
		if(data != null) {
			marks.clear()
			marks.addAll(data)
		} else {
			request({
				setMark(index, sort(MarkModel.getMarksByIndex(index).toMutableList()))
			})
		}
	}

	fun clearMarks() {
		val index = pickYear.intValue
		request({
			MarkModel.clear(index)
			setMark(index, mutableListOf())
			toastOK("清空完成")
		})
	}

	fun setSortBy(index: Int) {
		sortBy = index
		val data = marksData[pickYear.intValue] ?: return
		setMark(pickYear.intValue, sort(data))
	}

	fun setSortType(index: Int) {
		sortType = index
		val data = marksData[pickYear.intValue] ?: return
		setMark(pickYear.intValue, sort(data))
	}

	fun clearNew(index: Int) {
		runBackGround {
			MarkModel.setRead(marks[index].id)
		}
	}

	private fun setMark(index: Int, array: MutableList<Mark>) {
		marksData[index] = array
		marks.clear()
		marks.addAll(array)
	}

	private fun sort(array: MutableList<Mark>): MutableList<Mark> {
		when(sortBy) {
			0 -> array.sortWith { a, b ->
				val v = a.type.compareTo(b.type) * sortType
				if(v == 0) a.mark.compareTo(b.mark) * sortType
				else v
			}

			1 -> array.sortWith { a, b -> a.mark.compareTo(b.mark) * sortType }
			2 -> array.sortedWith { a, b -> a.time.compareTo(b.time) * sortType }
		}
		return array
	}
}