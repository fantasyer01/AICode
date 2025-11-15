#include "MenuRenderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <fstream>
#include <sstream>
#include <iostream>

MenuRenderer::MenuRenderer() 
    : VAO(0), VBO(0), buttonVAO(0), buttonVBO(0),
      textShader(0), buttonShader(0), currentLanguage(Language::ENGLISH), 
      selectedMenuIndex(0), selectedSettingIndex(0) {
}

MenuRenderer::~MenuRenderer() {
    if (VAO) glDeleteVertexArrays(1, &VAO);
    if (VBO) glDeleteBuffers(1, &VBO);
    if (buttonVAO) glDeleteVertexArrays(1, &buttonVAO);
    if (buttonVBO) glDeleteBuffers(1, &buttonVBO);
    if (textShader) glDeleteProgram(textShader);
    if (buttonShader) glDeleteProgram(buttonShader);
}

bool MenuRenderer::initialize(int screenWidth, int screenHeight) {
    // Initialize FreeType
    FT_Library ft;
    if (FT_Init_FreeType(&ft)) {
        std::cerr << "ERROR::FREETYPE: Could not init FreeType Library" << std::endl;
        return false;
    }
    
    // Load font - Use Microsoft YaHei for Chinese and English support
    FT_Face face;
    if (FT_New_Face(ft, "C:/Windows/Fonts/msyh.ttc", 0, &face)) {
        std::cerr << "ERROR::FREETYPE: Failed to load Microsoft YaHei font" << std::endl;
        std::cerr << "Trying fallback font SimHei..." << std::endl;
        // Fallback to SimHei if YaHei not available
        if (FT_New_Face(ft, "C:/Windows/Fonts/simhei.ttf", 0, &face)) {
            std::cerr << "ERROR::FREETYPE: Failed to load fallback font" << std::endl;
            return false;
        }
    }
    
    // Set font size (larger for menu)
    FT_Set_Pixel_Sizes(face, 0, 48);
    
    // Disable byte-alignment restriction
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    
    // Load ASCII characters (0-128)
    for (unsigned char c = 0; c < 128; c++) {
        if (FT_Load_Char(face, c, FT_LOAD_RENDER)) {
            std::cerr << "ERROR::FREETYPE: Failed to load Glyph " << c << std::endl;
            continue;
        }
        
        GLuint texture;
        glGenTextures(1, &texture);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RED,
            face->glyph->bitmap.width,
            face->glyph->bitmap.rows,
            0,
            GL_RED,
            GL_UNSIGNED_BYTE,
            face->glyph->bitmap.buffer
        );
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        Character character = {
            texture,
            glm::ivec2(face->glyph->bitmap.width, face->glyph->bitmap.rows),
            glm::ivec2(face->glyph->bitmap_left, face->glyph->bitmap_top),
            static_cast<unsigned int>(face->glyph->advance.x)
        };
        characters.insert(std::pair<char, Character>(c, character));
    }
    
    // Load common Chinese characters (simplified Chinese range)
    // Common Chinese characters are in Unicode range U+4E00 to U+9FFF
    // We'll load a subset for performance - extend as needed
    std::wstring chineseChars = L"太阳系漫游者水金地火木土天王海星选择语言设置开始游戏退出英文中距离行第近直径千米已知唯生命质量表面温度占有大气颗卫红色风暴环美丽冰巨";
    for (wchar_t wc : chineseChars) {
        if (FT_Load_Char(face, wc, FT_LOAD_RENDER)) {
            std::wcerr << L"ERROR::FREETYPE: Failed to load Chinese Glyph " << wc << std::endl;
            continue;
        }
        
        GLuint texture;
        glGenTextures(1, &texture);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RED,
            face->glyph->bitmap.width,
            face->glyph->bitmap.rows,
            0,
            GL_RED,
            GL_UNSIGNED_BYTE,
            face->glyph->bitmap.buffer
        );
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        Character character = {
            texture,
            glm::ivec2(face->glyph->bitmap.width, face->glyph->bitmap.rows),
            glm::ivec2(face->glyph->bitmap_left, face->glyph->bitmap_top),
            static_cast<unsigned int>(face->glyph->advance.x)
        };
        chineseCharacters.insert(std::pair<wchar_t, Character>(wc, character));
    }
    
    FT_Done_Face(face);
    FT_Done_FreeType(ft);
    
    // Load shaders
    textShader = loadTextShader();
    buttonShader = loadButtonShader();
    if (!textShader || !buttonShader) {
        return false;
    }
    
    // Setup VAO/VBO for text rendering
    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    glBindVertexArray(VAO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float) * 6 * 4, NULL, GL_DYNAMIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), 0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
    
    // Setup VAO/VBO for button rendering
    float buttonVertices[] = {
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
        
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };
    
    glGenVertexArrays(1, &buttonVAO);
    glGenBuffers(1, &buttonVBO);
    glBindVertexArray(buttonVAO);
    glBindBuffer(GL_ARRAY_BUFFER, buttonVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(buttonVertices), buttonVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glBindVertexArray(0);
    
    // Calculate button positions
    calculateButtonPositions(screenWidth, screenHeight);
    
    return true;
}

