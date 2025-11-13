#include "Player.h"
#include <GLFW/glfw3.h>
#include <glm/gtc/matrix_transform.hpp>
#include <cmath>

Player::Player(const glm::vec3& startPos)
    : position(startPos),
      worldUp(0.0f, 1.0f, 0.0f),
      yaw(-90.0f),
      pitch(0.0f),
      movementSpeed(3.0f),
      mouseSensitivity(0.1f) {
    updateCameraVectors();
}

void Player::processKeyboard(int key, float deltaTime) {
    float velocity = movementSpeed * deltaTime;
    
    if (key == GLFW_KEY_W)
        position += front * velocity;
    if (key == GLFW_KEY_S)
        position -= front * velocity;
    if (key == GLFW_KEY_A)
        position -= right * velocity;
    if (key == GLFW_KEY_D)
        position += right * velocity;
    
    // Keep player at ground level
    position.y = 0.5f;
}

void Player::processMouse(float xoffset, float yoffset) {
    xoffset *= mouseSensitivity;
    yoffset *= mouseSensitivity;
    
    yaw += xoffset;
    pitch += yoffset;
    
    // Constrain pitch
    if (pitch > 89.0f)
        pitch = 89.0f;
    if (pitch < -89.0f)
        pitch = -89.0f;
    
    updateCameraVectors();
}

glm::mat4 Player::getViewMatrix() const {
    return glm::lookAt(position, position + front, up);
}

void Player::updateCameraVectors() {
    glm::vec3 newFront;
    newFront.x = cos(glm::radians(yaw)) * cos(glm::radians(pitch));
    newFront.y = sin(glm::radians(pitch));
    newFront.z = sin(glm::radians(yaw)) * cos(glm::radians(pitch));
    front = glm::normalize(newFront);
    
    right = glm::normalize(glm::cross(front, worldUp));
    up = glm::normalize(glm::cross(right, front));
}
