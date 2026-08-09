package com.qust.helper.next.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineJob(
    private val scope: CoroutineScope,
    private val  block: suspend CoroutineScope.() -> Unit
): AutoCloseable {

    private var job: Job? = null

    fun start() {
        val oldJob = job
        job = scope.launch {
            withContext(Dispatchers.IO){
                if(oldJob != null && oldJob.isActive) oldJob.cancelAndJoin()
                block(this)
            }
        }
    }

    fun stop() {
        val oldJob = job
        if(oldJob != null && oldJob.isActive) oldJob.cancel()
    }

    override fun close() = stop()
}