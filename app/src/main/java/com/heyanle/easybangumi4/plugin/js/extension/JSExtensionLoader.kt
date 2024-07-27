package com.heyanle.easybangumi4.plugin.js.extension

import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import java.io.File

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSExtensionLoader(
    private val file: File,
): ExtensionLoader {

    companion object {
        const val TAG = "JSExtensionLoader"

        const val JS_IMPORT = """
            importPackage(Packages.com.heyanle.easybangumi4.plugin.extension);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.runtime);
            importPackage(Packages.org.jsoup);
            importPackage(Packages.okhttp3);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.utils.api);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.entity);
        """
    }

    override val key: String
        get() = "js:${file.path}"

    override fun canLoad(): Boolean {
        return file.isFile && file.exists() && file.canRead()
    }

    override fun load(): ExtensionInfo? {
        if (!file.exists() || !file.canRead()) {
            return null
        }

        val map = HashMap<String, String>()

        val lineList = file.readLines()
        for (line in lineList) {
            if (line.isEmpty() || !line.startsWith("//")){
                break
            }

            var firstAtIndex = -1
            var spacerAfterAtIndex = -1

            line.forEachIndexed { index, c ->
                if (firstAtIndex == -1 && c != '@'){
                    firstAtIndex = index
                }
                if (firstAtIndex != -1 && spacerAfterAtIndex == -1 && c == ' '){
                    spacerAfterAtIndex = index
                }
                if (firstAtIndex != -1 && spacerAfterAtIndex != -1){
                    return@forEachIndexed
                }
            }

            if (firstAtIndex == -1 || spacerAfterAtIndex == -1){
                continue
            }

            val key = line.substring(firstAtIndex + 1, spacerAfterAtIndex)
            val value = line.substring(spacerAfterAtIndex + 1)
            map[key] = value
        }
        val text = lineList.joinToString("\n")
        val jsRuntime = JSRuntime()
        jsRuntime.init()
        jsRuntime.postWithScope { context, scriptableObject ->
            context.evaluateString(
                scriptableObject,
                JS_IMPORT,
                null,
                1,
                null

            )
            context.evaluateString(
                scriptableObject,
                text,
                null,
                1,
                null
            )

        }

        return null

    }


}