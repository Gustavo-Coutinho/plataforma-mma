/**
 * Camera Management Module
 * Handles camera access, face detection, and image capture
 */

class CameraManager {
    constructor(elementPrefix = '') {
        this.stream = null;
        this.prefix = elementPrefix;
        this.isDetecting = false;
        this.detectionInterval = null;
        this.faceCascade = null;
        
        this.initializeElements();
    }

    initializeElements() {
        // Get DOM elements with optional prefix - these are cached but can be null initially
        this.updateElementReferences();
    }

    updateElementReferences() {
        // Update element references - useful for modal content that loads later
        // Handle the element ID naming properly based on prefix
        if (this.prefix === 'reset') {
            // For reset camera, IDs are like: resetVideoElement, resetCanvasElement
            this.video = document.getElementById('resetVideoElement');
            this.canvas = document.getElementById('resetCanvasElement');
            this.placeholder = document.getElementById('resetCameraPlaceholder');
            this.loading = document.getElementById('resetCameraLoading');
            this.statusDiv = document.getElementById('resetFaceDetectionStatus');
            this.startBtn = document.getElementById('startResetCameraBtn');
        } else {
            // For main login camera, IDs are like: videoElement, canvasElement
            this.video = document.getElementById('videoElement');
            this.canvas = document.getElementById('canvasElement');
            this.placeholder = document.getElementById('cameraPlaceholder');
            this.loading = document.getElementById('cameraLoading');
            this.statusDiv = document.getElementById('faceDetectionStatus');
            this.startBtn = document.getElementById('startCameraBtn');
        }
        this.ctx = this.canvas ? this.canvas.getContext('2d') : null;
    }

