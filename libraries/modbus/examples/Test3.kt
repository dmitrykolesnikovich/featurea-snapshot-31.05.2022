package featurea.modbus.test

import featurea.modbus.support.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Test3 {

    @Test
    fun testUInt16() {
        assertEquals(0f, UInt16(initialValue = 0f).normalizedValue)
        assertEquals(1f, UInt16(initialValue = 1f).normalizedValue)

        assertEquals(UInt16.MAX_VALUE, UInt16(initialValue = -1f).normalizedValue)
        assertEquals(UInt16.MAX_VALUE - 9, UInt16(initialValue = -10f).normalizedValue)
    }

    @Test
    fun testInt16() {
        assertEquals(0f, Int16(initialValue = 0f).normalizedValue)
        assertEquals(1f, Int16(initialValue = 1f).normalizedValue)

        assertNotEquals(Int16.MAX_VALUE, Int16(initialValue = -1f).normalizedValue)
        assertEquals(-1f, Int16(initialValue = -1f).normalizedValue)

        assertNotEquals(Int16.MAX_VALUE - 9, Int16(initialValue = -10f).normalizedValue)
        assertEquals(-10f, Int16(initialValue = -10f).normalizedValue)
    }

    @Test
    fun testUInt32() {
        assertEquals(0f, UInt32(initialValue = 0f).normalizedValue)
        assertEquals(1f, UInt32(initialValue = 1f).normalizedValue)

        assertEquals(UInt32.MAX_VALUE, UInt32(initialValue = -1f).normalizedValue)
        assertEquals(UInt32.MAX_VALUE - 9, UInt32(initialValue = -10f).normalizedValue)
    }

    @Test
    fun testInt32() {
        assertEquals(0f, Int32(initialValue = 0f).normalizedValue)
        assertEquals(1f, Int32(initialValue = 1f).normalizedValue)

        assertNotEquals(Int32.MAX_VALUE, Int32(initialValue = -1f).normalizedValue)
        assertEquals(-1f, Int32(initialValue = -1f).normalizedValue)
        assertNotEquals(Int32.MAX_VALUE - 9, Int32(initialValue = -10f).normalizedValue)
        assertEquals(-10f, Int32(initialValue = -10f).normalizedValue)
    }

    @Test
    fun testFloat32() {
        assertEquals(0f, Float32(initialValue = 0f).normalizedValue)
        assertEquals(1f, Float32(initialValue = 1f).normalizedValue)

        assertNotEquals(Float32.MAX_VALUE, Float32(initialValue = -1f).normalizedValue)
        assertEquals(-1f, Float32(initialValue = -1f).normalizedValue)
        assertNotEquals(Float32.MAX_VALUE - 9, Float32(initialValue = -10f).normalizedValue)
        assertEquals(-10f, Float32(initialValue = -10f).normalizedValue)
    }

}
