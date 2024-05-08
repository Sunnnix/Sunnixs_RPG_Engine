#version 330 core

in vec2 texPos0;
in vec2 texPos1;

out vec4 fragColor;

uniform sampler2D texSampler;

void main() {
    vec4 c = vec4(0);
    if(texPos1.x != -1)
        c = texture(texSampler, texPos1);
    if(c.w == 0 && texPos0.x != -1)
        c = texture(texSampler, texPos0);
    if(c.w == 0)
        discard;
    fragColor = c;
}
