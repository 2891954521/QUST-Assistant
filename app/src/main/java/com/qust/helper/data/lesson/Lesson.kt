package com.qust.helper.data.lesson

import com.qust.helper.model.Logger
import kotlinx.serialization.Serializable
import org.json.JSONObject

/**
 * 一节课程
 * @param type 课程类型 0: auto 自动添加的课程; 1: user 用户创建的课程
 * @param kchID 课程ID
 * @param color 课程颜色
 * @param len 课程课时
 * @param week 第几周有课, long形式的boolean数组
 * @param name 课程名称
 * @param place 课程教室
 * @param teacher 课程教师
 */
@Serializable
data class Lesson(
	var type: Int = 0,
	var kchID: String = "",
	var color: Int = 0,
	var len: Int = 1,
	var week: Long = 0,
	var name: String = "",
	var place: String = "",
	var teacher: String = "",
){
	companion object {
		/**
		 * 从json中解析Lesson
		 * @param json
		 * @return
		 */
		fun loadFromJson(json: JSONObject): Lesson {
			var lesson: Lesson
			try {
				lesson = Lesson(
					kchID = json.getString("kch"),
					name = json.getString("kcmc").trim(),
					place = if(json.has("cdmc")) json.getString("cdmc").trim() else "",
					teacher = if(json.has("xm")) json.getString("xm").trim() else "",
				)

				val times = json.getString("zcd").split(",")
				for(time in times) {
					var _time: String = time
					var type: Int  // 单双周类型
					if(_time.endsWith(")")){
						type = if(_time[_time.length - 2] == '单') 0 else 1
						_time = _time.substring(0, _time.length - 4)
					}else{
						type = -1
						_time = _time.substring(0, _time.length - 1)
					}

					if(_time.contains("-")) {
						val p = _time.split("-")
						fillLesson(lesson, p[0].toInt() - 1, p[1].toInt(), type)
					} else {
						lesson.week = lesson.week or (1L shl (_time.toInt() - 1))
					}
				}
			}catch(e: Exception){
				Logger.w(json.toString(), e)
				lesson = Lesson(name = "课程解析错误")
			}
			return lesson
		}

		/**
		 * 填充课程上课周 (翻转数据)
		 * @param start
		 * @param end
		 * @param type -1 正常 0 单周 1 双周
		 */
		private fun fillLesson(lesson: Lesson, start: Int, end: Int, type: Int) {
			var _start = start
			var mask: Long
			if(type == -1) {
				mask = (1L shl end - _start) - 1 shl _start
			} else {
				mask = 0L
				if(type == 0 && _start % 2 == 1 || type == 1 && _start % 2 == 0) _start += 1
				var i = _start
				while(i < end) {
					mask = mask or (1L shl i)
					i += 2
				}
			}
			lesson.week = lesson.week xor mask
		}
	}
}
