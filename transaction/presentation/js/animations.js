// Animation Manager - Database Transaction Presentation

class AnimationManager {
    constructor() {
        this.observers = new Map();
        this.init();
    }
    
    init() {
        this.setupIntersectionObserver();
        this.setupHoverAnimations();
    }
    
    setupIntersectionObserver() {
        // Observe elements that should animate when they come into view
        const observerOptions = {
            root: null,
            rootMargin: '0px',
            threshold: 0.1
        };
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    this.animateElement(entry.target);
                }
            });
        }, observerOptions);
        
        // Observe all animatable elements
        const animatableElements = document.querySelectorAll(
            '.card, .quadrant, .flow-step, .timeline-item, .step-box'
        );
        
        animatableElements.forEach(el => observer.observe(el));
        
        this.observers.set('intersection', observer);
    }
    
    animateElement(element) {
        // Add animation class if not already animated
        if (!element.classList.contains('animated')) {
            element.classList.add('animated');
            
            // Determine animation type based on element
            if (element.classList.contains('card')) {
                this.animateCard(element);
            } else if (element.classList.contains('flow-step')) {
                this.animateFlowStep(element);
            } else if (element.classList.contains('timeline-item')) {
                this.animateTimelineItem(element);
            }
        }
    }
    
    animateCard(card) {
        card.style.animation = 'none';
        setTimeout(() => {
            card.style.animation = 'zoomIn 0.5s ease-out';
        }, 10);
    }
    
    animateFlowStep(flowStep) {
        const elements = flowStep.querySelectorAll('.step-box, .flow-box, .arrow');
        elements.forEach((el, index) => {
            setTimeout(() => {
                el.style.animation = 'slideInBottom 0.6s ease-out';
            }, index * 200);
        });
    }
    
    animateTimelineItem(item) {
        item.style.animation = 'fadeIn 0.4s ease-out';
    }
    
    setupHoverAnimations() {
        // Add interactive hover effects
        const interactiveElements = document.querySelectorAll(
            '.card, .step-box, .flow-box, .quadrant'
        );
        
        interactiveElements.forEach(el => {
            el.addEventListener('mouseenter', () => {
                this.onHoverStart(el);
            });
            
            el.addEventListener('mouseleave', () => {
                this.onHoverEnd(el);
            });
        });
    }
    
    onHoverStart(element) {
        // Add pulse effect on hover
        element.style.transform = 'translateY(-5px) scale(1.02)';
        element.style.boxShadow = '0 6px 20px rgba(74, 144, 226, 0.2)';
        element.style.transition = 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)';
    }
    
    onHoverEnd(element) {
        element.style.transform = '';
        element.style.boxShadow = '';
    }
    
    // Play animation on specific element
    playAnimation(element, animationName, duration = '0.5s') {
        return new Promise((resolve) => {
            element.style.animation = `${animationName} ${duration} ease-out`;
            
            const handleAnimationEnd = () => {
                element.removeEventListener('animationend', handleAnimationEnd);
                resolve();
            };
            
            element.addEventListener('animationend', handleAnimationEnd);
        });
    }
    
    // Pause all animations
    pauseAnimations() {
        const style = document.createElement('style');
        style.id = 'pause-animations';
        style.textContent = '* { animation-play-state: paused !important; }';
        document.head.appendChild(style);
    }
    
    // Resume all animations
    resumeAnimations() {
        const style = document.getElementById('pause-animations');
        if (style) {
            style.remove();
        }
    }
    
    // Animate slide transition
    animateSlideTransition(oldSlide, newSlide, direction = 'next') {
        return new Promise((resolve) => {
            // Fade out old slide
            oldSlide.style.opacity = '0';
            oldSlide.style.transition = 'opacity 0.3s ease-out';
            
            setTimeout(() => {
                oldSlide.classList.remove('active');
                oldSlide.style.opacity = '';
                
                // Fade in new slide
                newSlide.classList.add('active');
                newSlide.style.opacity = '0';
                
                requestAnimationFrame(() => {
                    newSlide.style.opacity = '1';
                    newSlide.style.transition = 'opacity 0.3s ease-in';
                });
                
                setTimeout(resolve, 300);
            }, 300);
        });
    }
    
    // Highlight element temporarily
    highlightElement(element, duration = 2000) {
        element.classList.add('highlight-animate');
        
        setTimeout(() => {
            element.classList.remove('highlight-animate');
        }, duration);
    }
    
    // Shake element (for attention)
    shakeElement(element) {
        element.classList.add('shake');
        
        setTimeout(() => {
            element.classList.remove('shake');
        }, 500);
    }
    
    // Pulse element
    pulseElement(element, count = 3) {
        let pulseCount = 0;
        const pulseInterval = setInterval(() => {
            element.classList.add('pulse');
            
            setTimeout(() => {
                element.classList.remove('pulse');
            }, 1000);
            
            pulseCount++;
            if (pulseCount >= count) {
                clearInterval(pulseInterval);
            }
        }, 1500);
    }
    
    // Bounce element
    bounceElement(element) {
        element.classList.add('bounce');
        
        setTimeout(() => {
            element.classList.remove('bounce');
        }, 1000);
    }
    
    // Cascade animation for multiple elements
    cascadeAnimation(elements, animationName, delay = 200) {
        elements.forEach((el, index) => {
            setTimeout(() => {
                this.playAnimation(el, animationName);
            }, index * delay);
        });
    }
    
    // Timeline animation
    animateTimeline(timelineElement) {
        const items = timelineElement.querySelectorAll('.timeline-item');
        
        items.forEach((item, index) => {
            setTimeout(() => {
                item.style.animation = 'timelineAppear 0.4s ease-out';
                item.style.opacity = '1';
            }, index * 300);
        });
    }
    
    // Data flow animation
    animateDataFlow(startElement, endElement, duration = 1000) {
        const flowLine = document.createElement('div');
        flowLine.className = 'flow-line';
        
        const startRect = startElement.getBoundingClientRect();
        const endRect = endElement.getBoundingClientRect();
        
        flowLine.style.position = 'fixed';
        flowLine.style.left = `${startRect.right}px`;
        flowLine.style.top = `${startRect.top + startRect.height / 2}px`;
        flowLine.style.width = '2px';
        flowLine.style.height = `${Math.sqrt(
            Math.pow(endRect.left - startRect.right, 2) +
            Math.pow(endRect.top - startRect.top, 2)
        )}px`;
        flowLine.style.background = 'var(--primary-blue)';
        flowLine.style.transformOrigin = 'top';
        flowLine.style.transform = `rotate(${Math.atan2(
            endRect.top - startRect.top,
            endRect.left - startRect.right
        )}rad)`;
        
        document.body.appendChild(flowLine);
        
        setTimeout(() => {
            flowLine.remove();
        }, duration);
    }
    
    // Clean up
    destroy() {
        this.observers.forEach((observer) => {
            observer.disconnect();
        });
        this.observers.clear();
    }
}

// Initialize animation manager when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.animationManager = new AnimationManager();
    });
} else {
    window.animationManager = new AnimationManager();
}
