#include "Planet.h"
#include <glm/gtc/constants.hpp>
#include <cmath>

Planet::Planet(const std::string& name, float radius, const glm::vec3& color,
               float orbitalRadius, float orbitalSpeed, bool isSun)
    : name(name), radius(radius), color(color), orbitalRadius(orbitalRadius),
      orbitalSpeed(orbitalSpeed), currentAngle(0.0f), isSun(isSun) {
    updatePosition();
}

void Planet::update(float deltaTime) {
    if (!isSun) {
        currentAngle += orbitalSpeed * deltaTime;
        if (currentAngle > 2.0f * glm::pi<float>()) {
            currentAngle -= 2.0f * glm::pi<float>();
        }
        updatePosition();
    }
}

void Planet::updatePosition() {
    if (isSun) {
        position = glm::vec3(0.0f, 0.0f, 0.0f);
    } else {
        position.x = orbitalRadius * cos(currentAngle);
        position.y = 0.0f;
        position.z = orbitalRadius * sin(currentAngle);
    }
}
