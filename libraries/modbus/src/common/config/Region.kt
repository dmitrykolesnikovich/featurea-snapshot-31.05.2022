package featurea.modbus.config

import featurea.Specified

enum class Region(override val specifier: String, val isWritable: Boolean, val bitSize: Int) : Specified {
    Coils("Coils", isWritable = true, bitSize = 1),
    Discretes("Discretes", isWritable = false, bitSize = 1),
    Inputs("Inputs", isWritable = false, bitSize = 16),
    Holdings("Holdings", isWritable = true, bitSize = 16),
}