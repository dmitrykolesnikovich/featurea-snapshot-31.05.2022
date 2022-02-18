package featurea.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import java.io.File
import java.util.jar.JarFile

fun Context.getApkFile(): JarFile {
    val packageName = packageName
    val packageManager = packageManager
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val sourceDir = packageInfo.applicationInfo.sourceDir
        JarFile(sourceDir)
    } catch (e: Throwable) {
        e.printStackTrace()
        throw IllegalArgumentException()
    }
}

val Context.appRoot: String
    get() {
        val dir: File = if (isExternalStorageAvailable) {
            /*
            val sdDir = Environment.getExternalStorageDirectory()
            File(sdDir, "/Android/data/$packageName")
            */
            applicationContext.getExternalFilesDir("")!!
        } else {
            File("/data/data/$packageName")
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

val root: File
    get() = if (isExternalStorageAvailable) {
        Environment.getExternalStorageDirectory()
    } else {
        File("/Download")
    }

private val isExternalStorageAvailable: Boolean
    get() {
        val mExternalStorageAvailable: Boolean
        val mExternalStorageWriteable: Boolean
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            mExternalStorageWriteable = true
            mExternalStorageAvailable = mExternalStorageWriteable
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            mExternalStorageAvailable = true
            mExternalStorageWriteable = false
        } else {
            mExternalStorageWriteable = false
            mExternalStorageAvailable = mExternalStorageWriteable
        }
        return mExternalStorageAvailable && mExternalStorageWriteable
    }

val Context.featureaDir get() = appRoot
val Context.bundlesDir get() = File(featureaDir, "Projects")
val Context.logsDir get() = File(featureaDir, "Logs")
val Context.trashDir get() = File(featureaDir, "Trash")
val Context.cacheDir get() = File(featureaDir, "Cache")
val Context.downloadsDir get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

val lastModifiedFileComparator: java.util.Comparator<File> = Comparator { file1, file2 ->
    val lastModified1 = file1.lastModified()
    val lastModified2 = file2.lastModified()
    if (lastModified2 - lastModified1 > 0) {
        1
    } else if (lastModified2 - lastModified1 < 0) {
        -1
    } else {
        0
    }
}

operator fun SharedPreferences.get(key: String): String? = getString(key, null)
