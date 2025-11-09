// Enemy Entity Class
export class Enemy {
    constructor(x, y, type = 'walker', patrolMin = null, patrolMax = null) {
        this.position = { x, y };
        this.velocity = { x: -1, y: 0 }; // Start moving left
        this.size = { width: 32, height: 32 };
        this.type = type; // 'walker' or 'stationary'
        this.patrolRange = {
            min: patrolMin !== null ? patrolMin : x - 100,
            max: patrolMax !== null ? patrolMax : x + 100
        };
        this.direction = 'left';
        this.isAlive = true;
        this.gravity = 0.6;
        this.maxFallSpeed = 10;
    }

    update() {
        if (!this.isAlive) return;

        if (this.type === 'walker') {
            // Apply gravity
            this.velocity.y += this.gravity;
            if (this.velocity.y > this.maxFallSpeed) {
                this.velocity.y = this.maxFallSpeed;
            }

            // Update position
            this.position.x += this.velocity.x;
            this.position.y += this.velocity.y;

            // Patrol behavior - reverse direction at boundaries
            if (this.velocity.x < 0 && this.position.x <= this.patrolRange.min) {
                this.velocity.x = 1;
                this.direction = 'right';
            } else if (this.velocity.x > 0 && this.position.x >= this.patrolRange.max) {
                this.velocity.x = -1;
                this.direction = 'left';
            }
        } else if (this.type === 'stationary') {
            // Stationary enemies just apply gravity
            this.velocity.y += this.gravity;
            if (this.velocity.y > this.maxFallSpeed) {
                this.velocity.y = this.maxFallSpeed;
            }
            this.position.y += this.velocity.y;
        }
    }

    defeat() {
        this.isAlive = false;
    }

    reverseDirection() {
        this.velocity.x = -this.velocity.x;
        this.direction = this.direction === 'left' ? 'right' : 'left';
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
