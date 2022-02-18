#shader vertex(vec2 position, vec4 tint)

void main() {
    outPosition = mvp(position);
    pixel.tint = tint;
}

#shader pixel(vec4 tint)

void main() {
    outColor = tint;
}