void MenuRenderer::calculateButtonPositions(int screenWidth, int screenHeight) {
    float buttonWidth = 300.0f;
    float buttonHeight = 80.0f;
    float centerX = screenWidth / 2.0f - buttonWidth / 2.0f;
    float spacing = 100.0f;
    
    // Start Game button (top button)
    startButton.x = centerX;
    startButton.y = screenHeight / 2.0f + spacing;
    startButton.width = buttonWidth;
    startButton.height = buttonHeight;
    startButton.text = "Start Game";
    startButton.id = MenuButton::START_GAME;
    
    // Settings button (middle button)
    settingsButton.x = centerX;
    settingsButton.y = screenHeight / 2.0f;
    settingsButton.width = buttonWidth;
    settingsButton.height = buttonHeight;
    settingsButton.text = "Settings";
    settingsButton.id = MenuButton::SETTINGS;
    
    // Exit Game button (bottom button)
    exitButton.x = centerX;
    exitButton.y = screenHeight / 2.0f - spacing;
    exitButton.width = buttonWidth;
    exitButton.height = buttonHeight;
    exitButton.text = "Exit Game";
    exitButton.id = MenuButton::EXIT_GAME;
    
    // Settings page buttons
    float langButtonWidth = 200.0f;
    float langButtonHeight = 70.0f;
    float langCenterX = screenWidth / 2.0f - langButtonWidth / 2.0f;
    
    englishButton.x = langCenterX;
    englishButton.y = screenHeight / 2.0f + 60.0f;
    englishButton.width = langButtonWidth;
    englishButton.height = langButtonHeight;
    englishButton.text = "English";
    
    chineseButton.x = langCenterX;
    chineseButton.y = screenHeight / 2.0f - 60.0f;
    chineseButton.width = langButtonWidth;
    chineseButton.height = langButtonHeight;
    chineseButton.text = "Chinese";
}

void MenuRenderer::render(int screenWidth, int screenHeight) {
    // Disable depth testing for 2D menu rendering
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
    // Recalculate button positions in case window was resized
    calculateButtonPositions(screenWidth, screenHeight);
    
    // Render title
    std::string title = "Solar System Explorer";
    float titleScale = 1.5f;
    
    // Calculate actual title width by measuring the text
    float titleWidth = 0.0f;
    for (char c : title) {
        if (characters.find(c) != characters.end()) {
            Character ch = characters[c];
            titleWidth += (ch.advance >> 6) * titleScale;
        }
    }
    
    // Center the title horizontally
    float titleX = (screenWidth - titleWidth) / 2.0f;
    float titleY = screenHeight - 100.0f; // Position near top
    
    glm::vec3 titleColor(1.0f, 0.8f, 0.2f); // Gold color
    renderText(title, titleX, titleY, titleScale, titleColor, screenWidth, screenHeight);
    
    // Render buttons with selection highlight
    renderButton(startButton, selectedMenuIndex == 0, screenWidth, screenHeight);
    renderButton(settingsButton, selectedMenuIndex == 1, screenWidth, screenHeight);
    renderButton(exitButton, selectedMenuIndex == 2, screenWidth, screenHeight);
    
    // Render selection triangle indicator
    float triangleSize = 20.0f;
    float triangleX = startButton.x - 40.0f; // Position to the left of buttons
    float triangleY = 0.0f;
    
    // Position triangle based on selected menu
    switch(selectedMenuIndex) {
        case 0: triangleY = startButton.y + startButton.height / 2.0f; break;
        case 1: triangleY = settingsButton.y + settingsButton.height / 2.0f; break;
        case 2: triangleY = exitButton.y + exitButton.height / 2.0f; break;
    }
    
    glm::vec3 triangleColor(1.0f, 0.8f, 0.2f); // Gold color matching title
    renderTriangle(triangleX, triangleY, triangleSize, triangleColor, screenWidth, screenHeight);
    
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);
}

