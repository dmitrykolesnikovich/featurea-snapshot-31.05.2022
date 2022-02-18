// https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/1.getting_started/2.1.hello_triangle/hello_triangle.cpp#L13
#shader vertex(vec3 position)

void main() {
    outPosition = vec4(position, 1.0);
}

// https://github.com/JoeyDeVries/LearnOpenGL/blob/master/src/1.getting_started/2.1.hello_triangle/hello_triangle.cpp#L19
#shader pixel

void main() {
    outColor = vec4(1.0, 0.5, 0.2, 1.0);
}
