#include "Maze.h"
#include <stack>
#include <cstdlib>
#include <ctime>
#include <algorithm>

enum Direction { NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3 };

Maze::Maze(int width, int height, float cellSize)
    : width(width), height(height), cellSize(cellSize) {
    cells.resize(height, std::vector<Cell>(width));
    
    // Initialize all cells
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            cells[y][x].visited = false;
            for (int i = 0; i < 4; i++) {
                cells[y][x].walls[i] = true;
            }
        }
    }
    
    // Seed random number generator
    srand(static_cast<unsigned int>(time(0)));
}

void Maze::generate() {
    // Start from (0, 0)
    generateDFS(0, 0);
    
    // Set start position at bottom-left
    startPos = glm::vec3(cellSize * 0.5f, 0.5f, cellSize * 0.5f);
    
    // Set exit position at top-right
    exitPos = glm::vec3(
        cellSize * (width - 0.5f),
        0.5f,
        cellSize * (height - 0.5f)
    );
}

void Maze::generateDFS(int startX, int startY) {
    std::stack<std::pair<int, int>> stack;
    stack.push({startX, startY});
    cells[startY][startX].visited = true;
    
    while (!stack.empty()) {
        int x = stack.top().first;
        int y = stack.top().second;
        
        std::vector<int> neighbors = getUnvisitedNeighbors(x, y);
        
        if (!neighbors.empty()) {
            // Choose random neighbor
            int dir = neighbors[rand() % neighbors.size()];
            
            int nx = x, ny = y;
            switch (dir) {
                case NORTH: ny--; break;
                case EAST:  nx++; break;
                case SOUTH: ny++; break;
                case WEST:  nx--; break;
            }
            
            // Remove wall between current and chosen cell
            removeWall(x, y, nx, ny);
            
            // Mark neighbor as visited and push to stack
            cells[ny][nx].visited = true;
            stack.push({nx, ny});
        } else {
            stack.pop();
        }
    }
}

std::vector<int> Maze::getUnvisitedNeighbors(int x, int y) {
    std::vector<int> neighbors;
    
    // North
    if (y > 0 && !cells[y - 1][x].visited)
        neighbors.push_back(NORTH);
    
    // East
    if (x < width - 1 && !cells[y][x + 1].visited)
        neighbors.push_back(EAST);
    
    // South
    if (y < height - 1 && !cells[y + 1][x].visited)
        neighbors.push_back(SOUTH);
    
    // West
    if (x > 0 && !cells[y][x - 1].visited)
        neighbors.push_back(WEST);
    
    return neighbors;
}

void Maze::removeWall(int x1, int y1, int x2, int y2) {
    if (x1 == x2) {
        if (y1 < y2) {
            cells[y1][x1].walls[SOUTH] = false;
            cells[y2][x2].walls[NORTH] = false;
        } else {
            cells[y1][x1].walls[NORTH] = false;
            cells[y2][x2].walls[SOUTH] = false;
        }
    } else {
        if (x1 < x2) {
            cells[y1][x1].walls[EAST] = false;
            cells[y2][x2].walls[WEST] = false;
        } else {
            cells[y1][x1].walls[WEST] = false;
            cells[y2][x2].walls[EAST] = false;
        }
    }
}

bool Maze::hasWall(int x, int y, int direction) const {
    if (x < 0 || x >= width || y < 0 || y >= height)
        return true;
    return cells[y][x].walls[direction];
}
