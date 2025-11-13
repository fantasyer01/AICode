#include "Renderer.h"
#include "TextRenderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <iostream>

// Embedded shader sources
const char* vertexShaderSource = R"(
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;

out vec3 FragColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    FragColor = aColor;
}
)";

const char* fragmentShaderSource = R"(
#version 330 core
in vec3 FragColor;
out vec4 color;

void main()
{
    color = vec4(FragColor, 1.0);
}
)";

Renderer::Renderer() : shaderProgram(0), VAO(0), VBO(0), minimapVAO(0), minimapVBO(0) {}

Renderer::~Renderer() {
    cleanup();
}

bool Renderer::init(const char* vertexPath, const char* fragmentPath) {
    // Ignore the path parameters and use embedded shaders
    shaderProgram = createShaderProgramFromSource(vertexShaderSource, fragmentShaderSource);
    if (shaderProgram == 0) {
        return false;
    }
    
    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    
    glGenVertexArrays(1, &minimapVAO);
    glGenBuffers(1, &minimapVBO);
    
    return true;
}

GLuint Renderer::compileShader(const char* source, GLenum type) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    
    GLint success;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        std::cerr << "Shader compilation failed: " << infoLog << std::endl;
        return 0;
    }
    
    return shader;
}

GLuint Renderer::createShaderProgramFromSource(const char* vertexSource, const char* fragmentSource) {
    GLuint vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
    GLuint fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);
    
    if (vertexShader == 0 || fragmentShader == 0) {
        return 0;
    }
    
    GLuint program = glCreateProgram();
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    
    GLint success;
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Shader program linking failed: " << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    
    return program;
}

