#version 330 core

in vec2 texPos;

out vec4 fragColor;

uniform sampler2D texSampler;

void main() {
    vec4 c = texture(texSampler, texPos);
    if(c.w == 0)
        discard;
    fragColor = c;
}
