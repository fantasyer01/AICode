// Navigation Controller - Database Transaction Presentation

class NavigationController {
    constructor() {
        this.slides = document.querySelectorAll('.slide');
        this.currentSlide = 0;
        this.totalSlides = this.slides.length;
        
        this.init();
    }
    
    init() {
        // Initialize first slide
        if (this.slides.length > 0 && !this.slides[0].classList.contains('active')) {
            this.slides[0].classList.add('active');
        }
        
        // Update initial state
        this.updateProgress();
        this.updatePageNumber();
        this.updateChapterIndicator();
        
        // Keyboard navigation
        document.addEventListener('keydown', (e) => this.handleKeyPress(e));
        
        // Mouse click navigation
        document.addEventListener('click', (e) => this.handleClick(e));
        
        // Sidebar navigation
        this.setupSidebarNavigation();
        
        // Touch/Swipe support
        this.setupTouchNavigation();
        
        console.log(`Presentation loaded with ${this.totalSlides} slides`);
    }
    
    handleKeyPress(event) {
        switch(event.key) {
            case 'ArrowRight':
            case 'ArrowDown':
            case ' ': // Space key
                event.preventDefault();
                this.nextSlide();
                break;
            case 'ArrowLeft':
            case 'ArrowUp':
                event.preventDefault();
                this.prevSlide();
                break;
            case 'Home':
                event.preventDefault();
                this.gotoSlide(0);
                break;
            case 'End':
                event.preventDefault();
                this.gotoSlide(this.totalSlides - 1);
                break;
        }
    }
    
    handleClick(event) {
        // Ignore clicks on interactive elements
        if (event.target.closest('.sidebar-nav') || 
            event.target.closest('.sidebar-toggle') ||
            event.target.closest('a') ||
            event.target.closest('button')) {
            return;
        }
        
        // Click to advance
        this.nextSlide();
    }
    
    nextSlide() {
        if (this.currentSlide < this.totalSlides - 1) {
            this.gotoSlide(this.currentSlide + 1);
        }
    }
    
    prevSlide() {
        if (this.currentSlide > 0) {
            this.gotoSlide(this.currentSlide - 1);
        }
    }
    
    gotoSlide(index) {
        if (index < 0 || index >= this.totalSlides) return;
        
        // Hide current slide
        this.slides[this.currentSlide].classList.remove('active');
        
        // Show new slide
        this.currentSlide = index;
        this.slides[this.currentSlide].classList.add('active');
        
        // Update progress and indicators
        this.updateProgress();
        this.updatePageNumber();
        this.updateChapterIndicator();
        
        // Trigger slide-specific animations
        this.triggerSlideAnimations();
    }
    
    updateProgress() {
        const progress = ((this.currentSlide + 1) / this.totalSlides) * 100;
        const progressBar = document.getElementById('progressBar');
        if (progressBar) {
            progressBar.style.width = `${progress}%`;
        }
    }
    
    updatePageNumber() {
        const pageIndicator = document.getElementById('pageIndicator');
        if (pageIndicator) {
            pageIndicator.textContent = `${this.currentSlide + 1} / ${this.totalSlides}`;
        }
    }
    
    updateChapterIndicator() {
        const currentSlideElement = this.slides[this.currentSlide];
        const chapter = currentSlideElement.getAttribute('data-chapter');
        const chapterIndicator = document.getElementById('chapterIndicator');
        
        if (chapterIndicator && chapter && chapter !== '0') {
            const chapterNames = {
                '1': '第一章：事务基础与并发挑战',
                '2': '第二章：深入单机事务引擎',
                '3': '第三章：Spring 事务实战与避坑',
                '4': '第四章：分布式事务概览',
                '5': '第五章：总结与参考'
            };
            
            chapterIndicator.textContent = chapterNames[chapter] || '';
            chapterIndicator.classList.add('visible');
        } else if (chapterIndicator) {
            chapterIndicator.classList.remove('visible');
        }
    }
    
    setupSidebarNavigation() {
        const sidebar = document.getElementById('sidebarNav');
        const toggle = document.getElementById('sidebarToggle');
        
        if (toggle) {
            toggle.addEventListener('click', (e) => {
                e.stopPropagation();
                sidebar.classList.toggle('open');
            });
        }
        
        // Close sidebar when clicking outside
        document.addEventListener('click', (e) => {
            if (sidebar.classList.contains('open') && 
                !sidebar.contains(e.target) && 
                e.target !== toggle) {
                sidebar.classList.remove('open');
            }
        });
        
        // Sidebar link navigation
        const sidebarLinks = document.querySelectorAll('.sidebar-content a');
        sidebarLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const slideIndex = parseInt(link.getAttribute('data-slide'));
                if (!isNaN(slideIndex)) {
                    this.gotoSlide(slideIndex);
                    sidebar.classList.remove('open');
                }
            });
        });
    }
    
    setupTouchNavigation() {
        let touchStartX = 0;
        let touchEndX = 0;
        
        document.addEventListener('touchstart', (e) => {
            touchStartX = e.changedTouches[0].screenX;
        });
        
        document.addEventListener('touchend', (e) => {
            touchEndX = e.changedTouches[0].screenX;
            this.handleSwipe();
        });
        
        this.handleSwipe = () => {
            const swipeThreshold = 50;
            const diff = touchStartX - touchEndX;
            
            if (Math.abs(diff) > swipeThreshold) {
                if (diff > 0) {
                    // Swipe left - next slide
                    this.nextSlide();
                } else {
                    // Swipe right - previous slide
                    this.prevSlide();
                }
            }
        };
    }
    
    triggerSlideAnimations() {
        // Trigger animations specific to current slide
        const currentSlideElement = this.slides[this.currentSlide];
        
        // Re-trigger cascade animations for cards
        const cards = currentSlideElement.querySelectorAll('.card');
        cards.forEach((card, index) => {
            card.style.animation = 'none';
            setTimeout(() => {
                card.style.animation = '';
            }, 10);
        });
        
        // Trigger timeline animations
        const timelineItems = currentSlideElement.querySelectorAll('.timeline-item');
        timelineItems.forEach((item, index) => {
            setTimeout(() => {
                item.classList.add('animate');
            }, index * 200);
        });
    }
}

// Initialize navigation when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.navigationController = new NavigationController();
    });
} else {
    window.navigationController = new NavigationController();
}

// Initialize navigation when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.navigationController = new NavigationController();
    });
} else {
    window.navigationController = new NavigationController();
}
