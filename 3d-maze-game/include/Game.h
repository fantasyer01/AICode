#ifndef GAME_H
#define GAME_H

#include "Renderer.h"  // Must be first - includes GLAD before GLFW
#include <GLFW/glfw3.h>
#include "Maze.h"
#include "Player.h"
#include "Collision.h"
#include "TextRenderer.h"

enum GameState {
    MENU,
    PLAYING,
    VICTORY
};

class Game {
public:
    Game();
    ~Game();
    
    bool init();
    void run();
    void cleanup();
    
    void processInput(float deltaTime);
    void update(float deltaTime);
    void render();
    
    static void mouseCallback(GLFWwindow* window, double xpos, double ypos);
    static void keyCallback(GLFWwindow* window, int key, int scancode, int action, int mods);
    
private:
    GLFWwindow* window;
    int windowWidth;
    int windowHeight;
    
    GameState state;
    Maze* maze;
    Player* player;
    Renderer* renderer;
    TextRenderer* textRenderer;
    CollisionSystem* collision;
    
    float lastX, lastY;
    bool firstMouse;
    int selectedMenuItem;  // 0 = Start Game, 1 = Quit Game
    
    void startGame();
    bool checkVictory();
    void showMenu();
    void showVictory();
    
    static Game* instance;
};

#endif
