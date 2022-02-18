package featurea.modbus

enum class ModbusClientState {
    INIT,
    CONNECT,
    RESPONSE,
    DISCONNECT,
    DESTROY
} 