package com.qust.helper.viewmodel.eas

import androidx.compose.runtime.toMutableStateList
import com.qust.helper.data.i18n.Strings
import com.qust.helper.entity.eas.Exam
import com.qust.helper.model.account.EasAccount
import com.qust.helper.model.eas.ExamModel
import com.qust.helper.model.eas.MarkModel
import com.qust.helper.utils.Logger
import com.qust.helper.viewmodel.extend.toastError
import com.qust.helper.viewmodel.extend.toastOK

class QueryExamViewModel: BaseEasViewModel() {

	val exams = emptyList<Exam>().toMutableStateList()

	private val examData: MutableList<List<Exam>?> = MutableList(Strings.ARRAY_TERM_NAME.size) { null }

	fun query(){
		request({
			val term = pickYear.intValue
			val picker = getPickTerm()
			val resultData = ExamModel.queryExam(EasAccount, term, picker.first, picker.second)

			val origData = examData[term] ?: ExamModel.getExamsByTerm(term)

			if(origData.isEmpty()) {
				ExamModel.insertAll(resultData)
				examData[term] = null
				changeTerm(term)
				toastOK("查询完成")
				return@request
			}

			val origin = origData.toSet()
			val result = resultData.toSet()

			val new = result subtract origin
			val update = origin intersect result
			if(new.isNotEmpty() || update.isNotEmpty()) {
				ExamModel.updateAll(update.toList())
				ExamModel.insertAll(new.toList())
				examData[term] = null
				changeTerm(term)
				toastOK("新查询到 ${new.size} 门考试")
			}else{
				toastOK("未查询到新考试")
			}
		}) {
			Logger.e(e = it)
			toastError("查询失败：${it.message}")
		}
	}

	fun changeTerm(index: Int){
		val data = examData[index]
		if(data == null){
			request({
				val data = ExamModel.getExamsByTerm(index)
				examData[index] = data
				exams.clear()
				exams.addAll(data)
			})
		}else{
			exams.clear()
			exams.addAll(data)
		}
	}

	fun clearNew(index: Int) {
		runBackGround {
			MarkModel.setRead(exams[index].id)
		}
	}
}