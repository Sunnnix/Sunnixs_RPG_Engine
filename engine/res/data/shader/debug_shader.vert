#version 330 core

layout (location = 0) in vec3 aPosition;

out float type;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(aPosition.xy, 0.0, 1.0);
    type = aPosition.z;
}
