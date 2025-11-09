// Platform Entity Class
export class Platform {
    constructor(x, y, width, height, type = 'solid') {
        this.position = { x, y };
        this.size = { width, height };
        this.type = type; // 'solid', 'breakable', etc.
        this.isCollidable = true;
    }

    update() {
        // Platforms are typically static, but this method exists for future moving platforms
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

// Collectible Entity Class
export class Collectible {
    constructor(x, y, type = 'coin', value = 100) {
        this.position = { x, y };
        this.size = { width: 24, height: 24 };
        this.type = type; // 'coin', etc.
        this.value = value; // Points awarded
        this.isCollected = false;
        this.animationFrame = 0;
    }

    update() {
        if (this.isCollected) return;
        
        // Simple animation
        this.animationFrame = (this.animationFrame + 0.1) % 360;
    }

    collect() {
        this.isCollected = true;
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

// Goal/Flag Entity Class
export class Goal {
    constructor(x, y) {
        this.position = { x, y };
        this.size = { width: 32, height: 64 };
        this.reached = false;
    }

    update() {
        // Animation could be added here
    }

    reach() {
        this.reached = true;
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
