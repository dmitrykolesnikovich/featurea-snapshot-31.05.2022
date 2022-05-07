package featurea.opengl

import featurea.*
import featurea.layout.Camera
import featurea.layout.toScissorRectangle
import featurea.math.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.Color
import featurea.utils.Colors
import featurea.utils.Logger
import featurea.utils.Stack
import featurea.window.Window
import featurea.window.toScissorRectangle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/*GLES20*/

const val ACTIVE_ATTRIBUTE_MAX_LENGTH: Int = 0x8B8A
const val ACTIVE_ATTRIBUTES: Int = 0x8B89
const val ACTIVE_TEXTURE: Int = 0x84E0
const val ACTIVE_UNIFORM_MAX_LENGTH: Int = 0x8B87
const val ACTIVE_UNIFORMS: Int = 0x8B86
const val ALIASED_LINE_WIDTH_RANGE: Int = 0x846E
const val ALIASED_POINT_SIZE_RANGE: Int = 0x846D
const val ALPHA_BITS: Int = 0x0D55
const val ALPHA: Int = 0x1906
const val ALWAYS: Int = 0x0207
const val ARRAY_BUFFER_BINDING: Int = 0x8894
const val ARRAY_BUFFER: Int = 0x8892
const val ATTACHED_SHADERS: Int = 0x8B85
const val BACK: Int = 0x0405
const val BLEND_COLOR: Int = 0x8005
const val BLEND_DST_ALPHA: Int = 0x80CA
const val BLEND_DST_RGB: Int = 0x80C8
const val BLEND_EQUATION_ALPHA: Int = 0x883D
const val BLEND_EQUATION_RGB: Int = 0x8009
const val BLEND_EQUATION: Int = 0x8009
const val BLEND_SRC_ALPHA: Int = 0x80CB
const val BLEND_SRC_RGB: Int = 0x80C9
const val BLEND: Int = 0x0BE2
const val BLUE_BITS: Int = 0x0D54
const val BOOL_VEC2: Int = 0x8B57
const val BOOL_VEC3: Int = 0x8B58
const val BOOL_VEC4: Int = 0x8B59
const val BOOL: Int = 0x8B56
const val BUFFER_SIZE: Int = 0x8764
const val BUFFER_USAGE: Int = 0x8765
const val BYTE: Int = 0x1400
const val CCW: Int = 0x0901
const val CLAMP_TO_BORDER: Int = 0x812D
const val CLAMP_TO_EDGE: Int = 0x812F
const val COLOR_ATTACHMENT0: Int = 0x8CE0
const val COLOR_BUFFER_BIT: Int = 0x00004000
const val COLOR_CLEAR_VALUE: Int = 0x0C22
const val COLOR_WRITEMASK: Int = 0x0C23
const val COMPILE_STATUS: Int = 0x8B81
const val COMPRESSED_TEXTURE_FORMATS: Int = 0x86A3
const val CONSTANT_ALPHA: Int = 0x8003
const val CONSTANT_COLOR: Int = 0x8001
const val CULL_FACE_MODE: Int = 0x0B45
const val CULL_FACE: Int = 0x0B44
const val CURRENT_PROGRAM: Int = 0x8B8D
const val CURRENT_VERTEX_ATTRIB: Int = 0x8626
const val CW: Int = 0x0900
const val DECR_WRAP: Int = 0x8508
const val DECR: Int = 0x1E03
const val DELETE_STATUS: Int = 0x8B80
const val DEPTH_ATTACHMENT: Int = 0x8D00
const val DEPTH_BITS: Int = 0x0D56
const val DEPTH_BUFFER_BIT: Int = 0x00000100
const val DEPTH_CLEAR_VALUE: Int = 0x0B73
const val DEPTH_COMPONENT: Int = 0x1902
const val DEPTH_COMPONENT16: Int = 0x81A5
const val DEPTH_FUNC: Int = 0x0B74
const val DEPTH_RANGE: Int = 0x0B70
const val DEPTH_TEST: Int = 0x0B71
const val DEPTH_WRITEMASK: Int = 0x0B72
const val DITHER: Int = 0x0BD0
const val DONT_CARE: Int = 0x1100
const val DST_ALPHA: Int = 0x0304
const val DST_COLOR: Int = 0x0306
const val DYNAMIC_DRAW: Int = 0x88E8
const val ELEMENT_ARRAY_BUFFER_BINDING: Int = 0x8895
const val ELEMENT_ARRAY_BUFFER: Int = 0x8893
const val EQUAL: Int = 0x0202
const val EXTENSIONS: Int = 0x1F03
const val FALSE: Int = 0
const val FASTEST: Int = 0x1101
const val FILL: Int = 0x1B02
const val FIXED: Int = 0x140C
const val FLOAT_MAT2: Int = 0x8B5A
const val FLOAT_MAT3: Int = 0x8B5B
const val FLOAT_MAT4: Int = 0x8B5C
const val FLOAT_VEC2: Int = 0x8B50
const val FLOAT_VEC3: Int = 0x8B51
const val FLOAT_VEC4: Int = 0x8B52
const val FLOAT: Int = 0x1406
const val FRAGMENT_SHADER: Int = 0x8B30
const val FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: Int = 0x8CD1
const val FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: Int = 0x8CD0
const val FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: Int = 0x8CD3
const val FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: Int = 0x8CD2
const val FRAMEBUFFER_BINDING: Int = 0x8CA6
const val FRAMEBUFFER_COMPLETE: Int = 0x8CD5
const val FRAMEBUFFER_INCOMPLETE_ATTACHMENT: Int = 0x8CD6
const val FRAMEBUFFER_INCOMPLETE_DIMENSIONS: Int = 0x8CD9
const val FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: Int = 0x8CD7
const val FRAMEBUFFER_UNSUPPORTED: Int = 0x8CDD
const val FRAMEBUFFER: Int = 0x8D40
const val FRONT_AND_BACK: Int = 0x0408
const val FRONT_FACE: Int = 0x0B46
const val FRONT: Int = 0x0404
const val FUNC_ADD: Int = 0x8006
const val FUNC_REVERSE_SUBTRACT: Int = 0x800B
const val FUNC_SUBTRACT: Int = 0x800A
const val GENERATE_MIPMAP_HINT: Int = 0x8192
const val GEQUAL: Int = 0x0206
const val GREATER: Int = 0x0204
const val GREEN_BITS: Int = 0x0D53
const val HIGH_FLOAT: Int = 0x8DF2
const val HIGH_INT: Int = 0x8DF5
const val IMPLEMENTATION_COLOR_READ_FORMAT: Int = 0x8B9B
const val IMPLEMENTATION_COLOR_READ_TYPE: Int = 0x8B9A
const val INCR_WRAP: Int = 0x8507
const val INCR: Int = 0x1E02
const val INFO_LOG_LENGTH: Int = 0x8B84
const val INT_VEC2: Int = 0x8B53
const val INT_VEC3: Int = 0x8B54
const val INT_VEC4: Int = 0x8B55
const val INT: Int = 0x1404
const val INVALID_ENUM: Int = 0x0500
const val INVALID_FRAMEBUFFER_OPERATION: Int = 0x0506
const val INVALID_OPERATION: Int = 0x0502
const val INVALID_VALUE: Int = 0x0501
const val INVERT: Int = 0x150A
const val KEEP: Int = 0x1E00
const val LEQUAL: Int = 0x0203
const val LESS: Int = 0x0201
const val LINE_LOOP: Int = 0x0002
const val LINE_SMOOTH: Int = 0xB20
const val LINE_STRIP: Int = 0x0003
const val LINE_WIDTH: Int = 0x0B21
const val LINE: Int = 0x1B01
const val LINEAR_MIPMAP_LINEAR: Int = 0x2703
const val LINEAR_MIPMAP_NEAREST: Int = 0x2701
const val LINEAR: Int = 0x2601
const val LINES: Int = 0x0001
const val LINK_STATUS: Int = 0x8B82
const val LOW_FLOAT: Int = 0x8DF0
const val LOW_INT: Int = 0x8DF3
const val LUMINANCE_ALPHA: Int = 0x190A
const val LUMINANCE: Int = 0x1909
const val MAX_COMBINED_TEXTURE_IMAGE_UNITS: Int = 0x8B4D
const val MAX_CUBE_MAP_TEXTURE_SIZE: Int = 0x851C
const val MAX_FRAGMENT_UNIFORM_VECTORS: Int = 0x8DFD
const val MAX_RENDERBUFFER_SIZE: Int = 0x84E8
const val MAX_TEXTURE_IMAGE_UNITS: Int = 0x8872
const val MAX_TEXTURE_SIZE: Int = 0x0D33
const val MAX_VARYING_VECTORS: Int = 0x8DFC
const val MAX_VERTEX_ATTRIBS: Int = 0x8869
const val MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int = 0x8B4C
const val MAX_VERTEX_UNIFORM_VECTORS: Int = 0x8DFB
const val MAX_VIEWPORT_DIMS: Int = 0x0D3A
const val MAX: Int = 0x8008
const val MEDIUM_FLOAT: Int = 0x8DF1
const val MEDIUM_INT: Int = 0x8DF4
const val MIN: Int = 0x8007
const val MIRRORED_REPEAT: Int = 0x8370
const val MULTISAMPLE: Int = 0x809D
const val NEAREST_MIPMAP_LINEAR: Int = 0x2702
const val NEAREST_MIPMAP_NEAREST: Int = 0x2700
const val NEAREST: Int = 0x2600
const val NEVER: Int = 0x0200
const val NICEST: Int = 0x1102
const val NO_ERROR: Int = 0
const val NONE: Int = 0
const val NOTEQUAL: Int = 0x0205
const val NUM_COMPRESSED_TEXTURE_FORMATS: Int = 0x86A2
const val NUM_SHADER_BINARY_FORMATS: Int = 0x8DF9
const val ONE_MINUS_CONSTANT_ALPHA: Int = 0x8004
const val ONE_MINUS_CONSTANT_COLOR: Int = 0x8002
const val ONE_MINUS_DST_ALPHA: Int = 0x0305
const val ONE_MINUS_DST_COLOR: Int = 0x0307
const val ONE_MINUS_SRC_ALPHA: Int = 0x0303
const val ONE_MINUS_SRC_COLOR: Int = 0x0301
const val ONE: Int = 1
const val OUT_OF_MEMORY: Int = 0x0505
const val PACK_ALIGNMENT: Int = 0x0D05
const val POINTS: Int = 0x0000
const val POLYGON_OFFSET_FACTOR: Int = 0x8038
const val POLYGON_OFFSET_FILL: Int = 0x8037
const val POLYGON_OFFSET_UNITS: Int = 0x2A00
const val POLYGON_SMOOTH: Int = 0xb41
const val RED_BITS: Int = 0x0D52
const val RENDERBUFFER_ALPHA_SIZE: Int = 0x8D53
const val RENDERBUFFER_BINDING: Int = 0x8CA7
const val RENDERBUFFER_BLUE_SIZE: Int = 0x8D52
const val RENDERBUFFER_DEPTH_SIZE: Int = 0x8D54
const val RENDERBUFFER_GREEN_SIZE: Int = 0x8D51
const val RENDERBUFFER_HEIGHT: Int = 0x8D43
const val RENDERBUFFER_INTERNAL_FORMAT: Int = 0x8D44
const val RENDERBUFFER_RED_SIZE: Int = 0x8D50
const val RENDERBUFFER_STENCIL_SIZE: Int = 0x8D55
const val RENDERBUFFER_WIDTH: Int = 0x8D42
const val RENDERBUFFER: Int = 0x8D41
const val RENDERER: Int = 0x1F01
const val REPEAT: Int = 0x2901
const val REPLACE: Int = 0x1E01
const val RGB: Int = 0x1907
const val RGB5_A1: Int = 0x8057
const val RGB565: Int = 0x8D62
const val RGBA: Int = 0x1908
const val RGBA4: Int = 0x8056
const val SAMPLE_ALPHA_TO_COVERAGE: Int = 0x809E
const val SAMPLE_BUFFERS: Int = 0x80A8
const val SAMPLE_COVERAGE_INVERT: Int = 0x80AB
const val SAMPLE_COVERAGE_VALUE: Int = 0x80AA
const val SAMPLE_COVERAGE: Int = 0x80A0
const val SAMPLER_2D: Int = 0x8B5E
const val SAMPLER_CUBE: Int = 0x8B60
const val SAMPLES: Int = 0x80A9
const val SCISSOR_BOX: Int = 0x0C10
const val SCISSOR_TEST: Int = 0x0C11
const val SHADER_BINARY_FORMATS: Int = 0x8DF8
const val SHADER_COMPILER: Int = 0x8DFA
const val SHADER_SOURCE_LENGTH: Int = 0x8B88
const val SHADER_TYPE: Int = 0x8B4F
const val SHADING_LANGUAGE_VERSION: Int = 0x8B8C
const val SHORT: Int = 0x1402
const val SRC_ALPHA_SATURATE: Int = 0x0308
const val SRC_ALPHA: Int = 0x0302
const val SRC_COLOR: Int = 0x0300
const val STATIC_DRAW: Int = 0x88E4
const val STENCIL_ATTACHMENT: Int = 0x8D20
const val STENCIL_BACK_FAIL: Int = 0x8801
const val STENCIL_BACK_FUNC: Int = 0x8800
const val STENCIL_BACK_PASS_DEPTH_FAIL: Int = 0x8802
const val STENCIL_BACK_PASS_DEPTH_PASS: Int = 0x8803
const val STENCIL_BACK_REF: Int = 0x8CA3
const val STENCIL_BACK_VALUE_MASK: Int = 0x8CA4
const val STENCIL_BACK_WRITEMASK: Int = 0x8CA5
const val STENCIL_BITS: Int = 0x0D57
const val STENCIL_BUFFER_BIT: Int = 0x00000400
const val STENCIL_CLEAR_VALUE: Int = 0x0B91
const val STENCIL_FAIL: Int = 0x0B94
const val STENCIL_FUNC: Int = 0x0B92
const val STENCIL_INDEX8: Int = 0x8D48
const val STENCIL_PASS_DEPTH_FAIL: Int = 0x0B95
const val STENCIL_PASS_DEPTH_PASS: Int = 0x0B96
const val STENCIL_REF: Int = 0x0B97
const val STENCIL_TEST: Int = 0x0B90
const val STENCIL_VALUE_MASK: Int = 0x0B93
const val STENCIL_WRITEMASK: Int = 0x0B98
const val STREAM_DRAW: Int = 0x88E0
const val SUBPIXEL_BITS: Int = 0x0D50
const val TEXTURE_2D: Int = 0x0DE1
const val TEXTURE_BINDING_2D: Int = 0x8069
const val TEXTURE_BINDING_CUBE_MAP: Int = 0x8514
const val TEXTURE_BORDER_COLOR: Int = 0x1004
const val TEXTURE_CUBE_MAP_NEGATIVE_X: Int = 0x8516
const val TEXTURE_CUBE_MAP_NEGATIVE_Y: Int = 0x8518
const val TEXTURE_CUBE_MAP_NEGATIVE_Z: Int = 0x851A
const val TEXTURE_CUBE_MAP_POSITIVE_X: Int = 0x8515
const val TEXTURE_CUBE_MAP_POSITIVE_Y: Int = 0x8517
const val TEXTURE_CUBE_MAP_POSITIVE_Z: Int = 0x8519
const val TEXTURE_CUBE_MAP: Int = 0x8513
const val TEXTURE_MAG_FILTER: Int = 0x2800
const val TEXTURE_MIN_FILTER: Int = 0x2801
const val TEXTURE_WRAP_S: Int = 0x2802
const val TEXTURE_WRAP_T: Int = 0x2803
const val TEXTURE: Int = 0x1702
const val TEXTURE0: Int = 0x84C0
const val TEXTURE1: Int = 0x84C1
const val TEXTURE10: Int = 0x84CA
const val TEXTURE11: Int = 0x84CB
const val TEXTURE12: Int = 0x84CC
const val TEXTURE13: Int = 0x84CD
const val TEXTURE14: Int = 0x84CE
const val TEXTURE15: Int = 0x84CF
const val TEXTURE16: Int = 0x84D0
const val TEXTURE17: Int = 0x84D1
const val TEXTURE18: Int = 0x84D2
const val TEXTURE19: Int = 0x84D3
const val TEXTURE2: Int = 0x84C2
const val TEXTURE20: Int = 0x84D4
const val TEXTURE21: Int = 0x84D5
const val TEXTURE22: Int = 0x84D6
const val TEXTURE23: Int = 0x84D7
const val TEXTURE24: Int = 0x84D8
const val TEXTURE25: Int = 0x84D9
const val TEXTURE26: Int = 0x84DA
const val TEXTURE27: Int = 0x84DB
const val TEXTURE28: Int = 0x84DC
const val TEXTURE29: Int = 0x84DD
const val TEXTURE3: Int = 0x84C3
const val TEXTURE30: Int = 0x84DE
const val TEXTURE31: Int = 0x84DF
const val TEXTURE4: Int = 0x84C4
const val TEXTURE5: Int = 0x84C5
const val TEXTURE6: Int = 0x84C6
const val TEXTURE7: Int = 0x84C7
const val TEXTURE8: Int = 0x84C8
const val TEXTURE9: Int = 0x84C9
const val TRIANGLE_FAN: Int = 0x0006
const val TRIANGLE_STRIP: Int = 0x0005
const val TRIANGLES: Int = 0x0004
const val TRUE: Int = 1
const val UNPACK_ALIGNMENT: Int = 0x0CF5
const val UNSIGNED_BYTE: Int = 0x1401
const val UNSIGNED_INT: Int = 0x1405
const val UNSIGNED_SHORT_4_4_4_4: Int = 0x8033
const val UNSIGNED_SHORT_5_5_5_1: Int = 0x8034
const val UNSIGNED_SHORT_5_6_5: Int = 0x8363
const val UNSIGNED_SHORT: Int = 0x1403
const val VALIDATE_STATUS: Int = 0x8B83
const val VENDOR: Int = 0x1F00
const val VERSION: Int = 0x1F02
const val VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: Int = 0x889F
const val VERTEX_ATTRIB_ARRAY_ENABLED: Int = 0x8622
const val VERTEX_ATTRIB_ARRAY_NORMALIZED: Int = 0x886A
const val VERTEX_ATTRIB_ARRAY_POINTER: Int = 0x8645
const val VERTEX_ATTRIB_ARRAY_SIZE: Int = 0x8623
const val VERTEX_ATTRIB_ARRAY_STRIDE: Int = 0x8624
const val VERTEX_ATTRIB_ARRAY_TYPE: Int = 0x8625
const val VERTEX_SHADER: Int = 0x8B31
const val VIEWPORT: Int = 0x0BA2
const val ZERO: Int = 0

