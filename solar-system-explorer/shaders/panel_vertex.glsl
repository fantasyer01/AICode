#version 330 core
layout (location = 0) in vec2 aPos;

uniform mat4 projection;
uniform vec2 position;
uniform vec2 size;

void main()
{
    vec2 vertexPos = position + aPos * size;
    gl_Position = projection * vec4(vertexPos, 0.0, 1.0);
}
