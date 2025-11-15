#include "UIRenderer.h"
#include "MenuRenderer.h"
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <fstream>
#include <sstream>
#include <iostream>

// FreeType
#include <ft2build.h>
#include FT_FREETYPE_H

UIRenderer::UIRenderer() 
    : VAO(0), VBO(0), panelVAO(0), panelVBO(0),
      textShader(0), panelShader(0),
      fadeAlpha(0.0f), fadingIn(false), currentLanguage(Language::ENGLISH) {
}

UIRenderer::~UIRenderer() {
    if (VAO) glDeleteVertexArrays(1, &VAO);
    if (VBO) glDeleteBuffers(1, &VBO);
    if (panelVAO) glDeleteVertexArrays(1, &panelVAO);
    if (panelVBO) glDeleteBuffers(1, &panelVBO);
    if (textShader) glDeleteProgram(textShader);
    if (panelShader) glDeleteProgram(panelShader);
}

bool UIRenderer::initialize(int screenWidth, int screenHeight) {
    // Load planet data from JSON file
    loadPlanetData();
    
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
    
    // Set font size
    FT_Set_Pixel_Sizes(face, 0, 24);
    
    // Disable byte-alignment restriction
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    
    // Load ASCII characters (0-128)
    for (unsigned char c = 0; c < 128; c++) {
        // Load character glyph
        if (FT_Load_Char(face, c, FT_LOAD_RENDER)) {
            std::cerr << "ERROR::FREETYPE: Failed to load Glyph " << c << std::endl;
            continue;
        }
        
        // Generate texture
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
        
        // Set texture options
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        // Store character
        Character character = {
            texture,
            glm::ivec2(face->glyph->bitmap.width, face->glyph->bitmap.rows),
            glm::ivec2(face->glyph->bitmap_left, face->glyph->bitmap_top),
            static_cast<unsigned int>(face->glyph->advance.x)
        };
        characters.insert(std::pair<char, Character>(c, character));
    }
    
    // Load common Chinese characters for planet info
    std::wstring chineseChars = L"太阳系漫游者水金地火木土天王海星选择语言设置开始游戏退出英文中距离行第近直径千米已知唯生命质量表面温度占有大气颗卫红色风暴环美丽冰巨个大小是最本上";
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
    
    // Clean up FreeType
    FT_Done_Face(face);
    FT_Done_FreeType(ft);
    
    // Initialize shaders for UI rendering
    textShader = loadTextShader();
    panelShader = loadPanelShader();
    if (!textShader || !panelShader) {
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
    
    // Setup VAO/VBO for panel rendering
    float panelVertices[] = {
        0.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
        
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };
    
    glGenVertexArrays(1, &panelVAO);
    glGenBuffers(1, &panelVBO);
    glBindVertexArray(panelVAO);
    glBindBuffer(GL_ARRAY_BUFFER, panelVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(panelVertices), panelVertices, GL_STATIC_DRAW);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), (void*)0);
    glBindVertexArray(0);
    
    return true;
}

void UIRenderer::renderInfoCard(const std::string& planetName, int screenWidth, int screenHeight) {
    if (currentPlanet.empty() || fadeAlpha < 0.01f) {
        return;
    }
    
    // Disable depth testing for 2D UI rendering
    glDisable(GL_DEPTH_TEST);
    
    // Enable blending for transparency
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
    // Render semi-transparent panel
    float panelWidth = 500.0f;
    float panelHeight = 300.0f;
    float panelX = 50.0f;
    float panelY = screenHeight - panelHeight - 50.0f;
    
    renderPanel(panelX, panelY, panelWidth, panelHeight, fadeAlpha, screenWidth, screenHeight);
    
    // Render text on the panel
    if (planetData.find(currentPlanet) != planetData.end()) {
        const PlanetInfo& info = planetData[currentPlanet];
        
        float textX = panelX + 20.0f;
        float textY = panelY + panelHeight - 40.0f;
        float lineHeight = 30.0f;
        
        // Select language
        const std::string& displayName = (currentLanguage == Language::CHINESE) ? info.nameChinese : info.name;
        const std::vector<std::string>& displayFacts = (currentLanguage == Language::CHINESE) ? info.factsChinese : info.facts;
        
        // Render planet name (title)
        glm::vec3 titleColor(1.0f, 1.0f, 0.5f); // Yellow
        renderText(displayName, textX, textY, 1.2f, titleColor, screenWidth, screenHeight);
        textY -= lineHeight * 1.5f;
        
        // Render facts
        glm::vec3 textColor(0.9f, 0.9f, 0.9f); // White
        for (size_t i = 0; i < displayFacts.size() && i < 6; ++i) {
            std::string bullet = "  " + displayFacts[i];
            renderText(bullet, textX, textY, 0.7f, textColor, screenWidth, screenHeight);
            textY -= lineHeight;
        }
    }
    
    glDisable(GL_BLEND);
    
    // Re-enable depth testing for 3D rendering
    glEnable(GL_DEPTH_TEST);
}

