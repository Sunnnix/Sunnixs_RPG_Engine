#version 330 core

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec4 aColors;
layout (location = 2) in vec2 aTexPos;

out vec4 colors;
out vec2 texPos;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(aPosition, 0.0, 1.0);
    colors = aColors;
    texPos = aTexPos;
}
