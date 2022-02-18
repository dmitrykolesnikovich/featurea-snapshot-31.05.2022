// todo make this file automatically generated from `res/featurea/shader/*.c` files
package featurea.shader.reader

val exportAllBlock: String by lazy { exportProperties.values.joinToString("\n\n") }

val glslBlock: String = """
    #define outPosition gl_Position
    #define outColor gl_FragColor
    #define PIXEL_POSITION gl_FragCoord
""".trimIndent()

val uniformsBlock: String = """
    uniform mat4 MODEL_MATRIX;
    uniform mat4 VIEW_MATRIX;
    uniform mat4 PROJECTION_MATRIX;
    uniform mat4 VIEW_PROJECTION_MATRIX;
    uniform vec2 VIEW_SCALE;
    uniform vec2 WINDOW_SIZE;
""".trimIndent()


val uniformsMediumPrecisionBlock: String = """
    uniform mediump mat4 MODEL_MATRIX;
    uniform mediump mat4 VIEW_MATRIX;
    uniform mediump mat4 PROJECTION_MATRIX;
    uniform mediump mat4 VIEW_PROJECTION_MATRIX;
    uniform mediump vec2 VIEW_SCALE;
    uniform mediump vec2 WINDOW_SIZE;
""".trimIndent()

// IMPORTANT order has meaning: 1. glsl, 2. uniforms, 3. the rest
val exportProperties: Map<String, String> = linkedMapOf(

    "color.blend" to """
        vec4 blend(vec4 color1, vec4 color2) {
            vec3 mixColor = mix(color1.rgb, color2.rgb, color2.a);
            return vec4(mixColor.rgb, color2.a);
        }
    """.trimIndent(),

    "color.srgb" to """
        vec4 srgb(vec4 color) {
            return pow(max(color, 0.0), vec4(1.0 / 2.2));
        }
    """.trimIndent(),

    "conversion.toPixelPerfectFloat" to """
        float toPixelPerfectFloat(float value) {
            return floor(value - 0.0009765625) + 0.5;
        }
    """.trimIndent(),

    "conversion.viewportPosition" to """
        vec2 viewportPosition(vec2 point) {
            vec2 result = (VIEW_MATRIX * vec4(point, 0, 1)).xy;
            result.y = WINDOW_SIZE.y - result.y;
            return result;
        }
    """.trimIndent(),

    "conversion.viewportX" to """
        float viewportX(float x) {
            x *= VIEW_SCALE.x;
            return x;
        }
    """.trimIndent(),

    "conversion.viewportY" to """
        float viewportY(float y) {
            y *= VIEW_SCALE.y;
            return y;
        }
    """.trimIndent(),

    "conversion.viewportSize" to """
        vec2 viewportSize(vec2 size) {
            size.x *= VIEW_SCALE.x;
            size.y *= VIEW_SCALE.y;
            return size;
        }
    """.trimIndent(),

    "math.PI" to """
        const float PI = 3.141592;
    """.trimIndent(),

    "math.degree" to """
        float degree(vec2 vector) {
            return atan(vector.y, vector.x) / PI * 180.0;
        }
    """.trimIndent(),

    "mvp" to """
        vec2 m(vec2 position) {
            return (MODEL_MATRIX * vec4(position, 0, 1)).xy;
        }
        
        vec2 mv(vec2 position) {
            return (VIEW_MATRIX * MODEL_MATRIX * vec4(position, 0, 1)).xy;
        }
        
        vec4 mvp(vec2 position) {
            return VIEW_PROJECTION_MATRIX * MODEL_MATRIX * vec4(position, 0, 1);
        }
    """.trimIndent(),

    "transform.inverse" to """
        float inverse(float m) {
            return 1.0 / m;
        }

        mat2 inverse(mat2 m) {
            return mat2(m[1][1],-m[0][1], -m[1][0], m[0][0]) / (m[0][0]*m[1][1] - m[0][1]*m[1][0]);
        }

        mat3 inverse(mat3 m) {
            float a00 = m[0][0], a01 = m[0][1], a02 = m[0][2];
            float a10 = m[1][0], a11 = m[1][1], a12 = m[1][2];
            float a20 = m[2][0], a21 = m[2][1], a22 = m[2][2];
            
            float b01 = a22 * a11 - a12 * a21;
            float b11 = -a22 * a10 + a12 * a20;
            float b21 = a21 * a10 - a11 * a20;
            
            float det = a00 * b01 + a01 * b11 + a02 * b21;
            
            return mat3(b01, (-a22 * a01 + a02 * a21), (a12 * a01 - a02 * a11),
                      b11, (a22 * a00 - a02 * a20), (-a12 * a00 + a02 * a10),
                      b21, (-a21 * a00 + a01 * a20), (a11 * a00 - a01 * a10)) / det;
        }

        mat4 inverse(mat4 m) {
            float
                a00 = m[0][0], a01 = m[0][1], a02 = m[0][2], a03 = m[0][3],
                a10 = m[1][0], a11 = m[1][1], a12 = m[1][2], a13 = m[1][3],
                a20 = m[2][0], a21 = m[2][1], a22 = m[2][2], a23 = m[2][3],
                a30 = m[3][0], a31 = m[3][1], a32 = m[3][2], a33 = m[3][3],
                
                b00 = a00 * a11 - a01 * a10,
                b01 = a00 * a12 - a02 * a10,
                b02 = a00 * a13 - a03 * a10,
                b03 = a01 * a12 - a02 * a11,
                b04 = a01 * a13 - a03 * a11,
                b05 = a02 * a13 - a03 * a12,
                b06 = a20 * a31 - a21 * a30,
                b07 = a20 * a32 - a22 * a30,
                b08 = a20 * a33 - a23 * a30,
                b09 = a21 * a32 - a22 * a31,
                b10 = a21 * a33 - a23 * a31,
                b11 = a22 * a33 - a23 * a32,
                
                det = b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06;

            return mat4(
                a11 * b11 - a12 * b10 + a13 * b09,
                a02 * b10 - a01 * b11 - a03 * b09,
                a31 * b05 - a32 * b04 + a33 * b03,
                a22 * b04 - a21 * b05 - a23 * b03,
                a12 * b08 - a10 * b11 - a13 * b07,
                a00 * b11 - a02 * b08 + a03 * b07,
                a32 * b02 - a30 * b05 - a33 * b01,
                a20 * b05 - a22 * b02 + a23 * b01,
                a10 * b10 - a11 * b08 + a13 * b06,
                a01 * b08 - a00 * b10 - a03 * b06,
                a30 * b04 - a31 * b02 + a33 * b00,
                a21 * b02 - a20 * b04 - a23 * b00,
                a11 * b07 - a10 * b09 - a12 * b06,
                a00 * b09 - a01 * b07 + a02 * b06,
                a31 * b01 - a30 * b03 - a32 * b00,
                a20 * b03 - a21 * b01 + a22 * b00) / det;
        }
    """.trimIndent(),

    "transform.transpose" to """        
        mat4 transpose(mat4 inMatrix) {
            vec4 i0 = inMatrix[0];
            vec4 i1 = inMatrix[1];
            vec4 i2 = inMatrix[2];
            vec4 i3 = inMatrix[3];

            mat4 outMatrix = mat4(
                vec4(i0.x, i1.x, i2.x, i3.x),
                vec4(i0.y, i1.y, i2.y, i3.y),
                vec4(i0.z, i1.z, i2.z, i3.z),
                vec4(i0.w, i1.w, i2.w, i3.w)
            );

            return outMatrix;
        }
    """.trimIndent(),

    "transform.ext_mat3" to """
        mat3 ext_mat3(mat4 original) {
            return mat3(original[0].xyz, original[1].xyz, original[2].xyz);
        }
    """.trimIndent(),

    )
