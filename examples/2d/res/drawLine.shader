#shader vertex(vec2 point, vec2 normal, float alpha)

float HALF_WIDTH;

void main() {
    vec2 corner = point + normal * HALF_WIDTH;
    outPosition = PROJECTION_MATRIX * vec4(corner, 0, 1);
    pixel.alpha = alpha;
}

#shader pixel(float alpha)

vec3 TINT;

void main() {
    outColor = vec4(TINT, alpha);
}
