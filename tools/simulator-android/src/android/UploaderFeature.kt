package featurea.android.simulator

import android.os.Build
import featurea.android.MainActivityProxy
import featurea.android.bundlesDir
import featurea.fileTransfer.FileTransfer
import featurea.fileTransfer.FtpServer
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.fileTransfer.FileTransferEventListener
import featurea.utils.log
import java.io.File

class UploaderFeature(override val module: Module) : Component {

    private val mainActivity = import(MainActivityProxy)
    private val simulator: Simulator = import()

    private val downloader = FileTransfer().apply {
        addFileTransferComponentListener(object : FileTransferEventListener {
            override fun onUploadFile(ftpServer: FtpServer, file: File) {
                log("onUploadFile: $ftpServer, $file")
            }
            override fun onConsumeFile(file: File) {
                log("onConsumeFile: $file")
                // simulator.openBundle(file)
            }
        })
        createBroadcast(200, mainActivity.bundlesDir.absolutePath, Build.MODEL, Build.BRAND)
    }

    override fun onCreateComponent() {
        downloader.startBroadcast()
    }

    override fun onDeleteComponent() {
        downloader.destroyBroadcast()
    }

}
