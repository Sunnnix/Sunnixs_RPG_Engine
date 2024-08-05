#version 330 core

in float type;

out vec4 fragColor;

uniform vec4 color;

void main() {
    if(type == 0)
        fragColor = (color + vec4(1.0, 1.0, 1.0, 1.0)) / 2.0;
    else
        fragColor = color;
}
