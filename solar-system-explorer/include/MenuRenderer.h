#ifndef MENURENDERER_H
#define MENURENDERER_H

#include <glad/glad.h>
#include <string>
#include <map>
#include <glm/glm.hpp>
#include <ft2build.h>
#include FT_FREETYPE_H
#include "UIRenderer.h"

enum class MenuButton {
    START_GAME,
    SETTINGS,
    EXIT_GAME,
    NONE
};

enum class Language {
    ENGLISH,
    CHINESE
};

class MenuRenderer {
public:
    MenuRenderer();
    ~MenuRenderer();

    bool initialize(int screenWidth, int screenHeight);
    void render(int screenWidth, int screenHeight);
    void renderSettings(int screenWidth, int screenHeight);
    MenuButton checkButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight);
    MenuButton getHoveredButton(double mouseX, double mouseY, int screenWidth, int screenHeight);
    
    // Language settings
    void setLanguage(Language lang) { currentLanguage = lang; }
    Language getLanguage() const { return currentLanguage; }
    bool checkLanguageButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight);
    bool checkBackButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight);
    
    // Keyboard navigation
    void selectNext();
    void selectPrevious();
    MenuButton getSelectedButton() const;
    void activateSelected();
    
    // Settings page navigation
    void selectNextSetting();
    void selectPreviousSetting();
    int getSelectedSettingIndex() const { return selectedSettingIndex; }
    void activateSelectedSetting();

private:
    GLuint VAO, VBO;
    GLuint buttonVAO, buttonVBO;
    GLuint textShader;
    GLuint buttonShader;
    std::map<char, Character> characters;
    std::map<wchar_t, Character> chineseCharacters; // For Chinese character support

    struct ButtonInfo {
        float x, y;
        float width, height;
        std::string text;
        MenuButton id;
    };

    ButtonInfo startButton;
    ButtonInfo settingsButton;
    ButtonInfo exitButton;
    
    // Settings page buttons
    ButtonInfo englishButton;
    ButtonInfo chineseButton;
    ButtonInfo backButton;
    
    Language currentLanguage;
    int selectedMenuIndex; // 0=Start, 1=Settings, 2=Exit
    int selectedSettingIndex; // 0=English, 1=Chinese, 2=Back

    void renderText(const std::string& text, float x, float y, float scale, 
                   const glm::vec3& color, int screenWidth, int screenHeight);
    void renderButton(const ButtonInfo& button, bool hovered, int screenWidth, int screenHeight);
    void renderTriangle(float x, float y, float size, const glm::vec3& color, int screenWidth, int screenHeight);
    GLuint loadTextShader();
    GLuint loadButtonShader();
    void calculateButtonPositions(int screenWidth, int screenHeight);
};

#endif // MENURENDERER_H
