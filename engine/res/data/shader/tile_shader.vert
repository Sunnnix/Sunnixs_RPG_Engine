#version 330 core

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexPos0;
layout (location = 2) in vec2 aTexPos1;

out vec2 texPos0;
out vec2 texPos1;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(aPosition, 1.0);
    texPos0 = aTexPos0;
    texPos1 = aTexPos1;
}
