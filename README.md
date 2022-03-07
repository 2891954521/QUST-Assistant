# 青科助手  

## 使用说明  

### 课表  

主页课表界面可查看当日课表  
课表界面 `上滑` 可以查看学期总课表，`左右` 滑动以查看其他周课表  

#### 总课表界面  
**点击** `课程` 或 `空白` 可 `编辑` 或 `添加` 课程  
**长按**某课程可以进行 `复制` 或 `删除` ，`复制` 后长按 `空白` 或 `课程` 可以 `粘贴` 课程，**若课程时间冲突则无法粘贴**  
**长按** `非本周课程` 可添加新课程，**新添加的课程不能与现有课程冲突**  

#### 设置  
设置开学时间：设置开学时间以计算当前周数  
设置本学期总周数：设置学期总周数
设置时间表：设置课表使用的时间

### ~~笔记~~  
**该功能暂时停止维护，后续会进行优化**

### 导航 alpha
选择起始地点，点击导航，会自动计算路径

### 教务  
仅可访问青岛科技大学教务  
使用前请先登录教务系统，**未登录会自动跳转到登陆界面**  

#### 课表查询  
查询每学期课表，选择学期后点击查询，确认无误后**点击右上角以应用课表**  

#### 成绩查询  
查询每学期成绩，选择学期后点击查询，查询结果会被缓存，下次查看无需再次查询，需要更新数据时重新查询即可  

#### 学业查询  
查询专业课程修读状况及成绩，点击右上角`刷新`图标更新数据，数据会被缓存  
**平均绩点计算：** 点击右上角首个图标后进入平均绩点计算模式，点击选择课程后自动计算平均绩点   

#### 自动教评 beta
自动完成教评（默认全选A），点击右上角更新教评信息，点击自动评价自动填写

#### ~~选课系统~~  
未完成  

#### ~~空教室查询~~  
未完成  

#### 考试查询  
查询每学期考试安排，选择学期后点击查询，查询结果会被缓存

#### 饮水码  
输入多彩校园账号密码获取饮水码


## 更新日志
* #### 1.8.9
1. 修复课表无法更新的bug
2. 修复总课表界面周数异常的bug
3. 修复更新课表后开学日期异常的bug
4. 修复课程编辑界面位置异常的bug
5. 移除今日校园

* #### 1.8.8
1. 界面修改
2. 合并夕彩校园
3. 修复失效功能
4. 修复成绩查询只能查到期末成绩的bug
5. 修复自动教评功能

* #### 1.8.7 dev
1. 修改应用包名为com.qust.assistant
2. 修复今曰校园假条消失的bug
3. 饮水码支持亮度调节

* #### 1.8.6  
1. 修复了当前周大于总周数时闪退的bug
2. 获取课表时可以自动获取开学时间和周数
3. 修复了课程只有一周时从教务获取的课程信息错误的bug

* #### 1.8.5 dev
1. 修复当日课表内容丢失的bug
2. 修复课表界面长按菜单异常显示的bug
3. 其他细节优化

* #### 1.8.4 dev
1. 修复课表界面长按菜单无法显示的bug
2. 导航功能测试上线
3. 课程时间提醒重做

* #### 1.8.3
1. 修复课程单双周显示错误的bug
2. 修复编辑课程时修改上课周数点击取消仍会报存的bug
3. 修复编辑界面上课周数空白面积过大的bug
4. 修复无法自动下载新版本的bug

* #### 1.8.2
1. 修复课表界面课程时间显示位置不正确的bug
2. 修复自动教评功能
3. 修复更改开学日期导致闪退的bug
4. 今日校园自动提交功能暂时下线
5. 其他细节优化

* ### 1.8
1. 修复获取课表后不保存无法返回的bug
2. 合并今曰校园
3. 课表界面点击后会高亮标记点击的课程，二次点击后可进行编辑，减少误触概率
4. 教务系统查询交互优化，修复失效的功能
5. 增加开发版更新渠道
6. 新增平均绩点计算功能

* ### 1.7  
1. 加入界面过渡动画，大部分界面均支持进行右滑返回  
2. 各类查询界面学期选择更便捷  
3. 加入设置界面，课表设置移入总设置界面  
4. 编辑课程时课程名为空或没有设置上课时间时不允许添加  
5. 获取课表时未保存会提示进行保存  
6. 更换图标  
7. 加入自动教评功能（测试性）  
8. 学业情况查询可查看平均绩点（GPA）  

* ### 1.6  
1. 课表核心代码重构，可以自由调整课程节数，同一时间段可以添加多节不冲突的课程，课程上课周数设置更准确，长按课程可以进行复制粘贴  
2. 修复没有平时分的科目成绩显示为null的bug  
3. 添加考试查询功能  
4. 修复学业情况查询查询不到课程的bug  
5. 暂时屏蔽导航和课程时间计算功能

* ### 1.5  
1. 完成今日校园自动填写功能  
2. 添加学业情况查询功能  
3. 优化成绩查询结果样式  
4. 修复无法下载更新的bug  

* ### 1.4  
1. 课表查询可以先预览再应用了  
2. 添加今日校园自动填写功能（未完成）  
3. 优化自动更新功能  

* ### 1.3  
1. 加入查成绩功能  
2. 修复打开应用时概率闪退的bug  

* ### 1.2  
1. 加入自动更新功能  
2. 修复bug  
