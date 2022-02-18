package featurea.modbus.support

import featurea.modbus.config.Channel
import featurea.runtime.Component
import featurea.runtime.Module

class JournalService(val channel: Channel) {
    var hasJournalValueEver: Boolean = false
        private set
    var hasJournalValue: Boolean = false
        set(value) {
            field = value
            if (!hasJournalValueEver) {
                hasJournalValueEver = value
            }
        }


    var journalTimeoutProgress: Double = 0.0
    var journalValue: Float = 0f
}

class JournalSystem(override val module: Module) : Component {
    // todo add JournalService to index
}