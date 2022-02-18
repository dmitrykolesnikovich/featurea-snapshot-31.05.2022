package featurea.graphics

import featurea.opengl.Buffer
import featurea.shader.ShaderGraphics
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val Buffer.limit: ReadWriteProperty<ShaderGraphics, Int> get() = BufferDrawCallLimitDelegate(this)

class BufferDrawCallLimitDelegate(val buffer: Buffer) : ReadWriteProperty<ShaderGraphics, Int> {

    override fun getValue(thisRef: ShaderGraphics, property: KProperty<*>): Int {
        return buffer.drawCallLimit
    }

    override fun setValue(thisRef: ShaderGraphics, property: KProperty<*>, value: Int) {
        buffer.ensureDrawCallLimit(value)
    }

}
