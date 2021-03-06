package featurea.runtime

enum class RuntimeState {
    RUNTIME_NEW,
    RUNTIME_INIT_START,
    CONTAINER_INIT_START,
    CONTAINER_INIT_COMPLETE,
    CONTAINER_CREATE_AWAIT,
    CONTAINER_CREATE,
    MODULE_INIT,
    MODULE_CREATE_AWAIT,
    MODULE_CREATE,
    MODULE_BUILD,
    RUNTIME_INIT_COMPLETE,
}