void Renderer::setupMazeGeometry(const Maze& maze) {
    mazeVertices.clear();
    
    float cellSize = maze.getCellSize();
    float wallHeight = 3.0f;
    float wallThickness = 0.2f;
    
    // Wall color (gray)
    float wallR = 0.6f, wallG = 0.6f, wallB = 0.6f;
    
    // Generate wall geometry
    for (int y = 0; y < maze.getHeight(); y++) {
        for (int x = 0; x < maze.getWidth(); x++) {
            const auto& cell = maze.getCells()[y][x];
            float posX = x * cellSize;
            float posZ = y * cellSize;
            
            // North wall
            if (cell.walls[0]) {
                // Front face
                mazeVertices.insert(mazeVertices.end(), {
                    posX, 0.0f, posZ, wallR, wallG, wallB,
                    posX + cellSize, 0.0f, posZ, wallR, wallG, wallB,
                    posX + cellSize, wallHeight, posZ, wallR, wallG, wallB,
                    posX, 0.0f, posZ, wallR, wallG, wallB,
                    posX + cellSize, wallHeight, posZ, wallR, wallG, wallB,
                    posX, wallHeight, posZ, wallR, wallG, wallB,
                });
            }
            
            // South wall
            if (cell.walls[2]) {
                float z = posZ + cellSize;
                mazeVertices.insert(mazeVertices.end(), {
                    posX, 0.0f, z, wallR, wallG, wallB,
                    posX, wallHeight, z, wallR, wallG, wallB,
                    posX + cellSize, wallHeight, z, wallR, wallG, wallB,
                    posX, 0.0f, z, wallR, wallG, wallB,
                    posX + cellSize, wallHeight, z, wallR, wallG, wallB,
                    posX + cellSize, 0.0f, z, wallR, wallG, wallB,
                });
            }
            
            // West wall
            if (cell.walls[3]) {
                mazeVertices.insert(mazeVertices.end(), {
                    posX, 0.0f, posZ, wallR, wallG, wallB,
                    posX, wallHeight, posZ, wallR, wallG, wallB,
                    posX, wallHeight, posZ + cellSize, wallR, wallG, wallB,
                    posX, 0.0f, posZ, wallR, wallG, wallB,
                    posX, wallHeight, posZ + cellSize, wallR, wallG, wallB,
                    posX, 0.0f, posZ + cellSize, wallR, wallG, wallB,
                });
            }
            
            // East wall
            if (cell.walls[1]) {
                float x = posX + cellSize;
                mazeVertices.insert(mazeVertices.end(), {
                    x, 0.0f, posZ, wallR, wallG, wallB,
                    x, 0.0f, posZ + cellSize, wallR, wallG, wallB,
                    x, wallHeight, posZ + cellSize, wallR, wallG, wallB,
                    x, 0.0f, posZ, wallR, wallG, wallB,
                    x, wallHeight, posZ + cellSize, wallR, wallG, wallB,
                    x, wallHeight, posZ, wallR, wallG, wallB,
                });
            }
        }
    }
    
    // Add floor
    float floorSize = maze.getWidth() * cellSize;
    float floorR = 0.3f, floorG = 0.3f, floorB = 0.3f;
    mazeVertices.insert(mazeVertices.end(), {
        0.0f, 0.0f, 0.0f, floorR, floorG, floorB,
        floorSize, 0.0f, 0.0f, floorR, floorG, floorB,
        floorSize, 0.0f, floorSize, floorR, floorG, floorB,
        0.0f, 0.0f, 0.0f, floorR, floorG, floorB,
        floorSize, 0.0f, floorSize, floorR, floorG, floorB,
        0.0f, 0.0f, floorSize, floorR, floorG, floorB,
    });
    
    // Add ceiling
    float ceilR = 0.4f, ceilG = 0.4f, ceilB = 0.4f;
    mazeVertices.insert(mazeVertices.end(), {
        0.0f, wallHeight, 0.0f, ceilR, ceilG, ceilB,
        floorSize, wallHeight, floorSize, ceilR, ceilG, ceilB,
        floorSize, wallHeight, 0.0f, ceilR, ceilG, ceilB,
        0.0f, wallHeight, 0.0f, ceilR, ceilG, ceilB,
        0.0f, wallHeight, floorSize, ceilR, ceilG, ceilB,
        floorSize, wallHeight, floorSize, ceilR, ceilG, ceilB,
    });
    
    // Add exit marker (red cube)
    glm::vec3 exitPos = maze.getExitPosition();
    float exitSize = 0.5f;
    float exitR = 1.0f, exitG = 0.0f, exitB = 0.0f;
    
    float ex = exitPos.x;
    float ey = exitPos.y;
    float ez = exitPos.z;
    float es = exitSize / 2.0f;
    
    // Simple cube for exit
    mazeVertices.insert(mazeVertices.end(), {
        ex - es, ey - es, ez - es, exitR, exitG, exitB,
        ex + es, ey - es, ez - es, exitR, exitG, exitB,
        ex + es, ey + es, ez - es, exitR, exitG, exitB,
        ex - es, ey - es, ez - es, exitR, exitG, exitB,
        ex + es, ey + es, ez - es, exitR, exitG, exitB,
        ex - es, ey + es, ez - es, exitR, exitG, exitB,
    });
    
    glBindVertexArray(VAO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, mazeVertices.size() * sizeof(float), mazeVertices.data(), GL_STATIC_DRAW);
    
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
    glEnableVertexAttribArray(1);
    
    glBindVertexArray(0);
}

void Renderer::renderMaze(const Maze& maze, const Player& player, int width, int height) {
    setupMazeGeometry(maze);
    
    glUseProgram(shaderProgram);
    
    // Set up matrices
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = player.getViewMatrix();
    glm::mat4 projection = glm::perspective(glm::radians(60.0f), (float)width / (float)height, 0.1f, 100.0f);
    
    GLuint modelLoc = glGetUniformLocation(shaderProgram, "model");
    GLuint viewLoc = glGetUniformLocation(shaderProgram, "view");
    GLuint projLoc = glGetUniformLocation(shaderProgram, "projection");
    
    glUniformMatrix4fv(modelLoc, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(viewLoc, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projection));
    
    glBindVertexArray(VAO);
    glDrawArrays(GL_TRIANGLES, 0, mazeVertices.size() / 6);
    glBindVertexArray(0);
}

