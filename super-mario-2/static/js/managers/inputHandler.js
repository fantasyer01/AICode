// Input Handler - Manages keyboard input
export class InputHandler {
    constructor() {
        this.keys = {
            left: false,
            right: false,
            jump: false,
            pause: false,
            restart: false
        };

        this.keyMap = {
            'ArrowLeft': 'left',
            'KeyA': 'left',
            'ArrowRight': 'right',
            'KeyD': 'right',
            'ArrowUp': 'jump',
            'KeyW': 'jump',
            'Space': 'jump',
            'KeyP': 'pause',
            'KeyR': 'restart'
        };

        // Bind event listeners
        window.addEventListener('keydown', this.handleKeyDown.bind(this));
        window.addEventListener('keyup', this.handleKeyUp.bind(this));
    }

    handleKeyDown(event) {
        const action = this.keyMap[event.code];
        if (action) {
            event.preventDefault();
            this.keys[action] = true;
        }
    }

    handleKeyUp(event) {
        const action = this.keyMap[event.code];
        if (action) {
            event.preventDefault();
            this.keys[action] = false;
        }
    }

    // Check if key was just pressed (for single press detection)
    wasPressed(action) {
        if (this.keys[action] && !this.previousKeys[action]) {
            return true;
        }
        return false;
    }

    update() {
        // Store previous state for press detection
        this.previousKeys = { ...this.keys };
    }

    reset() {
        this.keys = {
            left: false,
            right: false,
            jump: false,
            pause: false,
            restart: false
        };
    }
}
