#include "SolarSystem.h"

SolarSystem::SolarSystem() {
    initializePlanets();
}

SolarSystem::~SolarSystem() {
}

void SolarSystem::update(float deltaTime) {
    for (auto& planet : planets) {
        planet->update(deltaTime);
    }
}

void SolarSystem::initializePlanets() {
    // Sun - center of the solar system
    planets.push_back(std::make_shared<Planet>(
        "Sun", 3.0f, glm::vec3(1.0f, 0.9f, 0.2f), 0.0f, 0.0f, true
    ));
    
    // Mercury - small, gray-brown
    planets.push_back(std::make_shared<Planet>(
        "Mercury", 0.4f, glm::vec3(0.5f, 0.5f, 0.5f), 8.0f, 0.16f
    ));
    
    // Venus - yellowish-white
    planets.push_back(std::make_shared<Planet>(
        "Venus", 0.9f, glm::vec3(0.9f, 0.8f, 0.6f), 12.0f, 0.12f
    ));
    
    // Earth - blue-green
    planets.push_back(std::make_shared<Planet>(
        "Earth", 1.0f, glm::vec3(0.2f, 0.5f, 0.9f), 16.0f, 0.10f
    ));
    
    // Mars - reddish
    planets.push_back(std::make_shared<Planet>(
        "Mars", 0.5f, glm::vec3(0.9f, 0.3f, 0.2f), 20.0f, 0.08f
    ));
    
    // Jupiter - large, orange-brown with bands
    planets.push_back(std::make_shared<Planet>(
        "Jupiter", 2.5f, glm::vec3(0.8f, 0.6f, 0.4f), 28.0f, 0.05f
    ));
    
    // Saturn - pale yellow
    planets.push_back(std::make_shared<Planet>(
        "Saturn", 2.2f, glm::vec3(0.9f, 0.8f, 0.5f), 36.0f, 0.04f
    ));
    
    // Uranus - cyan-blue
    planets.push_back(std::make_shared<Planet>(
        "Uranus", 1.6f, glm::vec3(0.4f, 0.8f, 0.9f), 44.0f, 0.03f
    ));
    
    // Neptune - deep blue
    planets.push_back(std::make_shared<Planet>(
        "Neptune", 1.5f, glm::vec3(0.2f, 0.3f, 0.9f), 52.0f, 0.02f
    ));
}