void MenuRenderer::renderSettings(int screenWidth, int screenHeight) {
    // Disable depth testing for 2D menu rendering
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
    // Recalculate button positions in case window was resized
    calculateButtonPositions(screenWidth, screenHeight);
    
    // Render title
    std::string title = "Settings";
    float titleScale = 1.5f;
    
    // Calculate actual title width
    float titleWidth = 0.0f;
    for (char c : title) {
        if (characters.find(c) != characters.end()) {
            Character ch = characters[c];
            titleWidth += (ch.advance >> 6) * titleScale;
        }
    }
    
    // Center the title horizontally
    float titleX = (screenWidth - titleWidth) / 2.0f;
    float titleY = screenHeight - 100.0f;
    
    glm::vec3 titleColor(1.0f, 0.8f, 0.2f); // Gold color
    renderText(title, titleX, titleY, titleScale, titleColor, screenWidth, screenHeight);
    
    // Render subtitle
    std::string subtitle = "Select Language:";
    float subtitleScale = 0.9f;
    float subtitleWidth = 0.0f;
    for (char c : subtitle) {
        if (characters.find(c) != characters.end()) {
            Character ch = characters[c];
            subtitleWidth += (ch.advance >> 6) * subtitleScale;
        }
    }
    float subtitleX = (screenWidth - subtitleWidth) / 2.0f;
    float subtitleY = screenHeight - 200.0f;
    glm::vec3 subtitleColor(0.9f, 0.9f, 0.9f);
    renderText(subtitle, subtitleX, subtitleY, subtitleScale, subtitleColor, screenWidth, screenHeight);
    
    // Render language selection buttons
    bool isEnglish = (currentLanguage == Language::ENGLISH);
    bool isChinese = (currentLanguage == Language::CHINESE);
    
    // Highlight selected language with selection index
    renderButton(englishButton, selectedSettingIndex == 0, screenWidth, screenHeight);
    renderButton(chineseButton, selectedSettingIndex == 1, screenWidth, screenHeight);
    
    // Render selection triangle indicator
    float triangleSize = 20.0f;
    float triangleX = englishButton.x - 40.0f; // Position to the left of buttons
    float triangleY = 0.0f;
    
    // Position triangle based on selected setting
    switch(selectedSettingIndex) {
        case 0: triangleY = englishButton.y + englishButton.height / 2.0f; break;
        case 1: triangleY = chineseButton.y + chineseButton.height / 2.0f; break;
    }
    
    glm::vec3 triangleColor(1.0f, 0.8f, 0.2f); // Gold color matching title
    renderTriangle(triangleX, triangleY, triangleSize, triangleColor, screenWidth, screenHeight);
    
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);
}

