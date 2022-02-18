package featurea.examples.learnopengl

class TestDocket : ApplicationComponent(), Script {

    lateinit var test: Test

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Test {
        val value: String = args.first() as String
        if (action == "create") {
            test = module.createComponent()
        } else {
            when (action) {
                "clearColor" -> {
                    val (red, green, blue, alpha) = checkNotNull(value).toColor()
                    test.context.gl.clearColor(red, green, blue, alpha)
                }
                "blendFunc.sfactor" -> test.blendFunction.sourceFactor = blendFuncOf(value)
                "blendFunc.dfactor" -> test.blendFunction.destinationFactor = blendFuncOf(value)
                "blendFunc.color" -> test.blendFunction.color = value.toColor()
                "diffuseTexture" -> test.diffuseTexturePath = value
                "specularTexture" -> test.specularTexturePath = value
                "texture.minFilter" -> test.textureFilter.minFilter = textureMinFilterOf(value)
                "texture.magFilter" -> test.textureFilter.magFilter = textureMagFilterOf(value)
                "texture.wrapS" -> test.sampling.wrappingFunction.first = textureWrapOf(value)
                "texture.wrapT" -> test.sampling.wrappingFunction.second = textureWrapOf(value)
                "alphaTest" -> test.alphaTest = value.toFloat()
                "alphaTest2" -> test.alphaTest2 = value.toFloat()
                "rotationAngle" -> test.rotationAngle = value.toDouble()
                "rotationIncrement" -> test.rotationIncrement = value.toFloat()
                "viewTranslation" -> test.viewTranslation = value.toVector()
                "rotationAxis" -> test.rotationAxis = value.toVector()
                "near" -> test.near = value.toFloat()
                "far" -> test.far = value.toFloat()
                "fov" -> test.fov = value.toFloat()
                "radius" -> test.radius = value.toFloat()
                "build" -> {
                    val diffuseTexture = test.diffuseTexturePath
                    if (diffuseTexture != null) test.context.loader.loadResource(diffuseTexture)
                    val specularTexture = test.specularTexturePath
                    if (specularTexture != null) test.context.loader.loadResource(specularTexture)
                    if (!test.context.isStudioRuntime) {
                        test.context.isStudioRuntime = true
                        test.context.load(1f) // quickfix todo improve
                    }
                }
            }
        }
        return test
    }

}

class TestLinesDocket // todo
