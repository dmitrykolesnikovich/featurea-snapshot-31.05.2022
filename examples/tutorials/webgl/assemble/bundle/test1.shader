#shader vertex(vec2 point)

void main() {
    outPosition = vec4(point, 0, 1);
}

#shader pixel

vec2 viewport;

void main() {
    vec2 fragment = PIXEL_POSITION.xy / viewport;
    outColor = vec4(1, 0, 0, fragment);
}