void UIRenderer::setCurrentPlanet(const std::string& planetName) {
    if (planetName != currentPlanet) {
        currentPlanet = planetName;
        if (!planetName.empty()) {
            fadingIn = true;
        } else {
            fadingIn = false;
        }
    }
}

void UIRenderer::updateFade(float deltaTime) {
    float fadeSpeed = 2.0f; // Slower fade for smoother transitions
    
    if (fadingIn && fadeAlpha < 1.0f) {
        fadeAlpha += fadeSpeed * deltaTime;
        if (fadeAlpha > 1.0f) fadeAlpha = 1.0f;
    } else if (!fadingIn && fadeAlpha > 0.0f) {
        fadeAlpha -= fadeSpeed * deltaTime;
        if (fadeAlpha < 0.0f) fadeAlpha = 0.0f;
    }
    
    // Only clear planet name when fully faded out
    if (fadeAlpha <= 0.0f && !fadingIn) {
        currentPlanet = "";
    }
}

void UIRenderer::setLanguage(Language lang) {
    currentLanguage = lang;
}

void UIRenderer::loadPlanetData() {
    // Hardcoded planet data for simplicity
    // In a full implementation, we would parse the JSON file
    
    PlanetInfo sunInfo;
    sunInfo.name = "Sun";
    sunInfo.nameChinese = "太阳";
    sunInfo.facts.push_back("Star at the center of our solar system");
    sunInfo.facts.push_back("Surface temperature: 5,500 C");
    sunInfo.facts.push_back("Contains 99.86% of solar system mass");
    sunInfo.factsChinese.push_back("太阳系中心的恒星");
    sunInfo.factsChinese.push_back("表面温度：5,500°C");
    sunInfo.factsChinese.push_back("占太阳系质量的99.86%");
    planetData["Sun"] = sunInfo;
    
    PlanetInfo mercuryInfo;
    mercuryInfo.name = "Mercury";
    mercuryInfo.nameChinese = "水星";
    mercuryInfo.facts.push_back("Closest planet to the Sun");
    mercuryInfo.facts.push_back("Diameter: 4,879 km (0.38x Earth)");
    mercuryInfo.facts.push_back("Orbital period: 88 Earth days");
    mercuryInfo.factsChinese.push_back("距离太阳最近的行星");
    mercuryInfo.factsChinese.push_back("直径：4,879千米（地球的0.38倍）");
    mercuryInfo.factsChinese.push_back("公转周期：88个地球日");
    planetData["Mercury"] = mercuryInfo;
    
    PlanetInfo venusInfo;
    venusInfo.name = "Venus";
    venusInfo.nameChinese = "金星";
    venusInfo.facts.push_back("Second planet from the Sun");
    venusInfo.facts.push_back("Diameter: 12,104 km (0.95x Earth)");
    venusInfo.facts.push_back("Hottest planet (470C)");
    venusInfo.factsChinese.push_back("距离太阳第二近的行星");
    venusInfo.factsChinese.push_back("直径：12,104千米（地球的0.95倍）");
    venusInfo.factsChinese.push_back("最热的行星（470°C）");
    planetData["Venus"] = venusInfo;
    
    PlanetInfo earthInfo;
    earthInfo.name = "Earth";
    earthInfo.nameChinese = "地球";
    earthInfo.facts.push_back("Third planet from the Sun");
    earthInfo.facts.push_back("Diameter: 12,742 km");
    earthInfo.facts.push_back("Only known planet with life");
    earthInfo.factsChinese.push_back("距离太阳第三近的行星");
    earthInfo.factsChinese.push_back("直径：12,742千米");
    earthInfo.factsChinese.push_back("已知唯一有生命的行星");
    planetData["Earth"] = earthInfo;
    
    PlanetInfo marsInfo;
    marsInfo.name = "Mars";
    marsInfo.nameChinese = "火星";
    marsInfo.facts.push_back("Fourth planet, the Red Planet");
    marsInfo.facts.push_back("Diameter: 6,779 km (0.53x Earth)");
    marsInfo.facts.push_back("Home to largest volcano in solar system");
    marsInfo.factsChinese.push_back("第四颗行星，红色星球");
    marsInfo.factsChinese.push_back("直径：6,779千米（地球的0.53倍）");
    marsInfo.factsChinese.push_back("拥有太阳系最大的火山");
    planetData["Mars"] = marsInfo;
    
    PlanetInfo jupiterInfo;
    jupiterInfo.name = "Jupiter";
    jupiterInfo.nameChinese = "木星";
    jupiterInfo.facts.push_back("Fifth planet, largest in solar system");
    jupiterInfo.facts.push_back("Diameter: 139,820 km (11x Earth)");
    jupiterInfo.facts.push_back("Famous Great Red Spot storm");
    jupiterInfo.factsChinese.push_back("第五颗行星，太阳系最大");
    jupiterInfo.factsChinese.push_back("直径：139,820千米（地球的11倍）");
    jupiterInfo.factsChinese.push_back("著名的大红斑风暴");
    planetData["Jupiter"] = jupiterInfo;
    
    PlanetInfo saturnInfo;
    saturnInfo.name = "Saturn";
    saturnInfo.nameChinese = "土星";
    saturnInfo.facts.push_back("Sixth planet with spectacular rings");
    saturnInfo.facts.push_back("Diameter: 116,460 km (9x Earth)");
    saturnInfo.facts.push_back("Rings made of ice and rock");
    saturnInfo.factsChinese.push_back("第六颗行星，拥有壮观的光环");
    saturnInfo.factsChinese.push_back("直径：116,460千米（地球的9倍）");
    saturnInfo.factsChinese.push_back("光环由冰和岩石组成");
    planetData["Saturn"] = saturnInfo;
    
    PlanetInfo uranusInfo;
    uranusInfo.name = "Uranus";
    uranusInfo.nameChinese = "天王星";
    uranusInfo.facts.push_back("Seventh planet, ice giant");
    uranusInfo.facts.push_back("Diameter: 50,724 km (4x Earth)");
    uranusInfo.facts.push_back("Rotates on its side");
    uranusInfo.factsChinese.push_back("第七颗行星，冰巨星");
    uranusInfo.factsChinese.push_back("直径：50,724千米（地球的4倍）");
    uranusInfo.factsChinese.push_back("横躺着自转");
    planetData["Uranus"] = uranusInfo;
    
    PlanetInfo neptuneInfo;
    neptuneInfo.name = "Neptune";
    neptuneInfo.nameChinese = "海王星";
    neptuneInfo.facts.push_back("Eighth planet, farthest from Sun");
    neptuneInfo.facts.push_back("Diameter: 49,244 km (3.9x Earth)");
    neptuneInfo.facts.push_back("Strongest winds in solar system");
    neptuneInfo.factsChinese.push_back("第八颗行星，距太阳最远");
    neptuneInfo.factsChinese.push_back("直径：49,244千米（地球的3.9倍）");
    neptuneInfo.factsChinese.push_back("太阳系中风速最强");
    planetData["Neptune"] = neptuneInfo;
}

