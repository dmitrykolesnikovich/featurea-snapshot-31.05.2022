package featurea.runtime

interface ComponentProvider {
    fun provideComponent(canonicalName: String, component: Any) {}
}