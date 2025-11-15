#ifndef RENDERER_H
#define RENDERER_H

#include <glad/glad.h>
#include <glm/glm.hpp>
#include "Planet.h"
#include <vector>

class Renderer {
public:
    Renderer();
    ~Renderer();

    bool initialize();
    void renderPlanet(const Planet& planet, const glm::mat4& view, const glm::mat4& projection);
    void renderSkybox(const glm::mat4& view, const glm::mat4& projection);
    void renderOrbit(float orbitalRadius, const glm::mat4& view, const glm::mat4& projection);
    
private:
    GLuint sphereVAO, sphereVBO, sphereEBO;
    GLuint skyboxVAO, skyboxVBO;
    GLuint orbitVAO, orbitVBO;
    GLuint planetShader;
    GLuint sunShader;
    GLuint skyboxShader;
    GLuint orbitShader;
    
    unsigned int sphereIndexCount;
    
    void createSphere();
    void createSkybox();
    void createOrbitCircle();
    GLuint loadShader(const char* vertexPath, const char* fragmentPath);
    GLuint compileShader(GLenum type, const char* source);
};

#endif // RENDERER_H
