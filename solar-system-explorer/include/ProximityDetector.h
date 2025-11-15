#ifndef PROXIMITYDETECTOR_H
#define PROXIMITYDETECTOR_H

#include "Planet.h"
#include <glm/glm.hpp>
#include <memory>
#include <vector>

class ProximityDetector {
public:
    ProximityDetector(float detectionMultiplier = 3.0f);
    
    std::shared_ptr<Planet> checkProximity(const glm::vec3& position, 
                                           const std::vector<std::shared_ptr<Planet>>& planets);
    
private:
    float detectionMultiplier;
};

#endif // PROXIMITYDETECTOR_H
