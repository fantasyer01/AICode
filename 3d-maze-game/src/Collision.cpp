#include "Collision.h"
#include <cmath>
#include <algorithm>

CollisionSystem::CollisionSystem(Maze* maze) : maze(maze) {}

glm::vec3 CollisionSystem::checkCollision(const glm::vec3& currentPos, const glm::vec3& newPos, float playerRadius) {
    glm::vec3 resultPos = newPos;
    float cellSize = maze->getCellSize();
    float wallThickness = 0.2f;
    
    // Get current cell
    int cellX = static_cast<int>(newPos.x / cellSize);
    int cellY = static_cast<int>(newPos.z / cellSize);
    
    // Check walls in the current and adjacent cells
    for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
            int checkX = cellX + dx;
            int checkY = cellY + dy;
            
            if (checkX < 0 || checkX >= maze->getWidth() ||
                checkY < 0 || checkY >= maze->getHeight())
                continue;
            
            const auto& cell = maze->getCells()[checkY][checkX];
            
            // Check each wall of the cell
            float x = checkX * cellSize;
            float y = checkY * cellSize;
            
            // North wall
            if (cell.walls[0]) {
                glm::vec2 boxMin(x, y - wallThickness / 2.0f);
                glm::vec2 boxMax(x + cellSize, y + wallThickness / 2.0f);
                if (circleAABBCollision(glm::vec2(resultPos.x, resultPos.z), playerRadius, boxMin, boxMax)) {
                    // Push player away from wall
                    if (resultPos.z < y) {
                        resultPos.z = y - playerRadius - wallThickness / 2.0f;
                    } else {
                        resultPos.z = y + playerRadius + wallThickness / 2.0f;
                    }
                }
            }
            
            // South wall
            if (cell.walls[2]) {
                glm::vec2 boxMin(x, y + cellSize - wallThickness / 2.0f);
                glm::vec2 boxMax(x + cellSize, y + cellSize + wallThickness / 2.0f);
                if (circleAABBCollision(glm::vec2(resultPos.x, resultPos.z), playerRadius, boxMin, boxMax)) {
                    if (resultPos.z < y + cellSize) {
                        resultPos.z = y + cellSize - playerRadius - wallThickness / 2.0f;
                    } else {
                        resultPos.z = y + cellSize + playerRadius + wallThickness / 2.0f;
                    }
                }
            }
            
            // West wall
            if (cell.walls[3]) {
                glm::vec2 boxMin(x - wallThickness / 2.0f, y);
                glm::vec2 boxMax(x + wallThickness / 2.0f, y + cellSize);
                if (circleAABBCollision(glm::vec2(resultPos.x, resultPos.z), playerRadius, boxMin, boxMax)) {
                    if (resultPos.x < x) {
                        resultPos.x = x - playerRadius - wallThickness / 2.0f;
                    } else {
                        resultPos.x = x + playerRadius + wallThickness / 2.0f;
                    }
                }
            }
            
            // East wall
            if (cell.walls[1]) {
                glm::vec2 boxMin(x + cellSize - wallThickness / 2.0f, y);
                glm::vec2 boxMax(x + cellSize + wallThickness / 2.0f, y + cellSize);
                if (circleAABBCollision(glm::vec2(resultPos.x, resultPos.z), playerRadius, boxMin, boxMax)) {
                    if (resultPos.x < x + cellSize) {
                        resultPos.x = x + cellSize - playerRadius - wallThickness / 2.0f;
                    } else {
                        resultPos.x = x + cellSize + playerRadius + wallThickness / 2.0f;
                    }
                }
            }
        }
    }
    
    return resultPos;
}

bool CollisionSystem::circleAABBCollision(const glm::vec2& circlePos, float radius,
                                         const glm::vec2& boxMin, const glm::vec2& boxMax) {
    // Find closest point on AABB to circle center
    float closestX = std::max(boxMin.x, std::min(circlePos.x, boxMax.x));
    float closestY = std::max(boxMin.y, std::min(circlePos.y, boxMax.y));
    
    // Calculate distance
    float distX = circlePos.x - closestX;
    float distY = circlePos.y - closestY;
    float distSquared = distX * distX + distY * distY;
    
    return distSquared < (radius * radius);
}
