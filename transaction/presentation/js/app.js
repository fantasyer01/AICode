// Main Application - Database Transaction Presentation

class PresentationApp {
    constructor() {
        this.initialized = false;
        this.fullscreenEnabled = false;
        
        this.init();
    }
    
    init() {
        if (this.initialized) return;
        
        // Wait for all components to be ready
        if (typeof NavigationController === 'undefined' || 
            typeof ChartRenderer === 'undefined' || 
            typeof AnimationManager === 'undefined') {
            setTimeout(() => this.init(), 100);
            return;
        }
        
        this.setupApp();
        this.setupHelp();
        this.setupFullscreen();
        this.setupPrintMode();
        
        this.initialized = true;
        console.log('✅ Presentation initialized successfully');
    }
    
    setupApp() {
        // Add welcome message
        console.log('%c数据库事务基础技术分享', 'color: #4A90E2; font-size: 24px; font-weight: bold;');
        console.log('%c使用键盘方向键或空格键导航', 'color: #7AB8F5; font-size: 14px;');
        console.log('%c按 H 键查看帮助', 'color: #A8D0F0; font-size: 14px;');
        
        // Handle window resize
        window.addEventListener('resize', () => {
            this.handleResize();
        });
        
        // Handle visibility change (pause animations when tab is hidden)
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                if (window.animationManager) {
                    window.animationManager.pauseAnimations();
                }
            } else {
                if (window.animationManager) {
                    window.animationManager.resumeAnimations();
                }
            }
        });
        
        // Prevent accidental page refresh
        window.addEventListener('beforeunload', (e) => {
            if (window.navigationController && 
                window.navigationController.currentSlide > 0) {
                e.preventDefault();
                e.returnValue = '';
                return '';
            }
        });
    }
    
    handleResize() {
        // Resize charts
        if (window.chartRenderer) {
            window.chartRenderer.resizeAll();
        }
        
        // Adjust layout if needed
        this.adjustLayout();
    }
    
    adjustLayout() {
        const width = window.innerWidth;
        const slides = document.querySelectorAll('.slide');
        
        // Adjust font sizes for different screen sizes
        if (width < 1024) {
            slides.forEach(slide => {
                slide.style.fontSize = '14px';
            });
        } else if (width < 1366) {
            slides.forEach(slide => {
                slide.style.fontSize = '16px';
            });
        } else {
            slides.forEach(slide => {
                slide.style.fontSize = '18px';
            });
        }
    }
    
    setupHelp() {
        // Show help modal with keyboard shortcut 'H'
        document.addEventListener('keydown', (e) => {
            if (e.key === 'h' || e.key === 'H' || e.key === '?') {
                this.showHelp();
            } else if (e.key === 'Escape') {
                this.hideHelp();
            }
        });
    }
    
    showHelp() {
        let helpModal = document.getElementById('helpModal');
        
        if (!helpModal) {
            helpModal = this.createHelpModal();
            document.body.appendChild(helpModal);
        }
        
        helpModal.style.display = 'flex';
        
        // Close on click outside
        helpModal.addEventListener('click', (e) => {
            if (e.target === helpModal) {
                this.hideHelp();
            }
        });
    }
    
    hideHelp() {
        const helpModal = document.getElementById('helpModal');
        if (helpModal) {
            helpModal.style.display = 'none';
        }
    }
    
    createHelpModal() {
        const modal = document.createElement('div');
        modal.id = 'helpModal';
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 10000;
        `;
        
        const content = document.createElement('div');
        content.style.cssText = `
            background: white;
            padding: 40px;
            border-radius: 12px;
            max-width: 600px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
        `;
        
        content.innerHTML = `
            <h2 style="color: #4A90E2; margin-bottom: 20px;">键盘快捷键</h2>
            <div style="display: grid; grid-template-columns: 150px 1fr; gap: 15px; font-size: 16px;">
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">→ / Space</kbd>
                <span>下一张幻灯片</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">←</kbd>
                <span>上一张幻灯片</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">Home</kbd>
                <span>第一张幻灯片</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">End</kbd>
                <span>最后一张幻灯片</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">F11</kbd>
                <span>全屏模式</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">H / ?</kbd>
                <span>显示此帮助</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">Esc</kbd>
                <span>关闭帮助</span>
                
                <kbd style="background: #f0f0f0; padding: 5px 10px; border-radius: 4px;">Ctrl + P</kbd>
                <span>打印/导出 PDF</span>
            </div>
            <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0;">
                <p style="color: #666; margin: 10px 0;">💡 提示：也可以点击页面任意位置前进，或使用侧边栏快速导航。</p>
                <p style="color: #666; margin: 10px 0;">🖱️ 鼠标悬停在图表和卡片上可查看更多信息。</p>
            </div>
            <button style="
                margin-top: 20px;
                padding: 10px 30px;
                background: #4A90E2;
                color: white;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-size: 16px;
            " onclick="window.presentationApp.hideHelp()">关闭</button>
        `;
        
        modal.appendChild(content);
        return modal;
    }
    
    setupFullscreen() {
        // Toggle fullscreen with F11 or F key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'F11') {
                e.preventDefault();
                this.toggleFullscreen();
            } else if (e.key === 'f' || e.key === 'F') {
                if (e.ctrlKey || e.metaKey) {
                    e.preventDefault();
                    this.toggleFullscreen();
                }
            }
        });
    }
    
    toggleFullscreen() {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().catch(err => {
                console.error('无法进入全屏模式:', err);
            });
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            }
        }
    }
    
    setupPrintMode() {
        // Detect print mode
        window.addEventListener('beforeprint', () => {
            console.log('准备打印...');
            this.enterPrintMode();
        });
        
        window.addEventListener('afterprint', () => {
            console.log('打印完成');
            this.exitPrintMode();
        });
    }
    
    enterPrintMode() {
        // Show all slides for printing
        const slides = document.querySelectorAll('.slide');
        slides.forEach(slide => {
            slide.style.display = 'flex';
            slide.style.opacity = '1';
            slide.style.pageBreakAfter = 'always';
        });
    }
    
    exitPrintMode() {
        // Restore normal mode
        const slides = document.querySelectorAll('.slide');
        slides.forEach((slide, index) => {
            if (index !== window.navigationController.currentSlide) {
                slide.style.display = 'none';
            }
            slide.style.pageBreakAfter = '';
        });
    }
    
    // Export presentation state
    exportState() {
        return {
            currentSlide: window.navigationController ? window.navigationController.currentSlide : 0,
            timestamp: new Date().toISOString()
        };
    }
    
    // Import presentation state
    importState(state) {
        if (state && state.currentSlide && window.navigationController) {
            window.navigationController.gotoSlide(state.currentSlide);
        }
    }
}

// Initialize app when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.presentationApp = new PresentationApp();
    });
} else {
    window.presentationApp = new PresentationApp();
}

// Global error handler
window.addEventListener('error', (e) => {
    console.error('Application Error:', e.error);
});

// Service Worker registration (optional, for offline support)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        // Uncomment to enable service worker
        // navigator.serviceWorker.register('/sw.js')
        //     .then(reg => console.log('Service Worker registered:', reg))
        //     .catch(err => console.log('Service Worker registration failed:', err));
    });
}