void MenuRenderer::renderButton(const ButtonInfo& button, bool hovered, int screenWidth, int screenHeight) {
    glUseProgram(buttonShader);
    
    // Create orthographic projection
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(screenWidth), 
                                      0.0f, static_cast<float>(screenHeight));
    
    glUniformMatrix4fv(glGetUniformLocation(buttonShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniform2f(glGetUniformLocation(buttonShader, "position"), button.x, button.y);
    glUniform2f(glGetUniformLocation(buttonShader, "size"), button.width, button.height);
    
    // Button color (brighter if hovered)
    glm::vec4 buttonColor = hovered ? 
        glm::vec4(0.3f, 0.5f, 0.8f, 0.9f) : 
        glm::vec4(0.2f, 0.3f, 0.6f, 0.8f);
    glUniform4fv(glGetUniformLocation(buttonShader, "panelColor"), 1, glm::value_ptr(buttonColor));
    
    glBindVertexArray(buttonVAO);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
    
    // Render button text
    float textScale = 0.8f;
    float textWidth = button.text.length() * 20.0f * textScale; // Approximate
    float textX = button.x + (button.width - textWidth) / 2.0f;
    float textY = button.y + button.height / 2.0f - 15.0f;
    
    glm::vec3 textColor(1.0f, 1.0f, 1.0f);
    renderText(button.text, textX, textY, textScale, textColor, screenWidth, screenHeight);
}

void MenuRenderer::renderText(const std::string& text, float x, float y, float scale, 
                              const glm::vec3& color, int screenWidth, int screenHeight) {
    glUseProgram(textShader);
    
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(screenWidth), 
                                      0.0f, static_cast<float>(screenHeight));
    glUniformMatrix4fv(glGetUniformLocation(textShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniform3f(glGetUniformLocation(textShader, "textColor"), color.x, color.y, color.z);
    
    glActiveTexture(GL_TEXTURE0);
    glBindVertexArray(VAO);

    // Convert UTF-8 string to wide string for proper character iteration
    std::wstring wtext;
    size_t i = 0;
    while (i < text.length()) {
        unsigned char c = text[i];
        if (c < 0x80) {
            // Single-byte character (ASCII)
            wtext += static_cast<wchar_t>(c);
            i++;
        } else if ((c & 0xE0) == 0xC0) {
            // Two-byte character
            if (i + 1 < text.length()) {
                wchar_t wc = ((c & 0x1F) << 6) | (text[i+1] & 0x3F);
                wtext += wc;
                i += 2;
            } else {
                i++;
            }
        } else if ((c & 0xF0) == 0xE0) {
            // Three-byte character (most Chinese characters)
            if (i + 2 < text.length()) {
                wchar_t wc = ((c & 0x0F) << 12) | ((text[i+1] & 0x3F) << 6) | (text[i+2] & 0x3F);
                wtext += wc;
                i += 3;
            } else {
                i++;
            }
        } else if ((c & 0xF8) == 0xF0) {
            // Four-byte character
            if (i + 3 < text.length()) {
                i += 4; // Skip for now, not common in Chinese
            } else {
                i++;
            }
        } else {
            i++;
        }
    }

    // Render each character
    for (wchar_t wc : wtext) {
        Character ch;
        bool found = false;
        
        // Check if it's an ASCII character
        if (wc < 128 && characters.find(static_cast<char>(wc)) != characters.end()) {
            ch = characters[static_cast<char>(wc)];
            found = true;
        }
        // Check if it's a Chinese character
        else if (chineseCharacters.find(wc) != chineseCharacters.end()) {
            ch = chineseCharacters[wc];
            found = true;
        }
        
        if (!found) {
            // Character not loaded, skip it
            continue;
        }

        float xpos = x + ch.bearing.x * scale;
        float ypos = y - (ch.size.y - ch.bearing.y) * scale;

        float w = ch.size.x * scale;
        float h = ch.size.y * scale;
        
        float vertices[6][4] = {
            { xpos,     ypos + h,   0.0f, 0.0f },            
            { xpos,     ypos,       0.0f, 1.0f },
            { xpos + w, ypos,       1.0f, 1.0f },

            { xpos,     ypos + h,   0.0f, 0.0f },
            { xpos + w, ypos,       1.0f, 1.0f },
            { xpos + w, ypos + h,   1.0f, 0.0f }           
        };
        
        glBindTexture(GL_TEXTURE_2D, ch.textureID);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(vertices), vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        x += (ch.advance >> 6) * scale;
    }
    
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
}

MenuButton MenuRenderer::checkButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    // Convert mouse coordinates (top-left origin) to OpenGL coordinates (bottom-left origin)
    float glMouseY = screenHeight - mouseY;
    
    // Check Start Game button
    if (mouseX >= startButton.x && mouseX <= startButton.x + startButton.width &&
        glMouseY >= startButton.y && glMouseY <= startButton.y + startButton.height) {
        return MenuButton::START_GAME;
    }
    
    // Check Settings button
    if (mouseX >= settingsButton.x && mouseX <= settingsButton.x + settingsButton.width &&
        glMouseY >= settingsButton.y && glMouseY <= settingsButton.y + settingsButton.height) {
        return MenuButton::SETTINGS;
    }
    
    // Check Exit Game button
    if (mouseX >= exitButton.x && mouseX <= exitButton.x + exitButton.width &&
        glMouseY >= exitButton.y && glMouseY <= exitButton.y + exitButton.height) {
        return MenuButton::EXIT_GAME;
    }
    
    return MenuButton::NONE;
}

bool MenuRenderer::checkLanguageButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    float glMouseY = screenHeight - mouseY;
    
    // Check English button
    if (mouseX >= englishButton.x && mouseX <= englishButton.x + englishButton.width &&
        glMouseY >= englishButton.y && glMouseY <= englishButton.y + englishButton.height) {
        currentLanguage = Language::ENGLISH;
        return true;
    }
    
    // Check Chinese button
    if (mouseX >= chineseButton.x && mouseX <= chineseButton.x + chineseButton.width &&
        glMouseY >= chineseButton.y && glMouseY <= chineseButton.y + chineseButton.height) {
        currentLanguage = Language::CHINESE;
        return true;
    }
    
    return false;
}

bool MenuRenderer::checkBackButtonClick(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    float glMouseY = screenHeight - mouseY;
    
    if (mouseX >= backButton.x && mouseX <= backButton.x + backButton.width &&
        glMouseY >= backButton.y && glMouseY <= backButton.y + backButton.height) {
        return true;
    }
    
    return false;
}

MenuButton MenuRenderer::getHoveredButton(double mouseX, double mouseY, int screenWidth, int screenHeight) {
    return checkButtonClick(mouseX, mouseY, screenWidth, screenHeight);
}

