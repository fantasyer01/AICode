#ifndef UIRENDERER_H
#define UIRENDERER_H

#include <glad/glad.h>
#include <string>
#include <map>
#include <vector>
#include <glm/glm.hpp>
#include <ft2build.h>
#include FT_FREETYPE_H

enum class Language; // Forward declaration

struct Character {
    GLuint textureID;
    glm::ivec2 size;
    glm::ivec2 bearing;
    unsigned int advance;
};

class UIRenderer {
public:
    UIRenderer();
    ~UIRenderer();

    bool initialize(int screenWidth, int screenHeight);
    void renderInfoCard(const std::string& planetName, int screenWidth, int screenHeight);
    void setCurrentPlanet(const std::string& planetName);
    void updateFade(float deltaTime);
    void setLanguage(Language lang);
    
private:
    GLuint VAO, VBO;
    GLuint panelVAO, panelVBO;
    GLuint textShader;
    GLuint panelShader;
    std::map<char, Character> characters;
    std::map<wchar_t, Character> chineseCharacters; // For Chinese character support
    
    // FreeType objects for on-demand glyph loading
    FT_Library ft;
    FT_Face face;
    bool freetypeInitialized;
    
    std::string currentPlanet;
    float fadeAlpha;
    bool fadingIn;
    
    struct PlanetInfo {
        std::string name;
        std::string nameChinese;
        std::vector<std::string> facts;
        std::vector<std::string> factsChinese;
    };
    
    std::map<std::string, PlanetInfo> planetData;
    Language currentLanguage;
    
    void loadPlanetData();
    void renderText(const std::string& text, float x, float y, float scale, const glm::vec3& color, int screenWidth, int screenHeight);
    void renderPanel(float x, float y, float width, float height, float alpha, int screenWidth, int screenHeight);
    GLuint loadTextShader();
    GLuint loadPanelShader();
    
    // On-demand character loading
    Character loadCharacter(wchar_t wc);
};

#endif // UIRENDERER_H