void UIRenderer::renderText(const std::string& text, float x, float y, float scale, const glm::vec3& color, int screenWidth, int screenHeight) {
    // Activate corresponding render state
    glUseProgram(textShader);
    
    // Update projection matrix for current screen size
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(screenWidth), 0.0f, static_cast<float>(screenHeight));
    glUniformMatrix4fv(glGetUniformLocation(textShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    
    // Apply fade alpha to color
    glm::vec3 fadedColor = color * fadeAlpha;
    glUniform3f(glGetUniformLocation(textShader, "textColor"), fadedColor.x, fadedColor.y, fadedColor.z);
    
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
                i += 4; // Skip for now
            } else {
                i++;
            }
        } else {
            i++;
        }
    }

    // Iterate through all characters
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
        
        // Update VBO for each character
        float vertices[6][4] = {
            { xpos,     ypos + h,   0.0f, 0.0f },            
            { xpos,     ypos,       0.0f, 1.0f },
            { xpos + w, ypos,       1.0f, 1.0f },

            { xpos,     ypos + h,   0.0f, 0.0f },
            { xpos + w, ypos,       1.0f, 1.0f },
            { xpos + w, ypos + h,   1.0f, 0.0f }           
        };
        
        // Render glyph texture over quad
        glBindTexture(GL_TEXTURE_2D, ch.textureID);
        
        // Update content of VBO memory
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(vertices), vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        // Render quad
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Advance cursor for next glyph
        x += (ch.advance >> 6) * scale; // Bitshift by 6 to get value in pixels (2^6 = 64)
    }
    
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void UIRenderer::renderPanel(float x, float y, float width, float height, float alpha, int screenWidth, int screenHeight) {
    glUseProgram(panelShader);
    
    // Create orthographic projection with actual screen size
    glm::mat4 projection = glm::ortho(0.0f, static_cast<float>(screenWidth), 0.0f, static_cast<float>(screenHeight));
    
    glUniformMatrix4fv(glGetUniformLocation(panelShader, "projection"), 1, GL_FALSE, glm::value_ptr(projection));
    glUniform2f(glGetUniformLocation(panelShader, "position"), x, y);
    glUniform2f(glGetUniformLocation(panelShader, "size"), width, height);
    
    // Dark semi-transparent panel
    glm::vec4 panelColor = glm::vec4(0.1f, 0.1f, 0.15f, 0.8f * alpha);
    glUniform4fv(glGetUniformLocation(panelShader, "panelColor"), 1, glm::value_ptr(panelColor));
    
    glBindVertexArray(panelVAO);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
}

