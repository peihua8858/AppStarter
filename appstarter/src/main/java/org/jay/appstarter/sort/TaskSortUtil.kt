package org.jay.appstarter.sort

import androidx.collection.ArraySet
import org.jay.appstarter.Task
import org.jay.appstarter.utils.DispatcherLog

// 任务排序工具类
object TaskSortUtil {

    private const val sPrintAllTaskName = false

    /**
     * 任务的有向无环图的拓扑排序
     */
    @Synchronized
    fun getSortResult(
        originTasks: List<Task>,
        clsLaunchTasks: List<Class<out Task>>
    ): MutableList<Task> {
        if (originTasks.isEmpty()) {
            return ArrayList()
        }
        val makeTime = System.currentTimeMillis()
        val dependSet: MutableSet<Int> = ArraySet()
        val graph = Graph(originTasks.size)
        for (i in originTasks.indices) {
            val task = originTasks[i]
            val depends = task.dependsOn()
            if (task.isSend || depends.isNullOrEmpty()) {
                continue
            }
            for (cls in depends) {
                val indexOfDepend = getIndexOfTask(originTasks, clsLaunchTasks, cls)
                check(indexOfDepend >= 0) {
                    "${task.javaClass.simpleName} depends on ${cls.simpleName} can not be found in task list"
                }
                dependSet.add(indexOfDepend)
                graph.addEdge(indexOfDepend, i)
            }
        }
        val indexList: List<Int> = graph.topologicalSort()

        // 分类任务
        val newTasksDepended: MutableList<Task> = ArrayList() // 被别人依赖的
        val newTasksWithOutDepend: MutableList<Task> = ArrayList() // 没有依赖的
        val newTasksRunAsSoon: MutableList<Task> = ArrayList() // 需要提升自己优先级的，先执行
        for (index in indexList) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks[index])
            } else {
                val task = originTasks[index]
                if (task.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task)
                } else {
                    newTasksWithOutDepend.add(task)
                }
            }
        }

        // 顺序：被别人依赖的————》需要提升自己优先级的————》没有依赖的
        val newTasksAll: MutableList<Task> = ArrayList(originTasks.size)
        newTasksAll.addAll(newTasksDepended)
        newTasksAll.addAll(newTasksRunAsSoon)
        newTasksAll.addAll(newTasksWithOutDepend)

        DispatcherLog.i("task analyse cost makeTime ${System.currentTimeMillis() - makeTime}")
        printAllTaskName(newTasksAll)
        return newTasksAll
    }

    private fun printAllTaskName(newTasksAll: List<Task>) {
        if (!sPrintAllTaskName) {
            return
        }
        DispatcherLog.i(newTasksAll.joinToString(", ") { it.javaClass.simpleName })
    }

    /**
     * 获取任务在任务列表中的下标
     */
    private fun getIndexOfTask(
        originTasks: List<Task>,
        clsLaunchTasks: List<Class<out Task>>,
        cls: Class<*>
    ): Int {
        val index = clsLaunchTasks.indexOf(cls)
        if (index >= 0) {
            return index
        }

        // 仅仅是保护性代码
        for (i in originTasks.indices) {
            if (cls == originTasks[i].javaClass) {
                return i
            }
        }
        return index
    }
}