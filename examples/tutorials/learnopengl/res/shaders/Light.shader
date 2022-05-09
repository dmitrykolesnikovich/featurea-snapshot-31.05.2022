#shader vertex(vec3 position)

mat4 model;
mat4 view;
mat4 projection;

void main() {
    outPosition = projection * view * model * vec4(position, 1.0);
}

#shader pixel

void main() {
    outColor = vec4(1.0);
}
