#ifndef MAZE_H
#define MAZE_H

#include <vector>
#include <glm/glm.hpp>

class Maze {
public:
    struct Cell {
        bool visited;
        bool walls[4]; // North, East, South, West
    };

    Maze(int width, int height, float cellSize);
    void generate();
    
    bool hasWall(int x, int y, int direction) const;
    glm::vec3 getStartPosition() const { return startPos; }
    glm::vec3 getExitPosition() const { return exitPos; }
    
    int getWidth() const { return width; }
    int getHeight() const { return height; }
    float getCellSize() const { return cellSize; }
    
    const std::vector<std::vector<Cell>>& getCells() const { return cells; }

private:
    int width;
    int height;
    float cellSize;
    std::vector<std::vector<Cell>> cells;
    glm::vec3 startPos;
    glm::vec3 exitPos;
    
    void generateDFS(int x, int y);
    std::vector<int> getUnvisitedNeighbors(int x, int y);
    void removeWall(int x1, int y1, int x2, int y2);
};

#endif
