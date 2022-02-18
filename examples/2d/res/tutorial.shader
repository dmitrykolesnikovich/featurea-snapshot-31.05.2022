#shader vertex(vec2 aPos, vec2 aPrevPos, vec2 aNextPos, float aOffset)

vec4 settings;

void main() {
    vec4 pos = vec4(aPos.xy, 0, 1);
    vec2 deltaNext = aNextPos.xy - aPos.xy;
    vec2 deltaPrev = aPos.xy - aPrevPos.xy;
    float angleNext = atan(deltaNext.y, deltaNext.x);
    float anglePrev = atan(deltaPrev.y, deltaPrev.x);
    if (deltaPrev.xy == vec2(0, 0)) anglePrev = angleNext;
    if (deltaNext.xy == vec2(0, 0)) angleNext = anglePrev;
    float angle = (anglePrev + angleNext) / 2.0;
    float distance = aOffset * 2.0 / cos(anglePrev - angle);
    pos.x += distance * sin(angle);
    pos.y -= distance * cos(angle);
    outPosition = PROJECTION_MATRIX * pos;
    pixel.v_alpha = 1.0;
}

#shader pixel(float v_alpha)

vec4 tint;

void main() {
    outColor = vec4(tint.rgb, tint.a * v_alpha);
}
