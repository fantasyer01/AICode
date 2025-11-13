#include "Game.h"
#include <glad/glad.h>
#include <iostream>

Game* Game::instance = nullptr;

Game::Game()
    : window(nullptr),
      windowWidth(1920),
      windowHeight(1080),
      state(MENU),
      maze(nullptr),
      player(nullptr),
      renderer(nullptr),
      textRenderer(nullptr),
      collision(nullptr),
      lastX(960.0f),
      lastY(540.0f),
      firstMouse(true),
      selectedMenuItem(0) {
    instance = this;
}

Game::~Game() {
    cleanup();
}

bool Game::init() {
    // Initialize GLFW
    if (!glfwInit()) {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return false;
    }
    
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);  // Disable window resizing
    glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);  // Disable window maximization
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);    // Start hidden to avoid white flash
    
    // Get primary monitor and its video mode for 2/3 screen size
    GLFWmonitor* monitor = glfwGetPrimaryMonitor();
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    
    // Set window size to 2/3 of screen dimensions
    windowWidth = (mode->width * 2) / 3;
    windowHeight = (mode->height * 2) / 3;
    
    // Update mouse position defaults
    lastX = windowWidth / 2.0f;
    lastY = windowHeight / 2.0f;
    
    window = glfwCreateWindow(windowWidth, windowHeight, "3D Maze Explorer", NULL, NULL);
    if (!window) {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return false;
    }
    
    // Center window on screen (1/6 from edges for 2/3 size window)
    glfwSetWindowPos(window, mode->width / 6, mode->height / 6);
    
    glfwMakeContextCurrent(window);
    glfwSetCursorPosCallback(window, mouseCallback);
    glfwSetKeyCallback(window, keyCallback);
    
    // Load OpenGL functions
    if (!gladLoadGLLoader((GLADloadproc)glfwGetProcAddress)) {
        std::cerr << "Failed to initialize GLAD" << std::endl;
        return false;
    }
    
    glEnable(GL_DEPTH_TEST);
    
    // Set initial background color to menu color to avoid white flash
    glClearColor(0.15f, 0.20f, 0.30f, 1.0f);
    
    // Clear the screen immediately to avoid white flash
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glfwSwapBuffers(window);
    
    // Set viewport to full window size
    glViewport(0, 0, windowWidth, windowHeight);
    
    // Initialize renderer
    renderer = new Renderer();
    if (!renderer->init("shaders/vertex.glsl", "shaders/fragment.glsl")) {
        std::cerr << "Failed to initialize renderer" << std::endl;
        return false;
    }
    
    // Initialize text renderer
    textRenderer = new TextRenderer();
    if (!textRenderer->init(renderer->getShaderProgram())) {
        std::cerr << "Failed to initialize text renderer" << std::endl;
        return false;
    }
    
    // Render first frame with menu before showing window
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    showMenu();
    glfwSwapBuffers(window);
    
    // Now show the window with the menu already rendered
    glfwShowWindow(window);
    
    return true;
}

