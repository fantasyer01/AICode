// Game configuration constants
const CONFIG = {
    // Canvas settings
    CANVAS_WIDTH: 600,
    CANVAS_HEIGHT: 600,
    
    // Grid settings
    GRID_SIZE: 30,
    
    // Tank speed settings
    PLAYER_SPEED: 2,
    ENEMY_SPEED: 2,
    
    // Bullet speed settings
    PLAYER_BULLET_SPEED: 3,
    ENEMY_BULLET_SPEED: 3,
    
    // Shooting settings
    SHOT_COOLDOWN: 500, // milliseconds
    
    // Game settings
    PLAYER_INITIAL_LIVES: 3,
    LEVEL_TRANSITION_DELAY: 2000, // milliseconds
    
    // Player initial position
    PLAYER_INITIAL_X: 150,
    PLAYER_INITIAL_Y: 540,
    
    // Enemy spawn settings
    ENEMY_SPAWN_Y: 30, // Top row of the map
    ENEMY_SPAWN_INTERVAL: 3000, // milliseconds between enemy spawns
    ENEMY_SPAWN_POSITIONS_X: [60, 180, 300, 420, 540], // Possible X positions at top
    
    // Intro animation settings
    INTRO_DURATION: 2500, // milliseconds
    INTRO_FADE_IN: 800,
    INTRO_FADE_OUT: 500
};
