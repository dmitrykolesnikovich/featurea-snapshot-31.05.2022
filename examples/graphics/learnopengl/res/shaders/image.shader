// https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/1.getting_started/4.1.textures/4.1.texture.vs
#shader vertex(vec3 aPos, vec2 uv)

void main() {
    outPosition = vec4(aPos, 1.0);
    pixel.uv = uv;
}

// https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/1.getting_started/4.1.textures/4.1.texture.fs
#shader pixel(vec2 uv)

sampler2D ourTexture;
float alphaTest;
float alphaTest2;

void main() {
    vec4 pixel = texture2D(ourTexture, uv);
    if (alphaTest != 0.0) {
        if (pixel.a < alphaTest) {
            discard;
        }
    } else if (pixel.a < alphaTest2) {
        pixel.rgba = pixel.rgba * 3.0;
    }
    outColor = pixel;
}
