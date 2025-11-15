#ifndef CAMERA_H
#define CAMERA_H

#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

class Camera {
public:
    Camera(const glm::vec3& position = glm::vec3(0.0f, 5.0f, 20.0f),
           float yaw = -90.0f, float pitch = 0.0f);

    glm::mat4 getViewMatrix() const;
    glm::vec3 getPosition() const { return position; }
    glm::vec3 getFront() const { return front; }
    
    void processKeyboard(int direction, float deltaTime);
    void processMouseMovement(float xoffset, float yoffset);

    enum Movement {
        FORWARD = 0,
        BACKWARD = 1,
        LEFT = 2,
        RIGHT = 3
    };

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

#endif // CAMERA_H
