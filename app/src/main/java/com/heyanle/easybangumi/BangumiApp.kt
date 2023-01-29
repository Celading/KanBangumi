package com.heyanle.easybangumi

import android.app.Application
import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.utils.exo_ssl.CropUtil
import com.heyanle.easybangumi.utils.exo_ssl.TrustAllHostnameVerifier
import com.heyanle.okkv2.MMKVStore
import com.heyanle.okkv2.core.Okkv
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import javax.net.ssl.HttpsURLConnection

/**
 * Created by HeYanLe on 2023/1/5 14:36.
 * https://github.com/heyanLE
 */
class BangumiApp : Application() {

    companion object {
        lateinit var INSTANCE: BangumiApp
    }

    override fun onCreate() {
        super.onCreate()


        INSTANCE = this

        initOkkv()

        initCrasher()

        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(TrustAllHostnameVerifier())

        EasyDB.init(this)

        AnimSourceFactory.init()

        initAppCenter()
    }

    private fun initOkkv() {
        Okkv.Builder().store(MMKVStore(this)).cache().build().init().default()
        // 如果不使用缓存，请手动指定 key
        Okkv.Builder().store(MMKVStore(this)).build().init().default("no_cache")
    }

    private fun initCrasher() {
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))
    }

    private fun initAppCenter() {
        kotlin.runCatching {
            // https://appcenter.ms
            AppCenter.start(
                this, "",
                Analytics::class.java, Crashes::class.java, Distribute::class.java
            )
            // 禁用自动更新 使用手动更新
            Distribute.disableAutomaticCheckForUpdate()
        }.onFailure {
            it.printStackTrace()
        }

    }

}