#shader vertex(vec3 position, vec2 uv)

mat4 model;
mat4 view;
mat4 projection;

void main() {
    outPosition = projection * view * model * vec4(position, 1.0);
    pixel.uv = uv;
}

#shader pixel(vec2 uv)

sampler2D texture;

void main() {
    outColor = texture2D(texture, uv);
}
