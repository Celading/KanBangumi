package com.heyanle.easy_extension

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.heyanle.easybangumi4.source_api.Source

/**
 * Created by heyanlin on 2023/10/24.
 */
sealed class Extension {
    abstract val label: String
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Int
    abstract val readme: String?
    abstract val icon: Drawable?
    abstract val loadType: Int

    companion object {
        const val TYPE_APP = 0
        const val TYPE_FILE = 1
    }

    data class Installed(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        override val loadType: Int,
        val sources: List<Source>,
        val resources: Resources?,
    ): Extension()

    data class InstallError(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        override val loadType: Int,
        val exception: Exception?,
        val errMsg: String,
    ): Extension()

}
