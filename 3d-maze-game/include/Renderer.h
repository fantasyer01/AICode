#ifndef RENDERER_H
#define RENDERER_H

#include <glad/glad.h>
#include <glm/glm.hpp>
#include <vector>
#include "Maze.h"
#include "Player.h"

class Renderer {
public:
    Renderer();
    ~Renderer();
    
    bool init(const char* vertexPath, const char* fragmentPath);
    void renderMaze(const Maze& maze, const Player& player, int width, int height);
    void renderMinimap(const Maze& maze, const Player& player, int width, int height);
    void renderMenu(int selectedItem, int width, int height);
    void cleanup();
    
private:
    GLuint shaderProgram;
    GLuint VAO, VBO;
    GLuint minimapVAO, minimapVBO;
    
    GLuint loadShader(const char* path, GLenum type);
    GLuint createShaderProgram(const char* vertexPath, const char* fragmentPath);
    
    void setupMazeGeometry(const Maze& maze);
    void setupMinimapGeometry();
    
    std::vector<float> mazeVertices;
};

#endif
