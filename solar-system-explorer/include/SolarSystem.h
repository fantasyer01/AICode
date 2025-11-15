#ifndef SOLARSYSTEM_H
#define SOLARSYSTEM_H

#include "Planet.h"
#include <vector>
#include <memory>

class SolarSystem {
public:
    SolarSystem();
    ~SolarSystem();

    void update(float deltaTime);
    const std::vector<std::shared_ptr<Planet>>& getPlanets() const { return planets; }
    
private:
    std::vector<std::shared_ptr<Planet>> planets;
    
    void initializePlanets();
};

#endif // SOLARSYSTEM_H
