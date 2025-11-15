#include "Renderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <fstream>
#include <sstream>
#include <iostream>
#include <vector>
#include <cmath>

Renderer::Renderer() 
    : sphereVAO(0), sphereVBO(0), sphereEBO(0),
      skyboxVAO(0), skyboxVBO(0),
      orbitVAO(0), orbitVBO(0),
      planetShader(0), sunShader(0), skyboxShader(0), orbitShader(0),
      sphereIndexCount(0) {
}

Renderer::~Renderer() {
    if (sphereVAO) glDeleteVertexArrays(1, &sphereVAO);
    if (sphereVBO) glDeleteBuffers(1, &sphereVBO);
    if (sphereEBO) glDeleteBuffers(1, &sphereEBO);
    if (skyboxVAO) glDeleteVertexArrays(1, &skyboxVAO);
    if (skyboxVBO) glDeleteBuffers(1, &skyboxVBO);
    if (orbitVAO) glDeleteVertexArrays(1, &orbitVAO);
    if (orbitVBO) glDeleteBuffers(1, &orbitVBO);
    if (planetShader) glDeleteProgram(planetShader);
    if (sunShader) glDeleteProgram(sunShader);
    if (skyboxShader) glDeleteProgram(skyboxShader);
    if (orbitShader) glDeleteProgram(orbitShader);
}

bool Renderer::initialize() {
    planetShader = loadShader("shaders/planet_vertex.glsl", "shaders/planet_fragment.glsl");
    sunShader = loadShader("shaders/sun_vertex.glsl", "shaders/sun_fragment.glsl");
    skyboxShader = loadShader("shaders/skybox_vertex.glsl", "shaders/skybox_fragment.glsl");
    orbitShader = loadShader("shaders/orbit_vertex.glsl", "shaders/orbit_fragment.glsl");
    
    if (!planetShader || !sunShader || !skyboxShader || !orbitShader) {
        return false;
    }
    
    createSphere();
    createSkybox();
    createOrbitCircle();
    
    return true;
}

