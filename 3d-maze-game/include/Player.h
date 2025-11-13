#ifndef PLAYER_H
#define PLAYER_H

#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

class Player {
public:
    Player(const glm::vec3& startPos);
    
    void processKeyboard(int key, float deltaTime);
    void processMouse(float xoffset, float yoffset);
    
    glm::mat4 getViewMatrix() const;
    glm::vec3 getPosition() const { return position; }
    void setPosition(const glm::vec3& pos) { position = pos; }
    
    glm::vec3 getFront() const { return front; }
    glm::vec3 getRight() const { return right; }
    
    float getYaw() const { return yaw; }
    float getPitch() const { return pitch; }
    
private:
    glm::vec3 position;
    glm::vec3 front;
    glm::vec3 up;
    glm::vec3 right;
    glm::vec3 worldUp;
    
    float yaw;
    float pitch;
    
    float movementSpeed;
    float mouseSensitivity;
    
    void updateCameraVectors();
};

#endif
