#shader vertex(vec3 position)

void main() {
    outPosition = vec4(position, 1.0);
}

#shader pixel

void main() {
    outColor = vec4(1.0, 0.5, 0.2, 1.0);
}
