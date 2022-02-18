#shader vertex(vec2 point)

void main() {
    outPosition = PROJECTION_MATRIX * vec4(point, 0, 1);
}

#shader pixel

vec2 ORIGIN;
vec2 SIZE;
mat4 ROTATION;

float rectangle(vec2 fragment, vec2 origin, vec2 size) {
    vec2 d = 1.0 - smoothstep(-3.0, 0.0, abs(fragment - origin) - size);
    return min(d.x, d.y);
}

void main() {
    vec2 fragment = vec2(PIXEL_POSITION.x, WINDOW_SIZE.y - PIXEL_POSITION.y);
    fragment = (ROTATION * vec4(fragment, 0, 1)).xy; // rotate
    float tint = rectangle(fragment, ORIGIN, SIZE);
    outColor = vec4(1, 0, 0, tint);
}
