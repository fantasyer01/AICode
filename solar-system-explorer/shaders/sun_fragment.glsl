#version 330 core
out vec4 FragColor;

in vec3 FragPos;

uniform vec3 sunColor;

void main()
{
    // Emissive sun - no lighting calculations needed
    FragColor = vec4(sunColor, 1.0);
}
