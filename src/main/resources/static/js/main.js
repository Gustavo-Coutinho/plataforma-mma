/**
 * Main Application Module
 * Coordinates all modules and handles global functionality
 */

class App {
    constructor() {
        this.version = '1.0.0';
        this.initialized = false;
        
        this.init();
    }

    init() {
        console.log(`🚀 MMA Facial Auth System v${this.version} - Initializing...`);
        
        // Check browser compatibility
        this.checkBrowserCompatibility();
        
        // Setup global error handling
        this.setupErrorHandling();
        
        // Setup keyboard shortcuts
        this.setupKeyboardShortcuts();
        
        // Setup responsive handlers
        this.setupResponsiveHandlers();
        
        // Initialize tooltips and popovers
        this.initializeBootstrapComponents();
        
        // Setup HTTPS warning
        this.checkSecureContext();
        
        console.log('✅ Application initialized successfully');
        this.initialized = true;
    }

    checkBrowserCompatibility() {
        const required = {
            'getUserMedia': !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia),
            'canvas': !!document.createElement('canvas').getContext,
            'localStorage': !!window.localStorage,
            'fetch': !!window.fetch,
            'WebGL': !!window.WebGLRenderingContext
        };

        const missing = Object.keys(required).filter(key => !required[key]);
        
        if (missing.length > 0) {
            console.warn('⚠️ Missing browser features:', missing);
            this.showCompatibilityWarning(missing);
        }

        // Check for modern JavaScript features
        try {
            eval('const test = async () => {}; class Test {}');
        } catch (e) {
            this.showCompatibilityWarning(['Modern JavaScript (ES6+)']);
        }
    }

    showCompatibilityWarning(missing) {
        const warning = document.createElement('div');
        warning.className = 'alert alert-warning alert-dismissible fade show';
        warning.style.cssText = 'position: fixed; top: 20px; left: 20px; right: 20px; z-index: 10000;';
        warning.innerHTML = `
            <i class="fas fa-exclamation-triangle me-2"></i>
            <strong>Navegador Não Compatível:</strong>
            Seu navegador não suporta: ${missing.join(', ')}.
            <br><small>Recomendamos usar Chrome, Firefox, Safari ou Edge atualizado.</small>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.insertBefore(warning, document.body.firstChild);
    }

    setupErrorHandling() {
        // Global error handler
        window.addEventListener('error', (event) => {
            console.error('💥 Global Error:', event.error);
            this.logError('JavaScript Error', event.error);
        });

        // Unhandled promise rejection handler
        window.addEventListener('unhandledrejection', (event) => {
            console.error('💥 Unhandled Promise Rejection:', event.reason);
            this.logError('Promise Rejection', event.reason);
        });
    }

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (event) => {
            // Ctrl/Cmd + Enter - Start camera or authenticate
            if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
                event.preventDefault();
                
                if (window.cameraManager) {
                    if (window.cameraManager.isReady()) {
                        const authBtn = document.getElementById('authenticateBtn');
                        if (authBtn && !authBtn.classList.contains('d-none')) {
                            authBtn.click();
                        }
                    } else {
                        const startBtn = document.getElementById('startCameraBtn');
                        if (startBtn && !startBtn.classList.contains('d-none')) {
                            startBtn.click();
                        }
                    }
                }
            }

            // Escape - Stop camera
            if (event.key === 'Escape') {
                if (window.cameraManager && window.cameraManager.isReady()) {
                    const stopBtn = document.getElementById('stopCameraBtn');
                    if (stopBtn && !stopBtn.classList.contains('d-none')) {
                        stopBtn.click();
                    }
                }
            }

            // F1 - Help
            if (event.key === 'F1') {
                event.preventDefault();
                window.open('/help', '_blank');
            }
        });
    }

    setupResponsiveHandlers() {
        // Handle orientation changes
        window.addEventListener('orientationchange', () => {
            setTimeout(() => {
                if (window.cameraManager && window.cameraManager.isReady()) {
                    // Restart camera to adjust to new orientation
                    window.cameraManager.stopCamera();
                    setTimeout(() => {
                        window.cameraManager.startCamera();
                    }, 1000);
                }
            }, 500);
        });

        // Handle visibility changes
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                // Page is hidden - pause non-essential operations
                if (window.cameraManager) {
                    window.cameraManager.stopFaceDetection();
                }
            } else {
                // Page is visible - resume operations
                if (window.cameraManager && window.cameraManager.isReady()) {
                    window.cameraManager.startFaceDetection();
                }
            }
        });
    }

    initializeBootstrapComponents() {
        // Initialize tooltips
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });

        // Initialize popovers
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }

    checkSecureContext() {
        if (location.protocol !== 'https:' && location.hostname !== 'localhost') {
            const warning = document.createElement('div');
            warning.className = 'alert alert-danger';
            warning.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; z-index: 10001; border-radius: 0;';
            warning.innerHTML = `
                <i class="fas fa-shield-alt me-2"></i>
                <strong>Conexão Insegura:</strong>
                Para usar a câmera, acesse via HTTPS ou localhost.
            `;
            
            document.body.insertBefore(warning, document.body.firstChild);
        }
    }

    logError(type, error) {
        // In production, this would send errors to a logging service
        const errorData = {
            type: type,
            message: error.message || error.toString(),
            stack: error.stack,
            timestamp: new Date().toISOString(),
            userAgent: navigator.userAgent,
            url: window.location.href
        };

        // Store locally for debugging
        try {
            const errors = JSON.parse(localStorage.getItem('mma_errors') || '[]');
            errors.push(errorData);
            
            // Keep only last 10 errors
            if (errors.length > 10) {
                errors.splice(0, errors.length - 10);
            }
            
            localStorage.setItem('mma_errors', JSON.stringify(errors));
        } catch (e) {
            console.error('Failed to store error:', e);
        }
    }

    // Utility functions
    static formatDateTime(date) {
        return new Intl.DateTimeFormat('pt-BR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        }).format(date);
    }

    static formatFileSize(bytes) {
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        if (bytes === 0) return '0 Bytes';
        const i = Math.floor(Math.log(bytes) / Math.log(1024));
        return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i];
    }

    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    static throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    // Performance monitoring
    measurePerformance(name, fn) {
        const start = performance.now();
        const result = fn();
        const end = performance.now();
        
        console.log(`⏱️ ${name}: ${end - start}ms`);
        
        return result;
    }

    // Health check
    async healthCheck() {
        try {
            const response = await fetch('/api/auth/status');
            const data = await response.json();
            
            console.log('🏥 Health Check:', data.success ? '✅ OK' : '❌ Failed');
            return data.success;
        } catch (error) {
            console.error('🏥 Health Check Failed:', error);
            return false;
        }
    }
}

// Global app instance
window.app = null;

// Initialize application when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.app = new App();
    
    // Add helpful console messages
    console.log(`
    🌿 Plataforma de Inteligência Ambiental - MMA
    
    Comandos úteis:
    - Ctrl/Cmd + Enter: Iniciar câmera ou autenticar
    - Escape: Parar câmera
    - F1: Ajuda
    
    Desenvolvido para o Ministério do Meio Ambiente - Brasil
    `);
});

// Export for testing
if (typeof module !== 'undefined' && module.exports) {
    module.exports = App;
}