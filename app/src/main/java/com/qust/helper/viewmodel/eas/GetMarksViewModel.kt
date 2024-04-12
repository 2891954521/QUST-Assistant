package com.qust.helper.viewmodel.eas

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.qust.helper.data.Data.TermName
import com.qust.helper.data.room.LessonDatabase
import com.qust.helper.data.room.Mark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetMarksViewModel(application: Application) : BaseEasViewModel(application){

	private val lessonData = LessonDatabase.getInstance(application)

	val hasQuerySql: Array<Boolean> = Array(TermName.size) { false }
	val marksData: Array<Array<Mark>> = Array(TermName.size) { emptyArray() }

	var marks: MutableState<Array<Mark>> = mutableStateOf(emptyArray())

	var update by mutableStateOf(false)

	private var sortBy: Int = 0

	private var sortType: Int = 1

	fun queryMarks() {
		viewModelScope.launch {
			showDialog("查询中")
			withContext(Dispatchers.IO){
				if(checkLogin()){
					val index = pickYear.intValue
					val pair = getYearAndTerm()
					val result = sort(easAccount.queryMark(index, pair.first, pair.second))
					try {
						if(!hasQuerySql[index]){
							marksData[index] = lessonData.markDao().selectByIndex(index).toTypedArray()
							hasQuerySql[index] = true
						}
						val origData = marksData[index]
						if(origData.isEmpty()){
							lessonData.markDao().insertAll(result)
							setMark(index, result.toTypedArray())
						}else{
							val difference = result.subtract(origData.toSet())
							if(difference.isNotEmpty()) {
								lessonData.markDao().insertAll(difference.toList())
								setMark(index, (difference + origData).toTypedArray())
							}
						}
					}catch(e: Exception){
						e.printStackTrace()
					}
				}
			}
			clearDialog()
		}
	}

	fun selectData(index: Int){
		if(hasQuerySql[index]){
			marks.value = marksData[index]
		}else{
			viewModelScope.launch {
				withContext(Dispatchers.IO) {
					setMark(index, sort(lessonData.markDao().selectByIndex(index)).toTypedArray())
					hasQuerySql[index] = true
				}
			}
		}
	}

	fun setSortBy(index: Int){
		sortBy = index
		setMark(pickYear.intValue, sort(marksData[pickYear.intValue].toList()).toTypedArray())
	}

	fun setSortType(index: Int){
		sortType = index
		setMark(pickYear.intValue, sort(marksData[pickYear.intValue].toList()).toTypedArray())
	}

	fun clearNew(index: Int) {
		marksData[pickYear.intValue][index] = marksData[pickYear.intValue][index].copy(isNew = 0)
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				lessonData.markDao().setRead(marks.value[index].id)
			}
		}
		update = !update
	}

	private fun setMark(index: Int, array: Array<Mark>){
		marksData[index] = array
		marks.value = array
	}

	private fun sort(array: List<Mark>): List<Mark>{
		return when(sortBy){
			0 -> array.sortedWith { a, b ->
				val v = a.type.compareTo(b.type) * sortType
				if(v == 0) a.mark.compareTo(b.mark) * sortType
				else v
			}
			1 -> array.sortedWith { a, b -> a.mark.compareTo(b.mark) * sortType }
			2 -> array.sortedWith { a, b -> a.time.time.compareTo(b.time.time) * sortType }
			else -> array
		}
	}
}