abstract class Opengl(override val module: Module) : Component {

    abstract fun activeTexture(texture: Int)
    abstract fun attachShader(program: Program, shader: Shader)
    abstract fun bindAttributeLocation(program: Program, index: Int, name: String)
    abstract fun bindBuffer(target: Int, buffer: Buffer?)
    abstract fun bindTexture(target: Int, texture: Texture?)
    abstract fun blendColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun blendEquation(mode: Int)
    abstract fun blendFunction(sourceFactor: Int, destinationFactor: Int)
    abstract fun blendFunctionSeparate(srcRgb: Int, dstRgb: Int, srcAlpha: Int, dstAlpha: Int)
    abstract fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int)
    abstract fun bufferData(target: Int, data: FloatArray, usage: Int)
    abstract fun bufferData(target: Int, data: IntArray, usage: Int)
    abstract fun bufferSubData(target: Int, offset: Long, size: Long, data: FloatArray)
    abstract fun clear(mask: Int)
    abstract fun clearColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun compileShader(shader: Shader)
    abstract fun createProgram(): Program
    abstract fun createShader(type: Int): Shader
    abstract fun cullFace(mode: Int)
    abstract fun deleteBuffer(buffer: Buffer)
    abstract fun deleteShader(shader: Shader)
    abstract fun deleteTexture(texture: Texture)
    abstract fun depthFunction(function: Int)
    abstract fun disable(capability: Int)
    abstract fun disableVertexAttributeArray(index: Int)
    abstract fun drawArrays(mode: Int, first: Int, count: Int)
    abstract fun drawElements(mode: Int, count: Int, type: Int, indices: IntArray)
    abstract fun enable(capability: Int)
    abstract fun enableVertexAttribArray(index: Int)
    abstract fun generateMipmap(target: Int)
    abstract fun getAttributeLocation(program: Program, name: String): Int
    abstract fun getProgramInfoLog(program: Program): String
    abstract fun getProgramParameter(program: Program, parameter: Int): Int
    abstract fun getShaderInfoLog(shader: Shader): String
    abstract fun getShaderParameter(shader: Shader, parameter: Int): Int
    abstract fun getString(name: Int): String
    abstract fun getUniformLocation(program: Program, name: String): UniformLocation
    abstract fun lineWidth(width: Float)
    abstract fun linkProgram(program: Program)
    abstract fun pixelStore(parameter: Int, value: Int)
    abstract fun polygonMode(face: Int, mode: Int)
    abstract fun scissor(x: Int, y: Int, width: Int, height: Int)
    abstract fun shaderSource(shader: Shader, source: String)
    abstract fun textureParameter(target: Int, parameter: Int, value: Int)
    abstract fun uniform(location: UniformLocation, float: Float)
    abstract fun uniform(location: UniformLocation, int: Int)
    abstract fun uniform(location: UniformLocation, float1: Float, float2: Float)
    abstract fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float)
    abstract fun uniform(location: UniformLocation, float1: Float, float2: Float, float3: Float, float4: Float)
    abstract fun uniform(location: UniformLocation, matrix: Matrix)
    abstract fun useProgram(program: Program?)
    abstract fun vertexAttributePointer(index: Int, size: Int, type: Int, stride: Int, offset: Int)
    abstract fun viewport(x: Int, y: Int, width: Int, height: Int)

    /*custom*/

    val logger: Logger = Logger(tag = "Opengl")
    var program: Program? = null
    var precision: Precision = Precision.MEDIUM
    val window: Window = import()
    private val system: System = import()
    private val utilityVector: Vector2.Result = Vector2().Result()
    private val scissorStack: Stack<IntRectangle> = Stack<IntRectangle>()

    init {
        logger.isEnable = system.properties["featurea.opengl.logger"] ?: false
    }

    abstract fun createTexture(texturePath: String): Texture

    abstract fun createBuffer(drawCallSize: Int, isMedium: Boolean): Buffer

    fun clear(color: Color = Colors.blackColor) {
        clearColor(color.red, color.green, color.blue, color.alpha)
        clear(COLOR_BUFFER_BIT)
    }

    fun drawTriangles(buffer: Buffer, usage: Int = STATIC_DRAW) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        bindBuffer(ARRAY_BUFFER, buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), usage)
            buffer.isDirty = false
        }
        drawArrays(TRIANGLES, 0, buffer.vertexCount)
    }

    fun drawTriangleFan(buffer: Buffer, usage: Int = STATIC_DRAW) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        bindBuffer(ARRAY_BUFFER, buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), usage)
            buffer.isDirty = false
        }
        drawArrays(TRIANGLE_FAN, 0, buffer.vertexCount)
    }

    fun drawTriangleStrip(buffer: Buffer, count: Int = buffer.vertexCount, usage: Int = STATIC_DRAW) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        bindBuffer(ARRAY_BUFFER, buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), usage)
            buffer.isDirty = false
        }
        drawArrays(TRIANGLE_STRIP, 0, count)
    }

    fun drawLineLoop(buffer: Buffer, isSmooth: Boolean = false) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), STATIC_DRAW)
            buffer.isDirty = false
        }
        if (isSmooth) {
            enable(LINE_SMOOTH)
        }
        drawArrays(LINE_LOOP, 0, buffer.vertexCount)
        if (isSmooth) {
            disable(LINE_SMOOTH)
        }
    }

    fun drawLines(buffer: Buffer, isSmooth: Boolean = false) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), STATIC_DRAW)
            buffer.isDirty = false
        }
        if (isSmooth) {
            enable(LINE_SMOOTH)
        }
        drawArrays(LINES, 0, buffer.vertexCount)
        if (isSmooth) {
            disable(LINE_SMOOTH)
        }
    }

    fun drawLineStrip(buffer: Buffer, count: Int = buffer.vertexCount, isSmooth: Boolean = false) {
        val program: Program = checkNotNull(program)
        program.bindBuffer(buffer)
        if (buffer.isDirty) {
            bufferData(ARRAY_BUFFER, buffer.data.toFloatArray(), STATIC_DRAW)
            buffer.isDirty = false
        }
        if (isSmooth) {
            enable(LINE_SMOOTH)
        }
        drawArrays(LINE_STRIP, 0, count)
        if (isSmooth) {
            disable(LINE_SMOOTH)
        }
    }

    fun scissor(camera: Camera, block: () -> Unit) {
        if (window.useCamera) {
            val (x1, y1, x2, y2) = camera.toScissorRectangle()
            enableScissor(x1, y1, x2, y2)
        }
        block()
        if (window.useCamera) {
            disableScissor()
        }
    }

    fun scissor(camera: Camera, scissor: Rectangle, isEnable: Boolean = true, block: () -> Unit) {
        if (isEnable) {
            enableScissorLocal(scissor, camera)
        }
        block()
        if (isEnable) {
            disableScissor()
        }
    }

    fun enableScissor(rectangle: Rectangle) {
        val (x1, y1, x2, y2) = rectangle
        enableScissor(x1, y1, x2, y2)
    }

    fun enableScissorLocal(localRectangle: Rectangle, camera: Camera) {
        val (x1, y1, x2, y2) = window.toScissorRectangle(camera, localRectangle, utilityVector)
        enableScissor(x1, y1, x2, y2)
    }

    fun enableScissor(x1: Float, y1: Float, x2: Float, y2: Float) {
        // flip
        val y1: Float = window.surface.viewport.height - y1
        val y2: Float = window.surface.viewport.height - y2

        // round
        var left: Int = when (precision) {
            Precision.MEDIUM -> x1.prevIntMediumPrecision
            else -> x1.roundToInt()
        }
        var top: Int = when (precision) {
            Precision.MEDIUM -> y1.nextIntMediumPrecision
            else -> y1.roundToInt()
        }
        var right: Int = when (precision) {
            Precision.MEDIUM -> x2.nextIntMediumPrecision
            else -> x2.roundToInt()
        }
        var bottom: Int = when (precision) {
            Precision.MEDIUM -> y2.prevIntMediumPrecision
            else -> y2.roundToInt()
        }

        // adjust
        val prevRectangle: IntRectangle? = scissorStack.lastOrNull()
        if (prevRectangle != null) {
            val (prevLeft, prevTop, prevRight, prevBottom) = prevRectangle
            left = max(left, prevLeft)
            top = min(top, prevTop)
            right = min(right, prevRight)
            bottom = max(bottom, prevBottom)
        }

        // action
        enable(SCISSOR_TEST)
        val width: Int = right - left
        val height: Int = top - bottom
        scissor(left, bottom, width, height)
        scissorStack.push(IntRectangle(left, top, right, bottom))
    }

    fun disableScissor() {
        scissorStack.pop()
        val prevRectangle: IntRectangle? = scissorStack.lastOrNull()
        if (prevRectangle != null) {
            val (left, top, right, bottom) = prevRectangle
            enable(SCISSOR_TEST)
            val width: Int = right - left
            val height: Int = top - bottom
            scissor(left, bottom, width, height)
        } else {
            disable(SCISSOR_TEST)
        }
    }

    fun clearColorBuffer(red: Float, green: Float, blue: Float, alpha: Float) {
        clearColor(red, green, blue, alpha)
        clear(COLOR_BUFFER_BIT)
    }

    fun clearColorBufferAndDepthBuffer(red: Float, green: Float, blue: Float, alpha: Float) {
        clearColor(red, green, blue, alpha)
        enable(DEPTH_TEST)
        clear(COLOR_BUFFER_BIT or DEPTH_BUFFER_BIT)
    }

}

/*convenience*/

expect class Shader
expect class Texture
expect class UniformLocation

fun Opengl.bindBufferData(buffer: Buffer, data: FloatArray, usage: Int) {
    bindBuffer(ARRAY_BUFFER, buffer)
    bufferData(ARRAY_BUFFER, data, usage)
}