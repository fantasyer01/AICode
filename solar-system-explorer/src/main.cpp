#include <glad/glad.h>
#include <GLFW/glfw3.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <iostream>
#include <fstream>

#include "SolarSystem.h"
#include "Camera.h"
#include "Renderer.h"
#include "ProximityDetector.h"
#include "UIRenderer.h"
#include "MenuRenderer.h"

// Game states
enum class GameState {
    MENU,
    SETTINGS,
    PLAYING
};

// Window dimensions
const int SCREEN_WIDTH = 1920;
const int SCREEN_HEIGHT = 1080;

// Camera
Camera camera(glm::vec3(0.0f, 10.0f, 30.0f));

// Mouse state
bool firstMouse = true;
float lastX = SCREEN_WIDTH / 2.0f;
float lastY = SCREEN_HEIGHT / 2.0f;

// Timing
float deltaTime = 0.0f;
float lastFrame = 0.0f;

// Input state
bool keys[1024] = {false};

// Info display state
std::string lastDisplayedPlanet = "";

// Game state
GameState gameState = GameState::MENU;

// Global UIRenderer pointer for language sync
UIRenderer* g_uiRenderer = nullptr;

// Function declarations
void framebuffer_size_callback(GLFWwindow* window, int width, int height);
void mouse_callback(GLFWwindow* window, double xpos, double ypos);
void mouse_button_callback(GLFWwindow* window, int button, int action, int mods);
void key_callback(GLFWwindow* window, int key, int scancode, int action, int mods);
void processInput(GLFWwindow* window);

