package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    val filteredTasks: StateFlow<List<Task>> = combine(_tasks, _filter) { tasks, filter ->
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = dao.getAllTasks()
        }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun addTask(description: String) {
        viewModelScope.launch {
            dao.insertTask(Task(description = description))
            _tasks.value = dao.getAllTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
            _tasks.value = dao.getAllTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            _tasks.value = dao.getAllTasks()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            _tasks.value = emptyList()
        }
    }
}