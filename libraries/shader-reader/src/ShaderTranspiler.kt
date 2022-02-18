package featurea.shader.reader

import featurea.*
import featurea.shader.reader.ShaderSourceBlock.*

// https://stackoverflow.com/a/4430934/909169
// https://webglfundamentals.org/webgl/lessons/webgl-precision-issues.html
data class ShaderSource(
    val vertexShaderSource: String,
    val pixelShaderSource: String,
    val attributes: List<ShaderAttribute>
)

// todo add support for `#import` directive
// todo add support for `import` config
fun transpileShaderSource(text: String): ShaderSource {
    val commonBlock: StringBuilder = StringBuilder()

    val vertexSource: StringBuilder = StringBuilder()
    val vertexAttributes = ArrayList<ShaderAttribute>()
    val vertexAttributesBlock: StringBuilder = StringBuilder()
    val vertexUniformsBlock: StringBuilder = StringBuilder()
    val vertexDefinesBlock: StringBuilder = StringBuilder()
    val vertexBodyBlock: StringBuilder = StringBuilder()

    val pixelSource: StringBuilder = StringBuilder()
    val varyingAttributes = ArrayList<String>()
    val varyingBlock: StringBuilder = StringBuilder()
    val pixelUniformsBlock: StringBuilder = StringBuilder()
    val pixelDefinesBlock: StringBuilder = StringBuilder()
    val pixelBodyBlock: StringBuilder = StringBuilder()

    var block: ShaderSourceBlock = COMMON
    fun updateStep(line: String): Boolean {
        if (line.startsWith("//")) return false

        if (line.startsWith("#shader vertex")) {
            block = VERTEX_DECLARATION
        } else if (block == VERTEX_DECLARATION) {
            block = VERTEX_UNIFORMS
        }
        if (block == VERTEX_UNIFORMS && line.contains("(")) {
            block = VERTEX_BODY
        }
        if (line.startsWith("#shader pixel")) {
            block = PIXEL_DECLARATION
        } else if (block == PIXEL_DECLARATION) {
            block = PIXEL_UNIFORMS
        }
        if (block == PIXEL_UNIFORMS && (line.contains("("))) {
            block = PIXEL_BODY
        }
        return true
    }

    val lines: Sequence<String> = text.lineSequence()
    for (line in lines) {
        // filter
        if (isInstrumentationEnabled) {
            if (line.trim().startsWith("#transpile")) continue
        }

        // action
        if (!updateStep(line)) continue
        when (block) {
            COMMON -> {
                commonBlock.appendLine(line)
            }
            VERTEX_UNIFORMS -> {
                if (line.isBlank()) continue
                else if (line.startsWith("#define ")) vertexDefinesBlock.appendLine(line)
                else if (line.startsWith("struct "))
                else vertexUniformsBlock.append("uniform ").appendLine(line)
            }
            VERTEX_DECLARATION -> {
                val declaration = line.replace("#shader vertex", "").removePrefix("(").removeSuffix(")")
                if (declaration.isBlank()) continue
                val tokens = declaration.split(",").map { it.trim() }
                for (token in tokens) {
                    val (type, name) = token.split(" ")
                    vertexAttributes.add(ShaderAttribute(name, sizeOf(type)))
                    vertexAttributesBlock.append("attribute ").append(token).append(";").appendLine()
                }
            }
            VERTEX_BODY -> {
                val bodyLine = when {
                    line.trim().startsWith("pixel.") -> line.replace("pixel.", "pixel_")
                    else -> line
                }
                vertexBodyBlock.appendLine(bodyLine)
            }
            PIXEL_UNIFORMS -> {
                if (line.isBlank()) continue
                else if (line.startsWith("#define ")) pixelDefinesBlock.appendLine(line)
                else pixelUniformsBlock.append("uniform ").appendLine(line)
            }
            PIXEL_DECLARATION -> {
                val declaration = line.replace("#shader pixel", "").removePrefix("(").removeSuffix(")")
                if (declaration.isBlank()) continue
                val tokens = declaration.split(",").map { it.trim() }
                for (token in tokens) {
                    try {
                        val (type, name) = token.split(" ")
                        varyingAttributes.add(name)
                        varyingBlock.append("varying ").append(type).append(" pixel_").append(name).appendLine(";")
                    } catch (e: Exception) {
                        log("token: $token")
                        log(e)
                    }
                }
            }
            PIXEL_BODY -> {
                var bodyLine: String = line
                for (name in varyingAttributes) {
                    bodyLine = bodyLine
                        .replace("(${name} ", "(pixel_${name} ")
                        .replace("(${name},", "(pixel_${name},")
                        .replace("(${name}.", "(pixel_${name}.")
                        .replace("(${name})", "(pixel_${name})")

                        .replace(" ${name} ", " pixel_${name} ")
                        .replace(" ${name},", " pixel_${name},")
                        .replace(" ${name}.", " pixel_${name}.")
                        .replace(" ${name})", " pixel_${name})")
                        .replace(" ${name};", " pixel_${name};")
                }
                pixelBodyBlock.appendLine(bodyLine)
            }
        }
    }

    // result
    vertexSource.apply {
        if (!System.target.isDesktop) appendLine("precision highp float;").appendLine()
        if (vertexDefinesBlock.isNotBlank()) appendLine(vertexDefinesBlock)
        if (vertexAttributesBlock.isNotBlank()) appendLine(vertexAttributesBlock)
        if (varyingBlock.isNotBlank()) appendLine(varyingBlock)
        if (commonBlock.isNotBlank()) appendLine(commonBlock)
        if (vertexUniformsBlock.isNotBlank()) appendLine(vertexUniformsBlock)
        appendLine("/*standard*/").appendLine()
        appendLine(glslBlock).appendLine()
        if (isStandardShaderLibraryIncluded) {
            if (System.target.isDesktop) {
                appendLine(uniformsBlock).appendLine()
            } else {
                appendLine(uniformsMediumPrecisionBlock).appendLine()
            }
            appendLine(exportAllBlock).appendLine()
        }
        appendLine("/*source*/").appendLine()
        appendLine(vertexBodyBlock.trim())
    }
    pixelSource.apply {
        if (!System.target.isDesktop) appendLine("precision mediump float;").appendLine()
        if (pixelDefinesBlock.isNotBlank()) appendLine(pixelDefinesBlock)
        if (varyingBlock.isNotBlank()) appendLine(varyingBlock)
        if (commonBlock.isNotBlank()) appendLine(commonBlock)
        if (pixelUniformsBlock.isNotBlank()) appendLine(pixelUniformsBlock)
        appendLine("/*standard*/").appendLine()
        appendLine(glslBlock).appendLine()
        if (isStandardShaderLibraryIncluded) {
            if (System.target.isDesktop) {
                appendLine(uniformsBlock).appendLine()
            } else {
                appendLine(uniformsMediumPrecisionBlock).appendLine()
            }
            appendLine(exportAllBlock).appendLine()
        }
        appendLine("/*source*/").appendLine()
        appendLine(pixelBodyBlock.trim())
    }
    return ShaderSource(vertexSource.toString(), pixelSource.toString(), vertexAttributes)
}

/*internals*/

private enum class ShaderSourceBlock {
    COMMON,
    VERTEX_DECLARATION,
    VERTEX_UNIFORMS,
    VERTEX_BODY,
    PIXEL_DECLARATION,
    PIXEL_UNIFORMS,
    PIXEL_BODY,
}

private fun sizeOf(type: String): Int = when (type) {
    "int" -> 1
    "float" -> 1
    "vec2" -> 2
    "vec3" -> 3
    "vec4" -> 4
    else -> error("type: $type")
}

fun String.withLineNumbers(): String {
    val maxLength: Int = split("\n").size.toString().length
    return split("\n").mapIndexed { index, line -> "${(index + 1).toString().ensureLength(maxLength, "0")}: $line" }
        .joinToString("\n")
}
