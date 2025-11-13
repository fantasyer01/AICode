#include "TextRenderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <vector>

TextRenderer::TextRenderer() : VAO(0), VBO(0), shaderProgram(0) {}

TextRenderer::~TextRenderer() {
    if (VAO) glDeleteVertexArrays(1, &VAO);
    if (VBO) glDeleteBuffers(1, &VBO);
}

bool TextRenderer::init(GLuint program) {
    shaderProgram = program;
    
    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    
    glBindVertexArray(VAO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float) * 6 * 6, NULL, GL_DYNAMIC_DRAW);
    
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3 * sizeof(float)));
    glEnableVertexAttribArray(1);
    
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
    
    return true;
}

void TextRenderer::renderText(const std::string& text, float x, float y, float scale, 
                              const glm::vec3& color, int screenWidth, int screenHeight) {
    glUseProgram(shaderProgram);
    
    // Set up orthographic projection
    glm::mat4 model = glm::mat4(1.0f);
    glm::mat4 view = glm::mat4(1.0f);
    glm::mat4 projection = glm::ortho(0.0f, (float)screenWidth, (float)screenHeight, 0.0f, -1.0f, 1.0f);
    
    GLuint modelLoc = glGetUniformLocation(shaderProgram, "model");
    GLuint viewLoc = glGetUniformLocation(shaderProgram, "view");
    GLuint projLoc = glGetUniformLocation(shaderProgram, "projection");
    
    glUniformMatrix4fv(modelLoc, 1, GL_FALSE, glm::value_ptr(model));
    glUniformMatrix4fv(viewLoc, 1, GL_FALSE, glm::value_ptr(view));
    glUniformMatrix4fv(projLoc, 1, GL_FALSE, glm::value_ptr(projection));
    
    glBindVertexArray(VAO);
    
    float currentX = x;
    float charWidth = 40.0f * scale;
    float charHeight = 60.0f * scale;
    float spacing = 10.0f * scale;
    
    for (char c : text) {
        if (c == ' ') {
            currentX += charWidth * 0.5f;
            continue;
        }
        
        renderChar(c, currentX, y, scale, color);
        currentX += charWidth + spacing;
    }
    
    glBindVertexArray(0);
}

void TextRenderer::renderChar(char c, float x, float y, float scale, const glm::vec3& color) {
    float w = 40.0f * scale;
    float h = 60.0f * scale;
    float thickness = 8.0f * scale;
    
    std::vector<float> vertices;
    
    // Simple geometric representation of each letter
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    
    switch(toupper(c)) {
        case 'A':
            // Vertical left line
            vertices = {
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical right line
            vertices = {
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Middle horizontal line
            vertices = {
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'E':
            // Vertical left line
            vertices = {
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Middle horizontal line
            vertices = {
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Bottom horizontal line
            vertices = {
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'G':
            // Vertical left line
            vertices = {
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Bottom horizontal line
            vertices = {
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical right line (bottom half)
            vertices = {
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Middle horizontal line (right half)
            vertices = {
                x + w/2, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'I':
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical center line
            vertices = {
                x + w/2 - thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y, 0.0f, color.r, color.g, color.b,
                x + w/2 - thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y, 0.0f, color.r, color.g, color.b,
                x + w/2 - thickness/2, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Bottom horizontal line
            vertices = {
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'M':
            // Vertical left line
            vertices = {
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical right line
            vertices = {
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Diagonal left (top-left to center)
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w/2 - thickness, y + h/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Diagonal right (center to top-right)
            vertices = {
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'R':
            // Vertical left line
            vertices = {
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical right line (top half)
            vertices = {
                x + w - thickness, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Middle horizontal line
            vertices = {
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Diagonal leg
            vertices = {
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'S':
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Left vertical (top half)
            vertices = {
                x, y + h/2, 0.0f, color.r, color.g, color.b,
                x + thickness, y + h/2, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y + h/2, 0.0f, color.r, color.g, color.b,
                x + thickness, y, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Middle horizontal line
            vertices = {
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 - thickness/2, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
                x, y + h/2 + thickness/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Right vertical (bottom half)
            vertices = {
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y + h/2, 0.0f, color.r, color.g, color.b,
                x + w - thickness, y + h/2, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Bottom horizontal line
            vertices = {
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h - thickness, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'T':
            // Top horizontal line
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + thickness, 0.0f, color.r, color.g, color.b,
                x, y + thickness, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Vertical center line
            vertices = {
                x + w/2 - thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y, 0.0f, color.r, color.g, color.b,
                x + w/2 - thickness/2, y + h, 0.0f, color.r, color.g, color.b,
                x + w/2 + thickness/2, y, 0.0f, color.r, color.g, color.b,
                x + w/2 - thickness/2, y, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        case 'X':
            // Diagonal from top-left to bottom-right
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + thickness * 1.5f, y, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x + w - thickness * 1.5f, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            
            // Diagonal from top-right to bottom-left
            vertices = {
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w - thickness * 1.5f, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
                x + thickness * 1.5f, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
            
        default:
            // Draw a simple rectangle for unknown characters
            vertices = {
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y, 0.0f, color.r, color.g, color.b,
                x + w, y + h, 0.0f, color.r, color.g, color.b,
                x, y + h, 0.0f, color.r, color.g, color.b,
            };
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.size() * sizeof(float), vertices.data());
            glDrawArrays(GL_TRIANGLES, 0, 6);
            break;
    }
}
