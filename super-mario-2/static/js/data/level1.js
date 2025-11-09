// Level 1 Data - Introduction Level
export const level1 = {
    id: 1,
    name: 'LEVEL 1-1',
    width: 3200,
    height: 600,
    background: '#5C94FC',
    playerStart: { x: 100, y: 400 },
    goalPosition: { x: 3000, y: 440 },
    
    platforms: [
        // Ground floor
        { x: 0, y: 550, width: 800, height: 50, type: 'solid' },
        { x: 900, y: 550, width: 400, height: 50, type: 'solid' },
        { x: 1400, y: 550, width: 600, height: 50, type: 'solid' },
        { x: 2100, y: 550, width: 1100, height: 50, type: 'solid' },
        
        // Floating platforms
        { x: 400, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 600, y: 380, width: 128, height: 32, type: 'solid' },
        { x: 1100, y: 450, width: 96, height: 32, type: 'solid' },
        { x: 1250, y: 400, width: 96, height: 32, type: 'solid' },
        { x: 1700, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 1900, y: 400, width: 96, height: 32, type: 'solid' },
        { x: 2400, y: 450, width: 128, height: 32, type: 'solid' },
    ],
    
    enemies: [
        { x: 500, y: 500, type: 'walker', patrolMin: 450, patrolMax: 650 },
        { x: 1000, y: 500, type: 'walker', patrolMin: 900, patrolMax: 1200 },
        { x: 1500, y: 500, type: 'walker', patrolMin: 1400, patrolMax: 1900 },
        { x: 2200, y: 500, type: 'stationary' },
        { x: 2600, y: 500, type: 'walker', patrolMin: 2100, patrolMax: 2900 },
    ],
    
    collectibles: [
        { x: 300, y: 500, type: 'coin', value: 100 },
        { x: 450, y: 410, type: 'coin', value: 100 },
        { x: 650, y: 340, type: 'coin', value: 100 },
        { x: 1150, y: 410, type: 'coin', value: 100 },
        { x: 1300, y: 360, type: 'coin', value: 100 },
        { x: 1750, y: 410, type: 'coin', value: 100 },
        { x: 1950, y: 360, type: 'coin', value: 100 },
        { x: 2450, y: 410, type: 'coin', value: 100 },
        { x: 2700, y: 500, type: 'coin', value: 100 },
        { x: 2850, y: 500, type: 'coin', value: 100 },
    ]
};