void Renderer::renderMinimap(const Maze& maze, const Player& player, int width, int height) {
    // Save current viewport
    GLint viewport[4];
    glGetIntegerv(GL_VIEWPORT, viewport);
    
    // Set minimap viewport (upper-left corner)
    int minimapSize = width / 5;  // 20% of screen width
    glViewport(10, height - minimapSize - 10, minimapSize, minimapSize);
    
    // Disable depth test for 2D rendering
    glDisable(GL_DEPTH_TEST);
    
    glUseProgram(shaderProgram);
    
    // Set up orthographic projection for minimap
    float mazeWidth = maze.getWidth() * maze.getCellSize();
    float mazeHeight = maze.getHeight() * maze.getCellSize();
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = glm::mat4(1.0f);
    glm::mat4 projection = glm::ortho(0.0f, mazeWidth, mazeHeight, 0.0f, -1.0f, 1.0f);
    
    GLuint modelLoc = glGetUniformLocation(shaderProgram, "model");
    GLuint viewLoc = glGetUniformLocation(shaderProgram, "view");
    GLuint projLoc = glGetUniformLocation(shaderProgram, "projection");
    
    glUniformMatrix4fv(modelLoc, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(viewLoc, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projection));
    
    // Draw background
    std::vector<float> bgVertices = {
        0.0f, 0.0f, 0.0f, 0.2f, 0.2f, 0.2f,
        mazeWidth, 0.0f, 0.0f, 0.2f, 0.2f, 0.2f,
        mazeWidth, mazeHeight, 0.0f, 0.2f, 0.2f, 0.2f,
        0.0f, 0.0f, 0.0f, 0.2f, 0.2f, 0.2f,
        mazeWidth, mazeHeight, 0.0f, 0.2f, 0.2f, 0.2f,
        0.0f, mazeHeight, 0.0f, 0.2f, 0.2f, 0.2f,
    };
    
    glBindVertexArray(minimapVAO);
    glBindBuffer(GL_ARRAY_BUFFER, minimapVBO);
    glBufferData(GL_ARRAY_BUFFER, bgVertices.size() * sizeof(float), bgVertices.data(), GL_DYNAMIC_DRAW);
    
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
    glEnableVertexAttribArray(1);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
    
    // Draw walls
    std::vector<float> wallVertices;
    float cellSize = maze.getCellSize();
    float wallThickness = cellSize * 0.1f;
    
    for (int y = 0; y < maze.getHeight(); y++) {
        for (int x = 0; x < maze.getWidth(); x++) {
            const auto& cell = maze.getCells()[y][x];
            float posX = x * cellSize;
            float posZ = y * cellSize;
            
            // North wall
            if (cell.walls[0]) {
                wallVertices.insert(wallVertices.end(), {
                    posX, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + cellSize, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + cellSize, posZ + wallThickness, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + cellSize, posZ + wallThickness, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX, posZ + wallThickness, 0.0f, 0.8f, 0.8f, 0.8f,
                });
            }
            
            // West wall
            if (cell.walls[3]) {
                wallVertices.insert(wallVertices.end(), {
                    posX, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + wallThickness, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + wallThickness, posZ + cellSize, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX, posZ, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX + wallThickness, posZ + cellSize, 0.0f, 0.8f, 0.8f, 0.8f,
                    posX, posZ + cellSize, 0.0f, 0.8f, 0.8f, 0.8f,
                });
            }
        }
    }
    
    if (!wallVertices.empty()) {
        glBufferData(GL_ARRAY_BUFFER, wallVertices.size() * sizeof(float), wallVertices.data(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, wallVertices.size() / 6);
    }
    
    // Draw exit marker
    glm::vec3 exitPos = maze.getExitPosition();
    float markerSize = cellSize * 0.3f;
    std::vector<float> exitVertices = {
        exitPos.x - markerSize, exitPos.z - markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
        exitPos.x + markerSize, exitPos.z - markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
        exitPos.x + markerSize, exitPos.z + markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
        exitPos.x - markerSize, exitPos.z - markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
        exitPos.x + markerSize, exitPos.z + markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
        exitPos.x - markerSize, exitPos.z + markerSize, 0.0f, 1.0f, 0.0f, 0.0f,
    };
    
    glBufferData(GL_ARRAY_BUFFER, exitVertices.size() * sizeof(float), exitVertices.data(), GL_DYNAMIC_DRAW);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    
    // Draw player marker
    glm::vec3 playerPos = player.getPosition();
    float playerSize = cellSize * 0.2f;
    std::vector<float> playerVertices = {
        playerPos.x - playerSize, playerPos.z - playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
        playerPos.x + playerSize, playerPos.z - playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
        playerPos.x + playerSize, playerPos.z + playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
        playerPos.x - playerSize, playerPos.z - playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
        playerPos.x + playerSize, playerPos.z + playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
        playerPos.x - playerSize, playerPos.z + playerSize, 0.0f, 0.0f, 1.0f, 0.0f,
    };
    
    glBufferData(GL_ARRAY_BUFFER, playerVertices.size() * sizeof(float), playerVertices.data(), GL_DYNAMIC_DRAW);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    
    glBindVertexArray(0);
    
    // Restore viewport and depth test
    glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    glEnable(GL_DEPTH_TEST);
}

void Renderer::renderMenu(int selectedItem, int width, int height, TextRenderer* textRenderer) {
    // Calculate centered positions
    float centerX = width / 2.0f;
    float centerY = height / 2.0f;
    
    // Text scale and positioning
    float textScale = 1.0f;
    
    // "START GAME" position (upper center)
    float startTextY = centerY - 100.0f;
    std::string startText = "START GAME";
    
    // "EXIT GAME" position (lower center)
    float exitTextY = centerY + 50.0f;
    std::string exitText = "EXIT GAME";
    
    // Calculate text width for centering (approximate)
    float charWidth = 40.0f * textScale;
    float spacing = 10.0f * textScale;
    float startTextWidth = (charWidth + spacing) * startText.length();
    float exitTextWidth = (charWidth + spacing) * exitText.length();
    
    // Colors based on selection
    glm::vec3 startColor = (selectedItem == 0) ? glm::vec3(1.0f, 1.0f, 1.0f) : glm::vec3(0.5f, 0.5f, 0.5f);
    glm::vec3 exitColor = (selectedItem == 1) ? glm::vec3(1.0f, 1.0f, 1.0f) : glm::vec3(0.5f, 0.5f, 0.5f);
    
    // Render text using TextRenderer
    if (textRenderer) {
        textRenderer->renderText(startText, centerX - startTextWidth / 2.0f, startTextY, textScale, startColor, width, height);
        textRenderer->renderText(exitText, centerX - exitTextWidth / 2.0f, exitTextY, textScale, exitColor, width, height);
    }
    
    // Render selection arrow
    glUseProgram(shaderProgram);
    
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = glm::mat4(1.0f);
    glm::mat4 projection = glm::ortho(0.0f, (float)width, (float)height, 0.0f, -1.0f, 1.0f);
    
    GLuint modelLoc = glGetUniformLocation(shaderProgram, "model");
    GLuint viewLoc = glGetUniformLocation(shaderProgram, "view");
    GLuint projLoc = glGetUniformLocation(shaderProgram, "projection");
    
    glUniformMatrix4fv(modelLoc, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(viewLoc, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projection));
    
    // Yellow selection arrow indicator
    float indicatorY = (selectedItem == 0) ? startTextY + 30.0f : exitTextY + 30.0f;
    float indicatorX = (selectedItem == 0) ? centerX - startTextWidth / 2.0f - 60.0f : centerX - exitTextWidth / 2.0f - 60.0f;
    float arrowSize = 40.0f;
    
    std::vector<float> arrowVertices = {
        indicatorX, indicatorY - arrowSize/2.0f, 0.0f, 1.0f, 1.0f, 0.0f,
        indicatorX + arrowSize, indicatorY, 0.0f, 1.0f, 1.0f, 0.0f,
        indicatorX, indicatorY + arrowSize/2.0f, 0.0f, 1.0f, 1.0f, 0.0f,
    };
    
    glBindVertexArray(minimapVAO);
    glBindBuffer(GL_ARRAY_BUFFER, minimapVBO);
    glBufferData(GL_ARRAY_BUFFER, arrowVertices.size() * sizeof(float), arrowVertices.data(), GL_DYNAMIC_DRAW);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
    glEnableVertexAttribArray(1);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    
    glBindVertexArray(0);
}

void Renderer::cleanup() {
    if (VAO) glDeleteVertexArrays(1, &VAO);
    if (VBO) glDeleteBuffers(1, &VBO);
    if (minimapVAO) glDeleteVertexArrays(1, &minimapVAO);
    if (minimapVBO) glDeleteBuffers(1, &minimapVBO);
    if (shaderProgram) glDeleteProgram(shaderProgram);
}
