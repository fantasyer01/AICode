#ifndef TEXT_RENDERER_H
#define TEXT_RENDERER_H

#include <glad/glad.h>
#include <glm/glm.hpp>
#include <string>
#include <map>

struct Character {
    float vertices[24];  // 4 vertices * 6 floats (x, y, z, r, g, b)
};

class TextRenderer {
public:
    TextRenderer();
    ~TextRenderer();
    
    bool init(GLuint shaderProgram);
    void renderText(const std::string& text, float x, float y, float scale, 
                   const glm::vec3& color, int screenWidth, int screenHeight);
    
private:
    GLuint VAO, VBO;
    GLuint shaderProgram;
    std::map<char, Character> characters;
    
    void initCharacters();
    void renderChar(char c, float x, float y, float scale, const glm::vec3& color);
};

#endif
