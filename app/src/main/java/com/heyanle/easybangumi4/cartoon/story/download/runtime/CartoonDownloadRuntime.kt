package com.heyanle.easybangumi4.cartoon.story.download.runtime

import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 运行时的下载，只服务于运行时
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntime(
    val req: CartoonDownloadReq,
) {

    interface Listener {
        fun onStateChange(runtime: CartoonDownloadRuntime)
    }

    companion object {
        const val STATE_WAITING = 0
        const val STATE_DOING = 1
        const val STATE_STEP_COMPLETELY = 2
        const val STATE_ERROR = 3
        const val STATE_SUCCESS = 4
        const val STATE_CANCEL = 5
    }

    // 是否已经被调度（加进线程池）
    val hasDispatched = AtomicBoolean(false)

    var runnable: Runnable? = null

    val lock = Object()

    var canCancel = true

    // 所有异步锁收归到这里
    @Volatile
    var countDownLatch: CountDownLatch? = null

    // for parse step
    var parseResult: SourceResult<PlayerInfo>? = null


    // for transformer step
    var transformer: Transformer? = null
    var transformerProgress: Int = 0
    var exportResult: ExportResult? = null
    var exportException: ExportException? = null


    // 0 -> step_waiting
    // 1 -> step_doing
    // 2 -> step_completely
    // 3 -> error
    // 4 -> success
    // 5 -> cancel
    @Volatile
    var state: Int = 0
        set(value) {
            field = value
            listener?.onStateChange(this)
        }

    @Volatile
    var error: Throwable? = null

    @Volatile
    var errorMsg: String = ""

    @Volatile
    var stepIndex: Int = 0

    @Volatile
    var playerInfo: PlayerInfo? = null

    @Volatile
    var cacheFolderUri: String? = null

    @Volatile
    var cacheDisplayName: String? = null

    @Volatile
    var targetFolderUri: String? = null

    @Volatile
    var targetDisplayName: String? = null

    var listener: Listener? = null


    fun getDownloadInfo(): DownloadingBus.DownloadingInfo {
        val bus: DownloadingBus = Inject.get()
        return bus.getInfo(DownloadingBus.DownloadScene.CARTOON, req.uuid)
    }

    fun dispatchToBus(process: Float, status: String, subStatus: String = ""){
        val info = getDownloadInfo()
        info.process.value = process
        info.status.value = status
        info.subStatus.value = subStatus
    }

    fun error(error: Throwable? = null, errorMsg: String? = null) {
        this.error = error
        this.errorMsg = errorMsg ?: error?.message ?: ""
        this.state = 3
        dispatchStateToBus()
    }

    fun stepCompletely() {
        this.state = 2
        dispatchStateToBus()
    }

    fun dispatchStateToBus() {
        val info = getDownloadInfo()
        when (state) {
            0, 2 -> {
                info.process.value = -1f
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.waiting)
                info.subStatus.value = ""
            }
            3 -> {
                info.process.value = 0f
                info.status.value = errorMsg
            }
        }
    }

    fun needCancel(): Boolean {
        return state == 5 || state == 3
    }

    fun dispatcher(): Boolean {
        if (hasDispatched.compareAndSet(false, true)) {
            state = 0
            dispatchStateToBus()
            return true
        }
        return false
    }

    fun cancel(){
        state = 5
        dispatchStateToBus()
        hasDispatched.set(false)
    }

}