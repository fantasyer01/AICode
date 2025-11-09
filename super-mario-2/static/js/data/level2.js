// Level 2 Data - Challenge Level
export const level2 = {
    id: 2,
    name: 'LEVEL 1-2',
    width: 4000,
    height: 600,
    background: '#5C94FC',
    playerStart: { x: 100, y: 400 },
    goalPosition: { x: 3800, y: 440 },
    
    platforms: [
        // Ground segments with gaps
        { x: 0, y: 550, width: 600, height: 50, type: 'solid' },
        { x: 750, y: 550, width: 300, height: 50, type: 'solid' },
        { x: 1200, y: 550, width: 400, height: 50, type: 'solid' },
        { x: 1750, y: 550, width: 350, height: 50, type: 'solid' },
        { x: 2250, y: 550, width: 400, height: 50, type: 'solid' },
        { x: 2800, y: 550, width: 500, height: 50, type: 'solid' },
        { x: 3450, y: 550, width: 550, height: 50, type: 'solid' },
        
        // Multi-level platforms
        { x: 350, y: 450, width: 96, height: 32, type: 'solid' },
        { x: 500, y: 380, width: 96, height: 32, type: 'solid' },
        { x: 650, y: 310, width: 96, height: 32, type: 'solid' },
        
        { x: 900, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 1050, y: 400, width: 96, height: 32, type: 'solid' },
        
        { x: 1350, y: 450, width: 96, height: 32, type: 'solid' },
        { x: 1500, y: 400, width: 128, height: 32, type: 'solid' },
        { x: 1650, y: 350, width: 96, height: 32, type: 'solid' },
        
        { x: 1900, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 2100, y: 450, width: 96, height: 32, type: 'solid' },
        
        { x: 2400, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 2550, y: 380, width: 96, height: 32, type: 'solid' },
        
        { x: 3000, y: 450, width: 128, height: 32, type: 'solid' },
        { x: 3200, y: 400, width: 96, height: 32, type: 'solid' },
        { x: 3350, y: 450, width: 96, height: 32, type: 'solid' },
    ],
    
    enemies: [
        { x: 400, y: 500, type: 'walker', patrolMin: 0, patrolMax: 550 },
        { x: 800, y: 500, type: 'walker', patrolMin: 750, patrolMax: 1000 },
        { x: 1300, y: 500, type: 'walker', patrolMin: 1200, patrolMax: 1550 },
        { x: 1550, y: 360, type: 'stationary' },
        { x: 1850, y: 500, type: 'walker', patrolMin: 1750, patrolMax: 2050 },
        { x: 2000, y: 410, type: 'stationary' },
        { x: 2400, y: 500, type: 'walker', patrolMin: 2250, patrolMax: 2600 },
        { x: 2900, y: 500, type: 'walker', patrolMin: 2800, patrolMax: 3250 },
        { x: 3500, y: 500, type: 'walker', patrolMin: 3450, patrolMax: 3850 },
    ],
    
    collectibles: [
        { x: 250, y: 500, type: 'coin', value: 100 },
        { x: 400, y: 410, type: 'coin', value: 100 },
        { x: 550, y: 340, type: 'coin', value: 100 },
        { x: 700, y: 270, type: 'coin', value: 100 },
        { x: 950, y: 410, type: 'coin', value: 100 },
        { x: 1100, y: 360, type: 'coin', value: 100 },
        { x: 1400, y: 410, type: 'coin', value: 100 },
        { x: 1550, y: 360, type: 'coin', value: 100 },
        { x: 1700, y: 310, type: 'coin', value: 100 },
        { x: 1950, y: 410, type: 'coin', value: 100 },
        { x: 2150, y: 410, type: 'coin', value: 100 },
        { x: 2450, y: 410, type: 'coin', value: 100 },
        { x: 2600, y: 340, type: 'coin', value: 100 },
        { x: 3050, y: 410, type: 'coin', value: 100 },
        { x: 3250, y: 360, type: 'coin', value: 100 },
        { x: 3400, y: 410, type: 'coin', value: 100 },
        { x: 3650, y: 500, type: 'coin', value: 100 },
    ]
};