GLuint MenuRenderer::loadTextShader() {
    std::string vertexCode;
    std::string fragmentCode;
    std::ifstream vShaderFile("shaders/text_vertex.glsl");
    std::ifstream fShaderFile("shaders/text_fragment.glsl");
    
    if (!vShaderFile.is_open() || !fShaderFile.is_open()) {
        std::cerr << "Failed to open text shader files" << std::endl;
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
    
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertex, 1, &vShaderCode, NULL);
    glCompileShader(vertex);
    
    GLint success;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(vertex, 512, NULL, infoLog);
        std::cerr << "Menu text vertex shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragment, 1, &fShaderCode, NULL);
    glCompileShader(fragment);
    
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(fragment, 512, NULL, infoLog);
        std::cerr << "Menu text fragment shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Menu text shader program linking failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    
    return program;
}

GLuint MenuRenderer::loadButtonShader() {
    std::string vertexCode;
    std::string fragmentCode;
    std::ifstream vShaderFile("shaders/panel_vertex.glsl");
    std::ifstream fShaderFile("shaders/panel_fragment.glsl");
    
    if (!vShaderFile.is_open() || !fShaderFile.is_open()) {
        std::cerr << "Failed to open button shader files" << std::endl;
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
    
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertex, 1, &vShaderCode, NULL);
    glCompileShader(vertex);
    
    GLint success;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(vertex, 512, NULL, infoLog);
        std::cerr << "Button vertex shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragment, 1, &fShaderCode, NULL);
    glCompileShader(fragment);
    
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(fragment, 512, NULL, infoLog);
        std::cerr << "Button fragment shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Button shader program linking failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    
    return program;
}

void MenuRenderer::selectNext() {
    selectedMenuIndex = (selectedMenuIndex + 1) % 3; // 3 menu items
}

void MenuRenderer::selectPrevious() {
    selectedMenuIndex = (selectedMenuIndex - 1 + 3) % 3; // Wrap around
}

MenuButton MenuRenderer::getSelectedButton() const {
    switch(selectedMenuIndex) {
        case 0: return MenuButton::START_GAME;
        case 1: return MenuButton::SETTINGS;
        case 2: return MenuButton::EXIT_GAME;
        default: return MenuButton::NONE;
    }
}

void MenuRenderer::activateSelected() {
    // This will be called from main.cpp when Enter is pressed
}

void MenuRenderer::renderTriangle(float x, float y, float size, const glm::vec3& color, int screenWidth, int screenHeight) {
    glUseProgram(buttonShader);
    
    // Create triangle vertices (pointing right)
    float halfSize = size / 2.0f;
    float vertices[] = {
        x, y + halfSize,           // Top vertex
        x, y - halfSize,           // Bottom vertex
        x + size, y,               // Right vertex (point)
        x, y + halfSize,
        x + size, y,
        x, y - halfSize
    };
    
    // Create orthographic projection
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(screenWidth), 
                                      0.0f, static_cast<float>(screenHeight));
    
    glUniformMatrix4fv(glGetUniformLocation(buttonShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    // Set color with full alpha
    glm::vec4 triangleColor(color.r, color.g, color.b, 1.0f);
    glUniform4fv(glGetUniformLocation(buttonShader, "panelColor"), 1, glm::value_ptr(triangleColor));
    
    // Render as a filled triangle using the button shader
    // We'll need to create a temporary VAO for the triangle
    GLuint triangleVAO, triangleVBO;
    glGenVertexArrays(1, &triangleVAO);
    glGenBuffers(1, &triangleVBO);
    
    glBindVertexArray(triangleVAO);
    glBindBuffer(GL_ARRAY_BUFFER, triangleVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_DYNAMIC_DRAW);
    
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    
    glUniform2f(glGetUniformLocation(buttonShader, "position"), 0.0f, 0.0f);
    glUniform2f(glGetUniformLocation(buttonShader, "size"), 1.0f, 1.0f);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
    
    glBindVertexArray(0);
    glDeleteVertexArrays(1, &triangleVAO);
    glDeleteBuffers(1, &triangleVBO);
}

void MenuRenderer::selectNextSetting() {
    selectedSettingIndex = (selectedSettingIndex + 1) % 2; // 2 language options
}

void MenuRenderer::selectPreviousSetting() {
    selectedSettingIndex = (selectedSettingIndex - 1 + 2) % 2; // Wrap around
}

void MenuRenderer::activateSelectedSetting() {
    // This will be called from main.cpp when Enter is pressed in settings
}
