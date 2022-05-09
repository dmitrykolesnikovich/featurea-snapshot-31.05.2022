#shader vertex(vec2 position)

void main() {
    outPosition = vec4(position, 0, 1);
}

#shader pixel

vec4 tint;

void main() {
    outColor = tint;
}
