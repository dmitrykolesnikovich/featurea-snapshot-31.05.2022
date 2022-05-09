struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
};

struct DirectionalLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

#shader vertex(vec3 position, vec3 normal, vec2 uv)

mat4 model;
mat4 view;
mat4 projection;

void main() {
    outPosition = projection * view * model * vec4(position, 1.0);
    pixel.position = vec3(model * vec4(position, 1.0));
    pixel.normal = ext_mat3(transpose(inverse(model))) * normal;
    pixel.uv = uv;
}

#shader pixel(vec3 position, vec3 normal, vec2 uv)

vec3 cameraPosition;
Material material;
DirectionalLight directionalLight;
#define NR_POINT_LIGHTS 4
PointLight pointLights[NR_POINT_LIGHTS];
SpotLight spotLight;

vec3 CalculateDirectionalLight(DirectionalLight light, vec3 objectNormal, vec3 cameraDirection);
vec3 CalculatePointLight(PointLight light, vec3 objectNormal, vec3 cameraDirection, vec3 pixelPosition);
vec3 CalculateSpotLight(SpotLight light, vec3 objectNormal, vec3 cameraDirection, vec3 pixelPosition);

void main() {
    vec3 objectNormal = normalize(normal);
    vec3 cameraDirection = normalize(cameraPosition - position);

    vec3 result = CalculateDirectionalLight(directionalLight, objectNormal, cameraDirection);
    for (int i = 0; i < NR_POINT_LIGHTS; i++) {
        result += CalculatePointLight(pointLights[i], objectNormal, cameraDirection, position);
    }
    result += CalculateSpotLight(spotLight, objectNormal, cameraDirection, position);

    outColor = vec4(result, 1.0);
}

vec3 CalculateDirectionalLight(DirectionalLight light, vec3 objectNormal, vec3 cameraDirection) {
    vec3 lightDirection = normalize(-light.direction);

    // ambient
    vec3 ambientLight = light.ambient * vec3(texture2D(material.diffuse, uv));

    // diffuse
    float diffuseAmount = max(dot(objectNormal, lightDirection), 0.0);
    vec3 diffuseLight = light.diffuse * diffuseAmount * vec3(texture2D(material.diffuse, uv));

    // specular
    vec3 reflectDirection = reflect(-lightDirection, objectNormal);
    float specularAmount = pow(max(dot(cameraDirection, reflectDirection), 0.0), material.shininess);
    vec3 specularLight = light.specular * specularAmount * vec3(texture2D(material.specular, uv));

    return ambientLight + diffuseLight + specularLight;
}

vec3 CalculatePointLight(PointLight light, vec3 objectNormal, vec3 cameraDirection, vec3 pixelPosition) {
    vec3 lightDirection = normalize(light.position - pixelPosition); // point light and spotlight

    // ambient
    vec3 ambientLight = light.ambient * vec3(texture2D(material.diffuse, uv));

    // diffuse
    float diffuseAmount = max(dot(objectNormal, lightDirection), 0.0);
    vec3 diffuseLight = light.diffuse * diffuseAmount * vec3(texture2D(material.diffuse, uv));

    // specular
    vec3 reflectDirection = reflect(-lightDirection, objectNormal);
    float specularAmount = pow(max(dot(cameraDirection, reflectDirection), 0.0), material.shininess);
    vec3 specularLight = light.specular * specularAmount * vec3(texture2D(material.specular, uv));

    // attenuation
    float distance = length(light.position - pixelPosition);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    ambientLight *= attenuation;
    diffuseLight *= attenuation;
    specularLight *= attenuation;

    return ambientLight + diffuseLight + specularLight;
}

vec3 CalculateSpotLight(SpotLight light, vec3 objectNormal, vec3 cameraDirection, vec3 pixelPosition) {
    vec3 lightDirection = normalize(light.position - pixelPosition);

    // ambient
    vec3 ambientLight = light.ambient * vec3(texture2D(material.diffuse, uv));

    // diffuse
    float diffuseAmount = max(dot(objectNormal, lightDirection), 0.0);
    vec3 diffuseLight = light.diffuse * diffuseAmount * vec3(texture2D(material.diffuse, uv));

    // specular
    vec3 reflectDirection = reflect(-lightDirection, objectNormal);
    float specularAmount = pow(max(dot(cameraDirection, reflectDirection), 0.0), material.shininess);
    vec3 specularLight = light.specular * specularAmount * vec3(texture2D(material.specular, uv));

    // attenuation
    float distance = length(light.position - pixelPosition);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    ambientLight *= attenuation;
    diffuseLight *= attenuation;
    specularLight *= attenuation;

    // intensity
    float theta = dot(lightDirection, normalize(-light.direction));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    ambientLight *= intensity;
    diffuseLight *= intensity;
    specularLight *= intensity;

    return ambientLight + diffuseLight + specularLight;
}
