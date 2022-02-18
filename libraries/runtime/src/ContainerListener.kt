package featurea.runtime

// todo rename `ContainerListener` to `StaticComponent`
interface ContainerListener {
    fun provideComponent(canonicalName: String, component: Any) {}
}