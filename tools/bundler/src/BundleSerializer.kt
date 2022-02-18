package featurea.bundler

import featurea.Bundle
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BundleSerializer {

    fun serializeBundle(bundle: Bundle, bundleFile: File): Boolean {
        if (bundleFile.exists()) return false

        ZipOutputStream(FileOutputStream(bundleFile)).use {
            for ((name, bytes) in bundle.entries) {
                it.putNextEntry(ZipEntry(name))
                it.write(bytes, 0, bytes.size)
                it.closeEntry()
            }
        }
        return true
    }

}