void Game::run() {
    float lastFrame = 0.0f;
    
    while (!glfwWindowShouldClose(window)) {
        float currentFrame = glfwGetTime();
        float deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;
        
        processInput(deltaTime);
        update(deltaTime);
        render();
        
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
}

void Game::processInput(float deltaTime) {
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        if (state == PLAYING) {
            state = MENU;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else if (state == MENU) {
            glfwSetWindowShouldClose(window, true);
        }
    }
    
    if (state == MENU) {
        // Handle menu navigation with arrow keys
        static bool upPressed = false;
        static bool downPressed = false;
        static bool enterPressed = false;
        
        bool upKey = glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS;
        bool downKey = glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS;
        bool enterKey = glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS;
        
        if (upKey && !upPressed) {
            selectedMenuItem = (selectedMenuItem - 1 + 2) % 2;  // 2 menu items
        }
        if (downKey && !downPressed) {
            selectedMenuItem = (selectedMenuItem + 1) % 2;
        }
        if (enterKey && !enterPressed) {
            if (selectedMenuItem == 0) {
                startGame();
            } else {
                glfwSetWindowShouldClose(window, true);
            }
        }
        
        upPressed = upKey;
        downPressed = downKey;
        enterPressed = enterKey;
    }
    
    if (state == PLAYING && player) {
        glm::vec3 oldPos = player->getPosition();
        
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            player->processKeyboard(GLFW_KEY_W, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            player->processKeyboard(GLFW_KEY_S, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            player->processKeyboard(GLFW_KEY_A, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            player->processKeyboard(GLFW_KEY_D, deltaTime);
        
        // Check collision
        glm::vec3 newPos = collision->checkCollision(oldPos, player->getPosition(), 0.4f);
        player->setPosition(newPos);
    }
    
    if (state == VICTORY) {
        if (glfwGetKey(window, GLFW_KEY_N) == GLFW_PRESS) {
            startGame();
        }
        if (glfwGetKey(window, GLFW_KEY_M) == GLFW_PRESS) {
            state = MENU;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }
}

void Game::update(float deltaTime) {
    if (state == PLAYING) {
        if (checkVictory()) {
            state = VICTORY;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }
}

void Game::render() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
    if (state == MENU) {
        showMenu();
    } else if (state == PLAYING) {
        if (maze && player && renderer) {
            renderer->renderMaze(*maze, *player, windowWidth, windowHeight);
            renderer->renderMinimap(*maze, *player, windowWidth, windowHeight);
        }
    } else if (state == VICTORY) {
        showVictory();
    }
}

void Game::startGame() {
    // Clean up previous game
    if (maze) delete maze;
    if (player) delete player;
    if (collision) delete collision;
    
    // Create new maze
    maze = new Maze(15, 15, 2.0f);
    maze->generate();
    
    // Create player at start position
    player = new Player(maze->getStartPosition());
    
    // Create collision system
    collision = new CollisionSystem(maze);
    
    // Set game state
    state = PLAYING;
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    firstMouse = true;
}

bool Game::checkVictory() {
    if (!player || !maze) return false;
    
    glm::vec3 playerPos = player->getPosition();
    glm::vec3 exitPos = maze->getExitPosition();
    
    float distance = glm::length(playerPos - exitPos);
    return distance < 1.0f;
}

void Game::showMenu() {
    // Set background color for menu
    glClearColor(0.15f, 0.20f, 0.30f, 1.0f);
    
    glDisable(GL_DEPTH_TEST);
    
    if (renderer) {
        renderer->renderMenu(selectedMenuItem, windowWidth, windowHeight, textRenderer);
    }
    
    glEnable(GL_DEPTH_TEST);
}

void Game::showVictory() {
    // Clear to victory color
    glClearColor(0.1f, 0.3f, 0.1f, 1.0f);
    
    // In a full implementation, render "Victory!" message
    // and "Press N for New Game, M for Menu" instructions
}

void Game::mouseCallback(GLFWwindow* window, double xpos, double ypos) {
    if (!instance || instance->state != PLAYING || !instance->player)
        return;
    
    if (instance->firstMouse) {
        instance->lastX = xpos;
        instance->lastY = ypos;
        instance->firstMouse = false;
    }
    
    float xoffset = xpos - instance->lastX;
    float yoffset = instance->lastY - ypos;
    
    instance->lastX = xpos;
    instance->lastY = ypos;
    
    instance->player->processMouse(xoffset, yoffset);
}

void Game::keyCallback(GLFWwindow* window, int key, int scancode, int action, int mods) {
    // Additional key handling can be added here
}

void Game::cleanup() {
    if (maze) delete maze;
    if (player) delete player;
    if (renderer) delete renderer;
    if (textRenderer) delete textRenderer;
    if (collision) delete collision;
    
    if (window) {
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
