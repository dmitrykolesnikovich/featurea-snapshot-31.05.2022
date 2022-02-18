package featurea.desktop.simulator.features

import featurea.desktop.featureaCachePath
import featurea.desktop.simulator.types.Simulator
import featurea.fileTransfer.ConsumeFileListener
import featurea.fileTransfer.FileTransfer
import featurea.runtime.Action
import featurea.runtime.import

private val PROJECTS_PATH = "$featureaCachePath/Projects"

val uploadBundle: Action = {
    val simulator: Simulator = import()

    val fileTransfer = FileTransfer()
    fileTransfer.addFileTransferComponentListener(ConsumeFileListener { file ->
        simulator.openBundle(file)
    })
    fileTransfer.createBroadcast(200, PROJECTS_PATH, System.getProperty("java.vm.name"), System.getProperty("os.name"))
    fileTransfer.startBroadcast()
    module.destroyListeners.add {
        fileTransfer.destroyBroadcast()
    }
}
