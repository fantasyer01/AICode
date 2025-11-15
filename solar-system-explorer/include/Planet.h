#ifndef PLANET_H
#define PLANET_H

#include <glm/glm.hpp>
#include <string>

class Planet {
public:
    Planet(const std::string& name, float radius, const glm::vec3& color,
           float orbitalRadius, float orbitalSpeed, bool isSun = false);

    void update(float deltaTime);
    
    glm::vec3 getPosition() const { return position; }
    float getRadius() const { return radius; }
    glm::vec3 getColor() const { return color; }
    std::string getName() const { return name; }
    bool getIsSun() const { return isSun; }
    float getOrbitalRadius() const { return orbitalRadius; }

private:
    std::string name;
    float radius;
    glm::vec3 color;
    float orbitalRadius;
    float orbitalSpeed;
    float currentAngle;
    glm::vec3 position;
    bool isSun;

    void updatePosition();
};

#endif // PLANET_H
