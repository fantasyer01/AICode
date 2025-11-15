#version 330 core
out vec4 FragColor;

in vec3 TexCoords;

void main()
{
    // Simple dark space background with stars effect
    vec3 coord = normalize(TexCoords);
    
    // Create a dark blue-black space color
    vec3 baseColor = vec3(0.01, 0.01, 0.05);
    
    // Simple star effect using procedural noise
    float starField = fract(sin(dot(coord.xy * 500.0, vec2(12.9898, 78.233))) * 43758.5453);
    if (starField > 0.998) {
        baseColor = vec3(1.0, 1.0, 1.0);
    }
    
    FragColor = vec4(baseColor, 1.0);
}
