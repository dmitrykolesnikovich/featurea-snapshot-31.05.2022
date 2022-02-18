package featurea.runtime

class ContainerRegistry internal constructor() {

    private val containers = ComponentRegistry<Container>()

    fun getOrNull(key: String): Container? {
        return containers.getOrNull(key)
    }

    fun injectContainer(canonicalName: String, container: Container) {
        check(!containers.containsKey(canonicalName))
        check(!containers.contains(container))
        container.registry = this
        containers.inject(canonicalName, container)
    }

    fun appendContainer(canonicalName: String, container: Container) {
        check(!containers.contains(container))
        container.registry = this
        containers.append(canonicalName, container)
    }

    fun removeContainer(container: Container) {
        removeContainer(container.key)
    }

    fun removeContainer(key: String) {
        containers.remove(key)?.onDestroy()
    }

    fun destroy() {
        for (container in containers) {
            container.destroy()
        }
    }

}