    async startCamera() {
        // Ensure we have fresh element references (important for modal content)
        this.updateElementReferences();
        
        if (!this.video || !this.canvas) {
            console.error('Camera elements not found. Prefix:', this.prefix);
            console.error('Video element ID:', this.prefix + 'videoElement', 'Found:', !!this.video);
            console.error('Canvas element ID:', this.prefix + 'canvasElement', 'Found:', !!this.canvas);
            throw new Error('Elementos de câmera não encontrados no DOM');
        }
        
        try {
            this.showLoading(true);
            this.updateStatus('Solicitando acesso à câmera...', 'info');

            // Check for getUserMedia support
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                throw new Error('Camera não suportada pelo navegador');
            }

            // Request camera access
            const constraints = {
                video: {
                    width: { ideal: 640 },
                    height: { ideal: 480 },
                    facingMode: 'user'
                },
                audio: false
            };

            this.stream = await navigator.mediaDevices.getUserMedia(constraints);
            
            if (this.video) {
                this.video.srcObject = this.stream;
                
                // Wait for video to be ready
                await new Promise((resolve) => {
                    this.video.onloadedmetadata = () => {
                        this.video.play();
                        resolve();
                    };
                });

                // Setup canvas
                if (this.canvas && this.ctx) {
                    this.canvas.width = this.video.videoWidth;
                    this.canvas.height = this.video.videoHeight;
                }

                this.showVideo();
                this.startFaceDetection();
                this.updateStatus('Câmera iniciada. Posicione seu rosto no centro.', 'success');
            }

        } catch (error) {
            console.error('Erro ao acessar câmera:', error);
            this.updateStatus('Erro ao acessar câmera: ' + error.message, 'danger');
            this.showPlaceholder();
        } finally {
            this.showLoading(false);
        }
    }

    stopCamera() {
        try {
            // Stop face detection
            this.stopFaceDetection();

            // Stop video stream
            if (this.stream) {
                this.stream.getTracks().forEach(track => track.stop());
                this.stream = null;
            }

            // Reset video element
            if (this.video) {
                this.video.srcObject = null;
            }

            this.showPlaceholder();
            this.updateStatus('Câmera desconectada.', 'info');

        } catch (error) {
            console.error('Erro ao parar câmera:', error);
            this.updateStatus('Erro ao desconectar câmera.', 'warning');
        }
    }

    startFaceDetection() {
        if (this.isDetecting) return;

        this.isDetecting = true;
        this.detectionInterval = setInterval(() => {
            this.detectFace();
        }, 500); // Check every 500ms
    }

    stopFaceDetection() {
        this.isDetecting = false;
        if (this.detectionInterval) {
            clearInterval(this.detectionInterval);
            this.detectionInterval = null;
        }
    }

    async detectFace() {
        if (!this.video || !this.canvas || !this.ctx || this.video.readyState !== 4) {
            return;
        }

        try {
            // Draw current frame to canvas
            this.ctx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);

            // Simple face detection simulation
            // In a real implementation, you would use OpenCV.js or similar
            const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
            const faceDetected = this.simulateFaceDetection(imageData);

            if (faceDetected) {
                this.updateStatus('Rosto detectado! Pronto para autenticar.', 'success');
                this.drawFaceBox(faceDetected);
            } else {
                this.updateStatus('Posicione seu rosto na câmera...', 'info');
            }

        } catch (error) {
            console.error('Erro na detecção facial:', error);
        }
    }

    simulateFaceDetection(imageData) {
        // Simplified face detection simulation
        // In production, this would use actual computer vision algorithms
        const width = imageData.width;
        const height = imageData.height;
        const data = imageData.data;

        // Simple brightness detection to simulate face presence
        let totalBrightness = 0;
        let samples = 0;

        // Sample pixels in center region
        const centerX = Math.floor(width / 2);
        const centerY = Math.floor(height / 2);
        const sampleRadius = Math.min(width, height) * 0.15;

        for (let y = centerY - sampleRadius; y < centerY + sampleRadius; y += 5) {
            for (let x = centerX - sampleRadius; x < centerX + sampleRadius; x += 5) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    const index = (y * width + x) * 4;
                    const brightness = (data[index] + data[index + 1] + data[index + 2]) / 3;
                    totalBrightness += brightness;
                    samples++;
                }
            }
        }

        const avgBrightness = totalBrightness / samples;
        
        // Simulate face detection based on brightness patterns
        if (avgBrightness > 80 && avgBrightness < 200 && samples > 100) {
            return {
                x: centerX - sampleRadius,
                y: centerY - sampleRadius,
                width: sampleRadius * 2,
                height: sampleRadius * 2,
                confidence: Math.min(avgBrightness / 150, 1.0)
            };
        }

        return null;
    }

    drawFaceBox(face) {
        if (!face || !this.ctx) return;

        // Draw face detection rectangle
        this.ctx.strokeStyle = '#20c997';
        this.ctx.lineWidth = 3;
        this.ctx.strokeRect(face.x, face.y, face.width, face.height);

        // Draw confidence indicator
        const confidence = Math.round(face.confidence * 100);
        this.ctx.fillStyle = '#20c997';
        this.ctx.font = '16px Arial';
        this.ctx.fillText(`${confidence}%`, face.x, face.y - 10);
    }

    async captureFrame() {
        if (!this.video || !this.canvas || !this.ctx) {
            throw new Error('Câmera não disponível');
        }

        try {
            // Capture current frame
            this.ctx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);
            
            // Convert to base64
            const imageData = this.canvas.toDataURL('image/jpeg', 0.8);
            
            // Remove data URL prefix
            return imageData.split(',')[1];

        } catch (error) {
            console.error('Erro ao capturar frame:', error);
            throw new Error('Falha na captura da imagem');
        }
    }

    showVideo() {
        if (this.placeholder) this.placeholder.classList.add('d-none');
        if (this.loading) this.loading.classList.add('d-none');
        if (this.video) this.video.classList.remove('d-none');
        if (this.canvas) this.canvas.classList.remove('d-none');
        
        if (this.startBtn) this.startBtn.classList.add('d-none');
    }

    showPlaceholder() {
        if (this.video) this.video.classList.add('d-none');
        if (this.canvas) this.canvas.classList.add('d-none');
        if (this.loading) this.loading.classList.add('d-none');
        if (this.placeholder) this.placeholder.classList.remove('d-none');
        
        if (this.startBtn) this.startBtn.classList.remove('d-none');
    }

    showLoading(show = true) {
        if (show) {
            if (this.placeholder) this.placeholder.classList.add('d-none');
            if (this.video) this.video.classList.add('d-none');
            if (this.loading) this.loading.classList.remove('d-none');
        } else {
            if (this.loading) this.loading.classList.add('d-none');
        }
    }

    enableAuthentication(enable = true) {
        if (this.authBtn) {
            if (enable) {
                this.authBtn.classList.remove('d-none');
                this.authBtn.disabled = false;
            } else {
                this.authBtn.classList.add('d-none');
                this.authBtn.disabled = true;
            }
        }
    }

    enableAuthentication(enable = true) {
        if (this.authBtn) {
            if (enable) {
                this.authBtn.classList.remove('d-none');
                this.authBtn.disabled = false;
            } else {
                this.authBtn.classList.add('d-none');
                this.authBtn.disabled = true;
            }
        }
    }

    updateStatus(message, type = 'info') {
        if (!this.statusDiv) return;

        this.statusDiv.className = `alert alert-${type}`;
        this.statusDiv.innerHTML = `<i class="fas fa-${this.getStatusIcon(type)} me-2"></i>${message}`;
        this.statusDiv.classList.remove('d-none');

        // Auto-hide info messages after 5 seconds
        if (type === 'info') {
            setTimeout(() => {
                if (this.statusDiv.classList.contains('alert-info')) {
                    this.statusDiv.classList.add('d-none');
                }
            }, 5000);
        }
    }

    getStatusIcon(type) {
        const icons = {
            'info': 'info-circle',
            'success': 'check-circle',
            'warning': 'exclamation-triangle',
            'danger': 'exclamation-circle'
        };
        return icons[type] || 'info-circle';
    }

    isReady() {
        return this.stream && this.video && this.video.readyState === 4;
    }

    destroy() {
        this.stopCamera();
        this.stopFaceDetection();
    }
}

// Global camera manager instances
window.cameraManager = null;
window.resetCameraManager = null;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Main login camera
    window.cameraManager = new CameraManager('');
    
    // Password reset camera (with 'reset' prefix)
    window.resetCameraManager = new CameraManager('reset');
});

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    if (window.cameraManager) {
        window.cameraManager.destroy();
    }
    if (window.resetCameraManager) {
        window.resetCameraManager.destroy();
    }
});