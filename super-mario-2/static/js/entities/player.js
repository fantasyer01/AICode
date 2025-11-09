// Player Entity Class
export class Player {
    constructor(x, y) {
        this.position = { x, y };
        this.velocity = { x: 0, y: 0 };
        this.size = { width: 32, height: 32 };
        this.state = 'idle'; // idle, walking, jumping, falling
        this.direction = 'right'; // left, right
        this.isGrounded = false;
        this.isAlive = true;
        this.lives = 3;
        this.invincible = false;
        this.invincibleTimer = 0;
        this.jumpPressed = false;
        this.jumpTimer = 0;
        
        // Physics constants
        this.gravity = 0.6;
        this.jumpVelocity = -12;
        this.shortJumpVelocity = -6;
        this.maxFallSpeed = 10;
        this.walkAcceleration = 0.5;
        this.maxWalkSpeed = 4;
        this.friction = 0.3;
        this.airControlFactor = 0.7;
    }

    update(inputHandler, deltaTime) {
        if (!this.isAlive) return;

        // Update invincibility timer
        if (this.invincible) {
            this.invincibleTimer--;
            if (this.invincibleTimer <= 0) {
                this.invincible = false;
            }
        }

        // Horizontal movement
        const acceleration = this.isGrounded ? this.walkAcceleration : this.walkAcceleration * this.airControlFactor;
        
        if (inputHandler.keys.left) {
            this.velocity.x -= acceleration;
            this.direction = 'left';
            this.state = 'walking';
        } else if (inputHandler.keys.right) {
            this.velocity.x += acceleration;
            this.direction = 'right';
            this.state = 'walking';
        } else {
            // Apply friction
            if (this.isGrounded) {
                if (Math.abs(this.velocity.x) < this.friction) {
                    this.velocity.x = 0;
                    this.state = 'idle';
                } else {
                    this.velocity.x -= Math.sign(this.velocity.x) * this.friction;
                }
            }
        }

        // Cap horizontal speed
        if (this.velocity.x > this.maxWalkSpeed) this.velocity.x = this.maxWalkSpeed;
        if (this.velocity.x < -this.maxWalkSpeed) this.velocity.x = -this.maxWalkSpeed;

        // Jumping
        if (inputHandler.keys.jump && this.isGrounded) {
            this.velocity.y = this.jumpVelocity;
            this.isGrounded = false;
            this.jumpPressed = true;
            this.jumpTimer = 0;
        }

        // Variable jump height
        if (this.jumpPressed) {
            this.jumpTimer++;
            if (!inputHandler.keys.jump || this.jumpTimer > 15) {
                this.jumpPressed = false;
                if (this.velocity.y < this.shortJumpVelocity) {
                    this.velocity.y = this.shortJumpVelocity;
                }
            }
        }

        // Apply gravity
        if (!this.isGrounded) {
            this.velocity.y += this.gravity;
            if (this.velocity.y > this.maxFallSpeed) {
                this.velocity.y = this.maxFallSpeed;
            }
        }

        // Update position
        this.position.x += this.velocity.x;
        this.position.y += this.velocity.y;

        // Update state
        if (!this.isGrounded) {
            this.state = this.velocity.y < 0 ? 'jumping' : 'falling';
        }
    }

    takeDamage() {
        if (this.invincible) return;
        
        this.lives--;
        if (this.lives <= 0) {
            this.isAlive = false;
        } else {
            this.invincible = true;
            this.invincibleTimer = 120; // 2 seconds at 60 FPS
        }
    }

    bounceOnEnemy() {
        this.velocity.y = -8; // Small bounce
    }

    reset(x, y) {
        this.position = { x, y };
        this.velocity = { x: 0, y: 0 };
        this.isGrounded = false;
        this.state = 'idle';
        this.direction = 'right';
        this.invincible = true;
        this.invincibleTimer = 60;
    }

    getBounds() {
        return {
            left: this.position.x,
            right: this.position.x + this.size.width,
            top: this.position.y,
            bottom: this.position.y + this.size.height
        };
    }
}