void Renderer::renderPlanet(const Planet& planet, const glm::mat4& view, const glm::mat4& projection) {
    GLuint shader = planet.getIsSun() ? sunShader : planetShader;
    glUseProgram(shader);
    
    // Create model matrix
    glm::mat4 model = glm::mat4(1.0f);
    model = glm::translate(model, planet.getPosition());
    model = glm::scale(model, glm::vec3(planet.getRadius()));
    
    // Set uniforms
    glUniformMatrix4fv(glGetUniformLocation(shader, "model"), 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(glGetUniformLocation(shader, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(shader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    if (planet.getIsSun()) {
        glUniform3fv(glGetUniformLocation(shader, "sunColor"), 1, glm::value_ptr(planet.getColor()));
    } else {
        glUniform3fv(glGetUniformLocation(shader, "planetColor"), 1, glm::value_ptr(planet.getColor()));
        glUniform3f(glGetUniformLocation(shader, "lightPos"), 0.0f, 0.0f, 0.0f); // Sun at origin
        
        // We'll pass camera position from the main loop - for now use a default
        glUniform3f(glGetUniformLocation(shader, "viewPos"), 0.0f, 5.0f, 20.0f);
    }
    
    // Draw sphere
    glBindVertexArray(sphereVAO);
    glDrawElements(GL_TRIANGLES, sphereIndexCount, GL_UNSIGNED_INT, 0);
    glBindVertexArray(0);
}

void Renderer::renderSkybox(const glm::mat4& view, const glm::mat4& projection) {
    glDepthFunc(GL_LEQUAL);
    glUseProgram(skyboxShader);
    
    glUniformMatrix4fv(glGetUniformLocation(skyboxShader, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(skyboxShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    glBindVertexArray(skyboxVAO);
    glDrawArrays(GL_TRIANGLES, 0, 36);
    glBindVertexArray(0);
    
    glDepthFunc(GL_LESS);
}

void Renderer::createSphere() {
    std::vector<float> vertices;
    std::vector<unsigned int> indices;
    
    const unsigned int X_SEGMENTS = 32;
    const unsigned int Y_SEGMENTS = 32;
    const float PI = 3.14159265359f;
    
    for (unsigned int y = 0; y <= Y_SEGMENTS; ++y) {
        for (unsigned int x = 0; x <= X_SEGMENTS; ++x) {
            float xSegment = (float)x / (float)X_SEGMENTS;
            float ySegment = (float)y / (float)Y_SEGMENTS;
            float xPos = std::cos(xSegment * 2.0f * PI) * std::sin(ySegment * PI);
            float yPos = std::cos(ySegment * PI);
            float zPos = std::sin(xSegment * 2.0f * PI) * std::sin(ySegment * PI);
            
            vertices.push_back(xPos);
            vertices.push_back(yPos);
            vertices.push_back(zPos);
            vertices.push_back(xPos); // Normal X
            vertices.push_back(yPos); // Normal Y
            vertices.push_back(zPos); // Normal Z
        }
    }
    
    for (unsigned int y = 0; y < Y_SEGMENTS; ++y) {
        for (unsigned int x = 0; x < X_SEGMENTS; ++x) {
            indices.push_back(y * (X_SEGMENTS + 1) + x);
            indices.push_back((y + 1) * (X_SEGMENTS + 1) + x);
            indices.push_back((y + 1) * (X_SEGMENTS + 1) + x + 1);
            
            indices.push_back(y * (X_SEGMENTS + 1) + x);
            indices.push_back((y + 1) * (X_SEGMENTS + 1) + x + 1);
            indices.push_back(y * (X_SEGMENTS + 1) + x + 1);
        }
    }
    
    sphereIndexCount = indices.size();
    
    glGenVertexArrays(1, &sphereVAO);
    glGenBuffers(1, &sphereVBO);
    glGenBuffers(1, &sphereEBO);
    
    glBindVertexArray(sphereVAO);
    
    glBindBuffer(GL_ARRAY_BUFFER, sphereVBO);
    glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), &vertices[0], GL_STATIC_DRAW);
    
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereEBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned int), &indices[0], GL_STATIC_DRAW);
    
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
    
    glBindVertexArray(0);
}

void Renderer::createSkybox() {
    float skyboxVertices[] = {
        -1.0f,  1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,

        -1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f, -1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,

        -1.0f, -1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

        -1.0f,  1.0f, -1.0f,
         1.0f,  1.0f, -1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f,  1.0f,
        -1.0f,  1.0f, -1.0f,

        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
         1.0f, -1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f,  1.0f,
         1.0f, -1.0f,  1.0f
    };
    
    glGenVertexArrays(1, &skyboxVAO);
    glGenBuffers(1, &skyboxVBO);
    
    glBindVertexArray(skyboxVAO);
    glBindBuffer(GL_ARRAY_BUFFER, skyboxVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(skyboxVertices), &skyboxVertices, GL_STATIC_DRAW);
    
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void*)0);
    
    glBindVertexArray(0);
}

GLuint Renderer::loadShader(const char* vertexPath, const char* fragmentPath) {
    std::string vertexCode;
    std::string fragmentCode;
    std::ifstream vShaderFile;
    std::ifstream fShaderFile;
    
    vShaderFile.open(vertexPath);
    fShaderFile.open(fragmentPath);
    
    if (!vShaderFile.is_open() || !fShaderFile.is_open()) {
        std::cerr << "Failed to open shader files: " << vertexPath << " or " << fragmentPath << std::endl;
        return 0;
    }
    
    std::stringstream vShaderStream, fShaderStream;
    vShaderStream << vShaderFile.rdbuf();
    fShaderStream << fShaderFile.rdbuf();
    vShaderFile.close();
    fShaderFile.close();
    
    vertexCode = vShaderStream.str();
    fragmentCode = fShaderStream.str();
    
    const char* vShaderCode = vertexCode.c_str();
    const char* fShaderCode = fragmentCode.c_str();
    
    GLuint vertex = compileShader(GL_VERTEX_SHADER, vShaderCode);
    GLuint fragment = compileShader(GL_FRAGMENT_SHADER, fShaderCode);
    
    if (!vertex || !fragment) {
        return 0;
    }
    
    GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    
    GLint success;
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Shader program linking failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    
    return program;
}

GLuint Renderer::compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    
    GLint success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        std::cerr << "Shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    return shader;
}

void Renderer::createOrbitCircle() {
    const int segments = 128;
    std::vector<float> vertices;
    const float PI = 3.14159265359f;
    
    for (int i = 0; i <= segments; ++i) {
        float angle = 2.0f * PI * i / segments;
        vertices.push_back(cos(angle));
        vertices.push_back(0.0f);
        vertices.push_back(sin(angle));
    }
    
    glGenVertexArrays(1, &orbitVAO);
    glGenBuffers(1, &orbitVBO);
    
    glBindVertexArray(orbitVAO);
    glBindBuffer(GL_ARRAY_BUFFER, orbitVBO);
    glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(float), &vertices[0], GL_STATIC_DRAW);
    
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), (void*)0);
    
    glBindVertexArray(0);
}

void Renderer::renderOrbit(float orbitalRadius, const glm::mat4& view, const glm::mat4& projection) {
    if (orbitalRadius == 0.0f) return; // Don't draw orbit for the Sun
    
    glUseProgram(orbitShader);
    
    // Create model matrix - scale to orbital radius
    glm::mat4 model = glm::mat4(1.0f);
    model = glm::scale(model, glm::vec3(orbitalRadius, 1.0f, orbitalRadius));
    
    glUniformMatrix4fv(glGetUniformLocation(orbitShader, "model"), 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(glGetUniformLocation(orbitShader, "view"), 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(glGetUniformLocation(orbitShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    // White color for orbits
    glUniform3f(glGetUniformLocation(orbitShader, "orbitColor"), 1.0f, 1.0f, 1.0f);
    
    glBindVertexArray(orbitVAO);
    glDrawArrays(GL_LINE_LOOP, 0, 129);
    glBindVertexArray(0);
}
