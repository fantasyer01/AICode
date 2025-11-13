#include "Game.h"
#include <iostream>

int main() {
    Game game;
    
    if (!game.init()) {
        std::cerr << "Failed to initialize game" << std::endl;
        return -1;
    }
    
    std::cout << "=== 3D Maze Explorer ===" << std::endl;
    std::cout << "Game initialized successfully!" << std::endl;
    std::cout << "" << std::endl;
    std::cout << "Menu Controls:" << std::endl;
    std::cout << "  UP/DOWN - Navigate menu" << std::endl;
    std::cout << "  ENTER   - Select option" << std::endl;
    std::cout << "" << std::endl;
    std::cout << "Game Controls:" << std::endl;
    std::cout << "  W/A/S/D - Move" << std::endl;
    std::cout << "  Mouse   - Look around" << std::endl;
    std::cout << "  ESC     - Return to menu" << std::endl;
    std::cout << "========================" << std::endl;
    
    game.run();
    game.cleanup();
    
    return 0;
}