GLuint UIRenderer::loadTextShader() {
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
    
    // Compile vertex shader
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertex, 1, &vShaderCode, NULL);
    glCompileShader(vertex);
    
    GLint success;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(vertex, 512, NULL, infoLog);
        std::cerr << "Text vertex shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    // Compile fragment shader
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragment, 1, &fShaderCode, NULL);
    glCompileShader(fragment);
    
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(fragment, 512, NULL, infoLog);
        std::cerr << "Text fragment shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    // Link shaders
    GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Text shader program linking failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    
    return program;
}

GLuint UIRenderer::loadPanelShader() {
    std::string vertexCode;
    std::string fragmentCode;
    std::ifstream vShaderFile("shaders/panel_vertex.glsl");
    std::ifstream fShaderFile("shaders/panel_fragment.glsl");
    
    if (!vShaderFile.is_open() || !fShaderFile.is_open()) {
        std::cerr << "Failed to open panel shader files" << std::endl;
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
    
    // Compile vertex shader
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertex, 1, &vShaderCode, NULL);
    glCompileShader(vertex);
    
    GLint success;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(vertex, 512, NULL, infoLog);
        std::cerr << "Panel vertex shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    // Compile fragment shader
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragment, 1, &fShaderCode, NULL);
    glCompileShader(fragment);
    
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetShaderInfoLog(fragment, 512, NULL, infoLog);
        std::cerr << "Panel fragment shader compilation failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    // Link shaders
    GLuint program = glCreateProgram();
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);
    glLinkProgram(program);
    
    glGetProgramiv(program, GL_LINK_STATUS, &success);
    if (!success) {
        char infoLog[512];
        glGetProgramInfoLog(program, 512, NULL, infoLog);
        std::cerr << "Panel shader program linking failed:\n" << infoLog << std::endl;
        return 0;
    }
    
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    
    return program;
}
