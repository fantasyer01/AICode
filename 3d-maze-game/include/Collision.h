#ifndef COLLISION_H
#define COLLISION_H

#include <glm/glm.hpp>
#include "Maze.h"

class CollisionSystem {
public:
    CollisionSystem(Maze* maze);
    
    glm::vec3 checkCollision(const glm::vec3& currentPos, const glm::vec3& newPos, float playerRadius);
    
private:
    Maze* maze;
    
    bool circleAABBCollision(const glm::vec2& circlePos, float radius,
                             const glm::vec2& boxMin, const glm::vec2& boxMax);
};

#endif
