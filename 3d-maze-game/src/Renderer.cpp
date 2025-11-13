#include "Renderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <fstream>
#include <sstream>
#include <iostream>

Renderer::Renderer() : shaderProgram(0), VAO(0), VBO(0), minimapVAO(0), minimapVBO(0) {}

Renderer::~Renderer() {
    cleanup();
}

bool Renderer::init(const char* vertexPath, const char* fragmentPath) {
    shaderProgram = createShaderProgram(vertexPath, fragmentPath);
    if (shaderProgram == 0) {
        return false;
    }
    
    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    
    glGenVertexArrays(1, &minimapVAO);
    glGenBuffers(1, &minimapVBO);
    
    return true;
}

GLuint Renderer::loadShader(const char* path, GLenum type) {
    std::ifstream file(path);
    if (!file.is_open()) {
        std::cerr << "Failed to open shader file: " << path << std::endl;
        return 0;
    }
    
    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string code = buffer.str();
    const char* codePtr = code.c_str();
    
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &codePtr, NULL);
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

GLuint Renderer::createShaderProgram(const char* vertexPath, const char* fragmentPath) {
    GLuint vertexShader = loadShader(vertexPath, GL_VERTEX_SHADER);
    GLuint fragmentShader = loadShader(fragmentPath, GL_FRAGMENT_SHADER);
    
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

void Renderer::renderMenu(int selectedItem, int width, int height) {
    glUseProgram(shaderProgram);
    
    // Set up orthographic projection for 2D menu
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = glm::mat4(1.0f);
    glm::mat4 projection = glm::ortho(0.0f, (float)width, (float)height, 0.0f, -1.0f, 1.0f);
    
    GLuint modelLoc = glGetUniformLocation(shaderProgram, "model");
    GLuint viewLoc = glGetUniformLocation(shaderProgram, "view");
    GLuint projLoc = glGetUniformLocation(shaderProgram, "projection");
    
    glUniformMatrix4fv(modelLoc, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(viewLoc, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projection));
    
    glBindVertexArray(minimapVAO);
    glBindBuffer(GL_ARRAY_BUFFER, minimapVBO);
    
    // Calculate centered positions
    float centerX = width / 2.0f;
    float centerY = height / 2.0f;
    
    // "START GAME" text position (upper center)
    float startTextY = centerY - 100.0f;
    
    // "EXIT GAME" text position (lower center)  
    float exitTextY = centerY + 50.0f;
    
    // Larger character boxes to look more like text
    float boxWidth = 80.0f;
    float boxHeight = 50.0f;
    float boxSpacing = 15.0f;
    
    // Colors based on selection
    float startR = (selectedItem == 0) ? 1.0f : 0.5f;
    float startG = (selectedItem == 0) ? 1.0f : 0.5f;
    float startB = (selectedItem == 0) ? 1.0f : 0.5f;
    
    float exitR = (selectedItem == 1) ? 1.0f : 0.5f;
    float exitG = (selectedItem == 1) ? 1.0f : 0.5f;
    float exitB = (selectedItem == 1) ? 1.0f : 0.5f;
    
    // Render "START GAME" text representation
    // Calculate total width for centering
    float startGameWidth = (boxWidth + boxSpacing) * 10; // "START GAME" = 10 letters
    float startX = centerX - startGameWidth / 2.0f;
    
    // Draw boxes representing "START GAME" text
    for (int i = 0; i < 10; i++) {
        float x = startX + i * (boxWidth + boxSpacing);
        
        std::vector<float> boxVertices = {
            x, startTextY, 0.0f, startR, startG, startB,
            x + boxWidth, startTextY, 0.0f, startR, startG, startB,
            x + boxWidth, startTextY + boxHeight, 0.0f, startR, startG, startB,
            x, startTextY, 0.0f, startR, startG, startB,
            x + boxWidth, startTextY + boxHeight, 0.0f, startR, startG, startB,
            x, startTextY + boxHeight, 0.0f, startR, startG, startB,
        };
        
        glBufferData(GL_ARRAY_BUFFER, boxVertices.size() * sizeof(float), boxVertices.data(), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Add spacing after 5th character (after "START")
        if (i == 4) {
            x += boxWidth; // Extra space between words
        }
    }
    
    // Render "EXIT GAME" text representation
    float exitGameWidth = (boxWidth + boxSpacing) * 9; // "EXIT GAME" = 9 letters
    float exitX = centerX - exitGameWidth / 2.0f;
    
    // Draw boxes representing "EXIT GAME" text
    for (int i = 0; i < 9; i++) {
        float x = exitX + i * (boxWidth + boxSpacing);
        
        std::vector<float> boxVertices = {
            x, exitTextY, 0.0f, exitR, exitG, exitB,
            x + boxWidth, exitTextY, 0.0f, exitR, exitG, exitB,
            x + boxWidth, exitTextY + boxHeight, 0.0f, exitR, exitG, exitB,
            x, exitTextY, 0.0f, exitR, exitG, exitB,
            x + boxWidth, exitTextY + boxHeight, 0.0f, exitR, exitG, exitB,
            x, exitTextY + boxHeight, 0.0f, exitR, exitG, exitB,
        };
        
        glBufferData(GL_ARRAY_BUFFER, boxVertices.size() * sizeof(float), boxVertices.data(), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Add spacing after 4th character (after "EXIT")
        if (i == 3) {
            x += boxWidth; // Extra space between words
        }
    }
    
    // Add yellow selection arrow indicator
    float indicatorY = (selectedItem == 0) ? startTextY + boxHeight / 2.0f : exitTextY + boxHeight / 2.0f;
    float indicatorX = (selectedItem == 0) ? startX - 60.0f : exitX - 60.0f;
    float arrowSize = 40.0f;
    
    std::vector<float> arrowVertices = {
        indicatorX, indicatorY - arrowSize/2.0f, 0.0f, 1.0f, 1.0f, 0.0f,
        indicatorX + arrowSize, indicatorY, 0.0f, 1.0f, 1.0f, 0.0f,
        indicatorX, indicatorY + arrowSize/2.0f, 0.0f, 1.0f, 1.0f, 0.0f,
    };
    
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