int main() {
    // Write to log file for debugging
    std::ofstream logFile("debug.log");
    logFile << "========================================" << std::endl;
    logFile << "Solar System Explorer Starting..." << std::endl;
    logFile << "========================================" << std::endl;
    logFile.flush();
    
    std::cout << "========================================" << std::endl;
    std::cout << "Solar System Explorer Starting..." << std::endl;
    std::cout << "========================================" << std::endl;
    
    // Initialize GLFW
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        logFile << "Failed to initialize GLFW" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        return -1;
    }
    std::cout << "GLFW initialized successfully" << std::endl;
    logFile << "GLFW initialized successfully" << std::endl;
    logFile.flush();

    // Configure GLFW
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);  // Start hidden to prevent white flash

    // Get primary monitor and video mode
    GLFWmonitor* monitor = glfwGetPrimaryMonitor();
    if (!monitor) {
        std::cerr << "Failed to get primary monitor" << std::endl;
        logFile << "Failed to get primary monitor" << std::endl;
        logFile.close();
        glfwTerminate();
        return -1;
    }
    logFile << "Got primary monitor" << std::endl;
    logFile.flush();
    
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    if (!mode) {
        std::cerr << "Failed to get video mode" << std::endl;
        logFile << "Failed to get video mode" << std::endl;
        logFile.close();
        glfwTerminate();
        return -1;
    }
    logFile << "Got video mode: " << mode->width << "x" << mode->height << std::endl;
    logFile.flush();
    
    int windowWidth = mode->width / 2;
    int windowHeight = mode->height / 2;
    
    logFile << "Window size: " << windowWidth << "x" << windowHeight << std::endl;
    logFile.flush();

    // Create window
    logFile << "Creating window..." << std::endl;
    logFile.flush();
    
    GLFWwindow* window = glfwCreateWindow(windowWidth, windowHeight, 
                                          "Solar System Explorer", NULL, NULL);
    if (!window) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        logFile << "Failed to create GLFW window" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        glfwTerminate();
        return -1;
    }
    std::cout << "Window created successfully (" << windowWidth << "x" << windowHeight << ")" << std::endl;
    logFile << "Window created successfully" << std::endl;
    logFile.flush();
    
    logFile << "Setting up callbacks..." << std::endl;
    logFile.flush();
    
    glfwMakeContextCurrent(window);
    
    // Load OpenGL function pointers immediately after making context current
    logFile << "Loading GLAD..." << std::endl;
    logFile.flush();

    if (!gladLoadGLLoader((GLADloadproc)glfwGetProcAddress)) {
        std::cerr << "Failed to initialize GLAD" << std::endl;
        logFile << "Failed to initialize GLAD" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        glfwTerminate();
        return -1;
    }
    std::cout << "GLAD initialized successfully" << std::endl;
    logFile << "GLAD initialized successfully" << std::endl;
    logFile.flush();
    
    // Now set up callbacks
    logFile << "Setting up callbacks..." << std::endl;
    logFile.flush();
    
    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);
    glfwSetCursorPosCallback(window, mouse_callback);
    glfwSetMouseButtonCallback(window, mouse_button_callback);
    glfwSetKeyCallback(window, key_callback);

    // Start with cursor visible for menu
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    // Configure OpenGL
    logFile << "Configuring OpenGL..." << std::endl;
    logFile.flush();
    
    glEnable(GL_DEPTH_TEST);
    glViewport(0, 0, windowWidth, windowHeight);
    
    logFile << "OpenGL configured" << std::endl;
    logFile.flush();

    // Initialize game components
    std::cout << "Initializing Menu..." << std::endl;
    logFile << "Initializing Menu..." << std::endl;
    logFile.flush();
    
    MenuRenderer menuRenderer;
    if (!menuRenderer.initialize(windowWidth, windowHeight)) {
        std::cerr << "Failed to initialize menu renderer" << std::endl;
        logFile << "Failed to initialize menu renderer" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        glfwTerminate();
        return -1;
    }
    std::cout << "Menu initialized successfully" << std::endl;
    logFile << "Menu initialized successfully" << std::endl;
    logFile.flush();
    
    // Set window user pointer to menu renderer for button click handling
    glfwSetWindowUserPointer(window, &menuRenderer);
    
    std::cout << "Initializing Solar System..." << std::endl;
    logFile << "Initializing Solar System..." << std::endl;
    logFile.flush();
    
    SolarSystem solarSystem;
    
    std::cout << "Initializing Renderer..." << std::endl;
    logFile << "Initializing Renderer..." << std::endl;
    logFile.flush();
    
    Renderer renderer;
    ProximityDetector proximityDetector(3.0f);
    UIRenderer uiRenderer;

    if (!renderer.initialize()) {
        std::cerr << "Failed to initialize renderer" << std::endl;
        logFile << "Failed to initialize renderer" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        glfwTerminate();
        return -1;
    }
    std::cout << "Renderer initialized successfully" << std::endl;
    logFile << "Renderer initialized successfully" << std::endl;
    logFile.flush();

    if (!uiRenderer.initialize(windowWidth, windowHeight)) {
        std::cerr << "Failed to initialize UI renderer" << std::endl;
        logFile << "Failed to initialize UI renderer" << std::endl;
        logFile.close();
        std::cerr << "Press Enter to exit..." << std::endl;
        std::cin.get();
        glfwTerminate();
        return -1;
    }
    std::cout << "UI Renderer initialized successfully" << std::endl;
    logFile << "UI Renderer initialized successfully" << std::endl;
    logFile.flush();
    
    // Set global UIRenderer pointer for language sync
    g_uiRenderer = &uiRenderer;

    logFile << "All systems initialized - entering game loop" << std::endl;
    logFile.close();

    std::cout << "Solar System Explorer - Main Menu" << std::endl;
    
    // Render first frame (menu) before showing window to prevent white flash
    glClearColor(0.0f, 0.0f, 0.05f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    menuRenderer.render(windowWidth, windowHeight);
    glfwSwapBuffers(window);
    
    // Position window at center before showing
    glfwSetWindowPos(window, mode->width / 4, mode->height / 4);
    
    // Now show the window with menu already rendered
    glfwShowWindow(window);

    // Game loop
    while (!glfwWindowShouldClose(window)) {
        // Calculate delta time
        float currentFrame = static_cast<float>(glfwGetTime());
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        // Process input
        processInput(window);

        // Render
        glClearColor(0.0f, 0.0f, 0.05f, 1.0f); // Slightly blue background
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (gameState == GameState::MENU) {
            // Render menu
            menuRenderer.render(windowWidth, windowHeight);
        }
        else if (gameState == GameState::SETTINGS) {
            // Render settings page
            menuRenderer.renderSettings(windowWidth, windowHeight);
        }
        else if (gameState == GameState::PLAYING) {
            // Update game state
            solarSystem.update(deltaTime);

            // Check proximity to planets
            auto nearbyPlanet = proximityDetector.checkProximity(camera.getPosition(), 
                                                                 solarSystem.getPlanets());
            
            if (nearbyPlanet) {
                uiRenderer.setCurrentPlanet(nearbyPlanet->getName());
                // Only print planet info when entering proximity for first time
                if (lastDisplayedPlanet != nearbyPlanet->getName()) {
                    lastDisplayedPlanet = nearbyPlanet->getName();
                    std::cout << "\n=== " << nearbyPlanet->getName() << " ==="<< std::endl;
                    std::cout << "Approaching " << nearbyPlanet->getName() << "!" << std::endl;
                    std::cout << "Check the info panel on screen for details." << std::endl;
                }
            } else {
                uiRenderer.setCurrentPlanet("");
                if (!lastDisplayedPlanet.empty()) {
                    lastDisplayedPlanet = "";
                }
            }
            
            uiRenderer.updateFade(deltaTime);

            // Setup matrices
            glm::mat4 view = camera.getViewMatrix();
            glm::mat4 projection = glm::perspective(glm::radians(60.0f), 
                                                   (float)windowWidth / (float)windowHeight, 
                                                   0.1f, 500.0f);

            // Render skybox
            renderer.renderSkybox(view, projection);

            // Render orbital paths
            for (const auto& planet : solarSystem.getPlanets()) {
                renderer.renderOrbit(planet->getOrbitalRadius(), view, projection);
            }

            // Render all planets
            for (const auto& planet : solarSystem.getPlanets()) {
                renderer.renderPlanet(*planet, view, projection);
            }

            // Render UI
            if (nearbyPlanet) {
                uiRenderer.renderInfoCard(nearbyPlanet->getName(), windowWidth, windowHeight);
            }
        }

        // Swap buffers and poll events
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    // Cleanup
    glfwTerminate();
    return 0;
}

void framebuffer_size_callback(GLFWwindow* window, int width, int height) {
    glViewport(0, 0, width, height);
}

void mouse_callback(GLFWwindow* window, double xpos, double ypos) {
    // Only process mouse movement in playing mode
    if (gameState != GameState::PLAYING) {
        return;
    }
    
    float xposf = static_cast<float>(xpos);
    float yposf = static_cast<float>(ypos);

    if (firstMouse) {
        lastX = xposf;
        lastY = yposf;
        firstMouse = false;
    }

    float xoffset = xposf - lastX;
    float yoffset = lastY - yposf; // Reversed since y-coordinates go from bottom to top

    lastX = xposf;
    lastY = yposf;

    camera.processMouseMovement(xoffset, yoffset);
}

void mouse_button_callback(GLFWwindow* window, int button, int action, int mods) {
    if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        // Get cursor position
        double xpos, ypos;
        glfwGetCursorPos(window, &xpos, &ypos);
        
        // Get window size
        int width, height;
        glfwGetFramebufferSize(window, &width, &height);
        
        MenuRenderer* menuRenderer = static_cast<MenuRenderer*>(glfwGetWindowUserPointer(window));
        if (menuRenderer) {
            if (gameState == GameState::MENU) {
                MenuButton clicked = menuRenderer->checkButtonClick(xpos, ypos, width, height);
                
                if (clicked == MenuButton::START_GAME) {
                    gameState = GameState::PLAYING;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    firstMouse = true; // Reset mouse to prevent camera jump
                    std::cout << "\nStarting game..." << std::endl;
                    std::cout << "Controls:" << std::endl;
                    std::cout << "  W/A/S/D - Move spacecraft" << std::endl;
                    std::cout << "  Mouse - Look around" << std::endl;
                    std::cout << "  ESC - Return to menu" << std::endl;
                    std::cout << "\nFly close to planets to learn about them!" << std::endl;
                }
                else if (clicked == MenuButton::SETTINGS) {
                    gameState = GameState::SETTINGS;
                    std::cout << "\nOpening settings..." << std::endl;
                }
                else if (clicked == MenuButton::EXIT_GAME) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
            else if (gameState == GameState::SETTINGS) {
                // Check language button clicks
                if (menuRenderer->checkLanguageButtonClick(xpos, ypos, width, height)) {
                    // Language changed, sync with UIRenderer
                    if (g_uiRenderer) {
                        g_uiRenderer->setLanguage(menuRenderer->getLanguage());
                    }
                    std::cout << "Language changed to: " 
                              << (menuRenderer->getLanguage() == Language::ENGLISH ? "English" : "Chinese") 
                              << std::endl;
                }
            }
        }
    }
}

void key_callback(GLFWwindow* window, int key, int scancode, int action, int mods) {
    if (key >= 0 && key < 1024) {
        if (action == GLFW_PRESS)
            keys[key] = true;
        else if (action == GLFW_RELEASE)
            keys[key] = false;
    }
    
    // Handle menu navigation
    MenuRenderer* menuRenderer = static_cast<MenuRenderer*>(glfwGetWindowUserPointer(window));
    
    if (action == GLFW_PRESS) {
        if (gameState == GameState::MENU) {
            if (key == GLFW_KEY_UP) {
                if (menuRenderer) menuRenderer->selectPrevious();
            }
            else if (key == GLFW_KEY_DOWN) {
                if (menuRenderer) menuRenderer->selectNext();
            }
            else if (key == GLFW_KEY_ENTER || key == GLFW_KEY_KP_ENTER) {
                // Activate selected menu item
                if (menuRenderer) {
                    MenuButton selected = menuRenderer->getSelectedButton();
                    
                    if (selected == MenuButton::START_GAME) {
                        gameState = GameState::PLAYING;
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                        firstMouse = true;
                        std::cout << "\nStarting game..." << std::endl;
                        std::cout << "Controls:" << std::endl;
                        std::cout << "  W/A/S/D - Move spacecraft" << std::endl;
                        std::cout << "  Mouse - Look around" << std::endl;
                        std::cout << "  ESC - Return to menu" << std::endl;
                        std::cout << "\nFly close to planets to learn about them!" << std::endl;
                    }
                    else if (selected == MenuButton::SETTINGS) {
                        gameState = GameState::SETTINGS;
                        std::cout << "\nOpening settings..." << std::endl;
                    }
                    else if (selected == MenuButton::EXIT_GAME) {
                        glfwSetWindowShouldClose(window, true);
                    }
                }
            }
        }
        else if (gameState == GameState::SETTINGS) {
            if (key == GLFW_KEY_UP) {
                if (menuRenderer) menuRenderer->selectPreviousSetting();
            }
            else if (key == GLFW_KEY_DOWN) {
                if (menuRenderer) menuRenderer->selectNextSetting();
            }
            else if (key == GLFW_KEY_ENTER || key == GLFW_KEY_KP_ENTER) {
                // Activate selected setting item
                if (menuRenderer) {
                    int selectedSetting = menuRenderer->getSelectedSettingIndex();
                    
                    if (selectedSetting == 0) {
                        // English selected
                        menuRenderer->setLanguage(Language::ENGLISH);
                        if (g_uiRenderer) {
                            g_uiRenderer->setLanguage(Language::ENGLISH);
                        }
                        std::cout << "Language changed to: English" << std::endl;
                    }
                    else if (selectedSetting == 1) {
                        // Chinese selected
                        menuRenderer->setLanguage(Language::CHINESE);
                        if (g_uiRenderer) {
                            g_uiRenderer->setLanguage(Language::CHINESE);
                        }
                        std::cout << "Language changed to: Chinese" << std::endl;
                    }
                }
            }
        }
    }

    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
        if (gameState == GameState::PLAYING) {
            // Return to menu
            gameState = GameState::MENU;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            std::cout << "\nReturned to main menu" << std::endl;
        }
        else if (gameState == GameState::SETTINGS) {
            // Return to menu from settings
            gameState = GameState::MENU;
            std::cout << "\nReturned to main menu" << std::endl;
        }
        else if (gameState == GameState::MENU) {
            // Exit game
            glfwSetWindowShouldClose(window, true);
        }
    }
}

void processInput(GLFWwindow* window) {
    if (keys[GLFW_KEY_W])
        camera.processKeyboard(Camera::FORWARD, deltaTime);
    if (keys[GLFW_KEY_S])
        camera.processKeyboard(Camera::BACKWARD, deltaTime);
    if (keys[GLFW_KEY_A])
        camera.processKeyboard(Camera::LEFT, deltaTime);
    if (keys[GLFW_KEY_D])
        camera.processKeyboard(Camera::RIGHT, deltaTime);
}
