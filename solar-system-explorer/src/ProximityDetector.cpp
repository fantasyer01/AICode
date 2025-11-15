#include "ProximityDetector.h"
#include <glm/geometric.hpp>
#include <limits>

ProximityDetector::ProximityDetector(float detectionMultiplier)
    : detectionMultiplier(detectionMultiplier) {
}

std::shared_ptr<Planet> ProximityDetector::checkProximity(
    const glm::vec3& position, 
    const std::vector<std::shared_ptr<Planet>>& planets) {
    
    float minDistance = std::numeric_limits<float>::max();
    std::shared_ptr<Planet> closestPlanet = nullptr;
    
    for (const auto& planet : planets) {
        float distance = glm::distance(position, planet->getPosition());
        float detectionRadius = planet->getRadius() * detectionMultiplier;
        
        if (distance < detectionRadius && distance < minDistance) {
            minDistance = distance;
            closestPlanet = planet;
        }
    }
    
    return closestPlanet;
}
