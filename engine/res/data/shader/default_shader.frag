#version 330 core

in vec2 texPos;

out vec4 fragColor;

uniform sampler2D texSampler;
uniform vec4 globalColoring;

void main() {
    vec4 c = texture(texSampler, texPos);
    if(c.w == 0)
        discard;
    vec3 finalColor = mix(c.rgb, globalColoring.rgb, globalColoring.w);
    fragColor = vec4(finalColor, c.w);
}
