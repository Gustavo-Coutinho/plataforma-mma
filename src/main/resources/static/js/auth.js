/**
 * Authentication Module
 * Handles biometric and traditional authentication
 */

class AuthManager {
    constructor() {
        this.apiBase = '/api/auth';
        this.currentToken = localStorage.getItem('mma_auth_token');
        this.currentUser = null;
        
        this.initializeElements();
        this.checkAuthStatus();
    }

    initializeElements() {
        // Get form elements
        this.loginForm = document.getElementById('loginForm');
        this.emailOrMatriculaInput = document.getElementById('emailOrMatricula');
        this.passwordInput = document.getElementById('password');
        this.startCameraBtn = document.getElementById('startCameraBtn');
        this.loginBtn = document.getElementById('loginBtn');
        this.livenessCheck = document.getElementById('livenessCheck');
        this.forgotPasswordBtn = document.getElementById('forgotPasswordBtn');
        
        // Password reset elements
        this.passwordResetModal = document.getElementById('passwordResetModal');
        this.resetEmailOrMatriculaInput = document.getElementById('resetEmailOrMatricula');
        this.newPasswordInput = document.getElementById('newPassword');
        this.confirmPasswordInput = document.getElementById('confirmPassword');
        this.startResetCameraBtn = document.getElementById('startResetCameraBtn');
        this.verifyResetBtn = document.getElementById('verifyResetBtn');
        this.confirmResetBtn = document.getElementById('confirmResetBtn');
        this.passwordFieldsContainer = document.getElementById('passwordFieldsContainer');
        this.identityVerified = false; // Track if user identity has been verified

        // Bind events
        if (this.loginForm) {
            this.loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Enable camera button when fields are filled
        if (this.emailOrMatriculaInput && this.passwordInput) {
            this.emailOrMatriculaInput.addEventListener('input', () => this.checkFormFields());
            this.passwordInput.addEventListener('input', () => this.checkFormFields());
        }

        if (this.startCameraBtn) {
            this.startCameraBtn.addEventListener('click', () => this.handleStartCamera());
        }

        if (this.forgotPasswordBtn) {
            this.forgotPasswordBtn.addEventListener('click', () => this.showPasswordResetModal());
        }

        // Enable reset camera button when reset email is filled
        if (this.resetEmailOrMatriculaInput) {
            this.resetEmailOrMatriculaInput.addEventListener('input', () => this.checkResetFormFields());
        }

        if (this.startResetCameraBtn) {
            this.startResetCameraBtn.addEventListener('click', () => this.handleStartResetCamera());
        }

        if (this.verifyResetBtn) {
            this.verifyResetBtn.addEventListener('click', () => this.handleVerifyIdentity());
        }

        if (this.confirmResetBtn) {
            this.confirmResetBtn.addEventListener('click', () => this.handlePasswordReset());
        }
    }

    checkFormFields() {
        const emailFilled = this.emailOrMatriculaInput && this.emailOrMatriculaInput.value.trim() !== '';
        const passwordFilled = this.passwordInput && this.passwordInput.value.trim() !== '';
        
        if (this.startCameraBtn) {
            this.startCameraBtn.disabled = !(emailFilled && passwordFilled);
            
            const placeholder = document.getElementById('cameraPlaceholder');
            if (placeholder) {
                const message = placeholder.querySelector('p');
                if (message) {
                    if (emailFilled && passwordFilled) {
                        message.textContent = 'Clique para iniciar a câmera';
                    } else {
                        message.textContent = 'Preencha os campos para habilitar a câmera';
                    }
                }
            }
        }
    }

    checkResetFormFields() {
        const emailFilled = this.resetEmailOrMatriculaInput && this.resetEmailOrMatriculaInput.value.trim() !== '';
        
        if (this.startResetCameraBtn) {
            this.startResetCameraBtn.disabled = !emailFilled;
            
            const placeholder = document.getElementById('resetCameraPlaceholder');
            if (placeholder) {
                const message = placeholder.querySelector('p');
                if (message) {
                    if (emailFilled) {
                        message.textContent = 'Clique para iniciar a câmera';
                    } else {
                        message.textContent = 'Preencha o email para habilitar a câmera';
                    }
                }
            }
        }
    }

    async handleStartCamera() {
        if (!window.cameraManager) {
            this.showMessage('Sistema de câmera não disponível', 'danger');
            return;
        }

        try {
            await window.cameraManager.startCamera();
            this.startCameraBtn.classList.add('d-none');
        } catch (error) {
            console.error('Erro ao iniciar câmera:', error);
            this.showMessage('Não foi possível iniciar a câmera: ' + error.message, 'danger');
        }
    }

    async handleStartResetCamera() {
        // Stop login camera if it's running
        if (window.cameraManager && window.cameraManager.isReady()) {
            console.log('Stopping login camera before starting reset camera');
            window.cameraManager.stopCamera();
        }

        if (!window.resetCameraManager) {
            this.showMessage('Sistema de câmera não disponível', 'danger');
            return;
        }

        try {
            await window.resetCameraManager.startCamera();
            this.startResetCameraBtn.classList.add('d-none');
            this.verifyResetBtn.classList.remove('d-none');
        } catch (error) {
            console.error('Erro ao iniciar câmera:', error);
            this.showMessage('Não foi possível iniciar a câmera: ' + error.message, 'danger');
        }
    }

    async handleLogin(event) {
        event.preventDefault();

        const emailOrMatricula = this.emailOrMatriculaInput.value.trim();
        const password = this.passwordInput.value.trim();

        if (!emailOrMatricula || !password) {
            this.showMessage('Por favor, preencha todos os campos.', 'warning');
            return;
        }


        // Enforce biometric-only authentication: credentials + facial biometric are mandatory
        const cameraAvailable = !!window.cameraManager;
        const hasBiometric = cameraAvailable && window.cameraManager.isReady();

        try {
            this.showLoading('Autenticando...');

            // If camera isn't available at all, advise user to enable it
            if (!cameraAvailable) {
                this.showMessage('Autenticação facial obrigatória — sistema de câmera não disponível. Contate o administrador.', 'warning');
                return;
            }

            // If camera exists but isn't started / ready, require the user to start it first
            if (!hasBiometric) {
                this.showMessage('Autenticação facial obrigatória — inicie a câmera e capture sua face antes de entrar.', 'warning');
                return;
            }

            // Proceed only with biometric login (credentials + face)
            await this.handleBiometricLogin(emailOrMatricula, password);

        } catch (error) {
            console.error('Erro no login:', error);
            this.showMessage(this.getErrorMessage(error), 'danger');
        } finally {
            this.hideLoading();
        }
    }

    async handleTraditionalLogin(emailOrMatricula, password) {
        const response = await this.makeRequest('POST', '/login', {
            emailOrMatricula: emailOrMatricula,
            password: password
        });

        if (response.success !== false) {
            this.handleAuthSuccess(response);
        } else {
            this.showMessage(response.message || 'Credenciais inválidas', 'danger');
        }
    }

    async handleBiometricLogin(emailOrMatricula, password) {
        if (!window.cameraManager || !window.cameraManager.isReady()) {
            this.showMessage('Câmera não disponível. Fazendo login tradicional...', 'info');
            return await this.handleTraditionalLogin(emailOrMatricula, password);
        }

        this.showLoading('Capturando imagem facial...');

        // Capture frame from camera
        const imageBase64 = await window.cameraManager.captureFrame();
        
        if (!imageBase64) {
            throw new Error('Falha na captura da imagem');
        }

        this.showLoading('Processando autenticação com biometria...');

        const enableLiveness = this.livenessCheck ? this.livenessCheck.checked : true;
        const sessionId = this.generateSessionId();

        const response = await this.makeRequest('POST', '/login-face', {
            emailOrMatricula: emailOrMatricula,
            password: password,
            faceImageBase64: imageBase64,
            sessionId: sessionId,
            enableLivenessCheck: enableLiveness,
            metadata: {
                timestamp: new Date().toISOString(),
                userAgent: navigator.userAgent,
                resolution: `${window.screen.width}x${window.screen.height}`
            }
        });

        if (response.success !== false) {
            this.handleAuthSuccess(response);
        } else {
            this.showMessage(response.message || 'Falha na autenticação biométrica', 'danger');
        }
    }

    showPasswordResetModal() {
        if (this.passwordResetModal) {
            const modal = new bootstrap.Modal(this.passwordResetModal);
            modal.show();
            
            // Add event listener for when modal is closed
            this.passwordResetModal.addEventListener('hidden.bs.modal', () => {
                // Reset entire form when modal closes
                this.resetPasswordResetForm();
            }, { once: true });
            
            // Ensure event listeners are attached after modal is shown
            setTimeout(() => {
                if (!this.resetEmailOrMatriculaInput) {
                    this.resetEmailOrMatriculaInput = document.getElementById('resetEmailOrMatricula');
                }
                if (!this.startResetCameraBtn) {
                    this.startResetCameraBtn = document.getElementById('startResetCameraBtn');
                }
                if (!this.verifyResetBtn) {
                    this.verifyResetBtn = document.getElementById('verifyResetBtn');
                }
                
                // Re-attach event listener to ensure it's working
                if (this.resetEmailOrMatriculaInput) {
                    // Remove old listener if exists and add fresh one
                    const newInput = this.resetEmailOrMatriculaInput.cloneNode(true);
                    this.resetEmailOrMatriculaInput.parentNode.replaceChild(newInput, this.resetEmailOrMatriculaInput);
                    this.resetEmailOrMatriculaInput = newInput;
                    this.resetEmailOrMatriculaInput.addEventListener('input', () => this.checkResetFormFields());
                }
                
                if (this.startResetCameraBtn) {
                    const newBtn = this.startResetCameraBtn.cloneNode(true);
                    this.startResetCameraBtn.parentNode.replaceChild(newBtn, this.startResetCameraBtn);
                    this.startResetCameraBtn = newBtn;
                    this.startResetCameraBtn.addEventListener('click', () => this.handleStartResetCamera());
                }
                
                if (this.verifyResetBtn) {
                    const newVerifyBtn = this.verifyResetBtn.cloneNode(true);
                    this.verifyResetBtn.parentNode.replaceChild(newVerifyBtn, this.verifyResetBtn);
                    this.verifyResetBtn = newVerifyBtn;
                    this.verifyResetBtn.addEventListener('click', () => this.handleVerifyIdentity());
                }

                if (this.confirmResetBtn) {
                    const newConfirmBtn = this.confirmResetBtn.cloneNode(true);
                    this.confirmResetBtn.parentNode.replaceChild(newConfirmBtn, this.confirmResetBtn);
                    this.confirmResetBtn = newConfirmBtn;
                    this.confirmResetBtn.addEventListener('click', () => this.handlePasswordReset());
                }
                
                // Initial check
                this.checkResetFormFields();
            }, 100);
        }
    }

    async handleVerifyIdentity() {
        const emailOrMatricula = this.resetEmailOrMatriculaInput.value.trim();

        if (!emailOrMatricula) {
            this.showMessage('Por favor, forneça seu email ou matrícula.', 'warning');
            return;
        }

        if (!window.resetCameraManager || !window.resetCameraManager.isReady()) {
            this.showMessage('Por favor, inicie a câmera e capture sua face.', 'warning');
            return;
        }

        try {
            this.showLoading('Verificando identidade...');

            // Capture frame from reset camera
            const imageBase64 = await window.resetCameraManager.captureFrame();
            
            if (!imageBase64) {
                throw new Error('Falha na captura da imagem');
            }

            this.showLoading('Validando identidade facial...');

            // Call biometric verification endpoint
            const response = await this.makeRequest('POST', '/login-face', {
                emailOrMatricula: emailOrMatricula,
                faceImageBase64: imageBase64,
                sessionId: this.generateSessionId()
            });

            if (response.success !== false && response.userId) {
                this.identityVerified = true;
                this.verifiedUserId = response.userId;
                
                // Hide camera and verification button
                if (window.resetCameraManager) {
                    window.resetCameraManager.stopCamera();
                }
                const resetCameraView = document.getElementById('resetCameraView');
                const resetCameraPlaceholder = document.getElementById('resetCameraPlaceholder');
                if (resetCameraView) resetCameraView.style.display = 'none';
                if (resetCameraPlaceholder) resetCameraPlaceholder.style.display = 'none';
                
                this.verifyResetBtn.classList.add('d-none');
                this.startResetCameraBtn.classList.add('d-none');
                
                // Show password fields
                if (this.passwordFieldsContainer) {
                    this.passwordFieldsContainer.classList.remove('d-none');
                }
                
                // Show confirm button
                if (this.confirmResetBtn) {
                    this.confirmResetBtn.classList.remove('d-none');
                }
                
                // Disable email field
                if (this.resetEmailOrMatriculaInput) {
                    this.resetEmailOrMatriculaInput.disabled = true;
                }
                
                this.showMessage('Identidade verificada! Agora escolha sua nova senha.', 'success');
            } else {
                this.showMessage('Falha na verificação de identidade. Verifique seus dados e tente novamente.', 'danger');
            }

        } catch (error) {
            console.error('Erro na verificação de identidade:', error);
            this.showMessage(this.getErrorMessage(error), 'danger');
        } finally {
            this.hideLoading();
        }
    }

    async handlePasswordReset() {
        if (!this.identityVerified) {
            this.showMessage('Por favor, verifique sua identidade primeiro.', 'warning');
            return;
        }

        const emailOrMatricula = this.resetEmailOrMatriculaInput.value.trim();
        const newPassword = this.newPasswordInput.value.trim();
        const confirmPassword = this.confirmPasswordInput.value.trim();

        if (!newPassword || !confirmPassword) {
            this.showMessage('Por favor, preencha todos os campos de senha.', 'warning');
            return;
        }

        if (newPassword !== confirmPassword) {
            this.showMessage('As senhas não conferem. Por favor, digite novamente.', 'warning');
            this.newPasswordInput.value = '';
            this.confirmPasswordInput.value = '';
            this.newPasswordInput.focus();
            return;
        }

        if (newPassword.length < 6) {
            this.showMessage('A senha deve ter no mínimo 6 caracteres.', 'warning');
            return;
        }

        try {
            this.showLoading('Atualizando senha...');

            const response = await this.makeRequest('POST', '/reset-password', {
                emailOrMatricula: emailOrMatricula,
                password: newPassword,
                sessionId: this.generateSessionId(),
                metadata: {
                    userId: this.verifiedUserId
                }
            });

            if (response.success !== false) {
                this.showMessage('Senha alterada com sucesso! Você já pode fazer login com a nova senha.', 'success');
                
                // Close modal
                const modalInstance = bootstrap.Modal.getInstance(this.passwordResetModal);
                if (modalInstance) {
                    modalInstance.hide();
                }
                
                // Reset form and state
                this.resetPasswordResetForm();
            } else {
                this.showMessage(response.message || 'Falha ao atualizar senha', 'danger');
            }

        } catch (error) {
            console.error('Erro ao atualizar senha:', error);
            this.showMessage(this.getErrorMessage(error), 'danger');
        } finally {
            this.hideLoading();
        }
    }

    resetPasswordResetForm() {
        this.resetEmailOrMatriculaInput.value = '';
        this.newPasswordInput.value = '';
        this.confirmPasswordInput.value = '';
        this.resetEmailOrMatriculaInput.disabled = false;
        this.identityVerified = false;
        this.verifiedUserId = null;
        
        if (this.passwordFieldsContainer) {
            this.passwordFieldsContainer.classList.add('d-none');
        }
        
        if (this.confirmResetBtn) {
            this.confirmResetBtn.classList.add('d-none');
        }
        
        if (this.verifyResetBtn) {
            this.verifyResetBtn.classList.remove('d-none');
        }
        
        if (this.startResetCameraBtn) {
            this.startResetCameraBtn.classList.remove('d-none');
        }
        
        const resetCameraPlaceholder = document.getElementById('resetCameraPlaceholder');
        if (resetCameraPlaceholder) resetCameraPlaceholder.style.display = 'block';
        
        if (window.resetCameraManager) {
            window.resetCameraManager.stopCamera();
        }
    }

    handleAuthSuccess(response) {
        // Store authentication data
        if (response.token) {
            localStorage.setItem('mma_auth_token', response.token);
            this.currentToken = response.token;
        }

        if (response.userId) {
            this.currentUser = {
                id: response.userId,
                nome: response.nome,
                email: response.email,
                matricula: response.matricula,
                orgao: response.orgao,
                roles: response.roles || []
            };
            localStorage.setItem('mma_user_data', JSON.stringify(this.currentUser));
        }

        this.showMessage('Autenticação realizada com sucesso! Redirecionando...', 'success');

        // Redirect to dashboard or appropriate page
        setTimeout(() => {
            window.location.href = '/dashboard';
        }, 1500);
    }

    async refreshToken() {
        if (!this.currentToken) {
            return false;
        }

        try {
            const response = await this.makeRequest('POST', '/refresh', {}, {
                'Authorization': `Bearer ${this.currentToken}`
            });

            if (response.token) {
                localStorage.setItem('mma_auth_token', response.token);
                this.currentToken = response.token;
                return true;
            }

        } catch (error) {
            console.error('Erro ao renovar token:', error);
            this.logout();
        }

        return false;
    }

    logout() {
        // Clear stored data
        localStorage.removeItem('mma_auth_token');
        localStorage.removeItem('mma_user_data');
        this.currentToken = null;
        this.currentUser = null;

        // Redirect to login
        window.location.href = '/';
    }

    checkAuthStatus() {
        const userData = localStorage.getItem('mma_user_data');
        
        if (this.currentToken && userData) {
            try {
                this.currentUser = JSON.parse(userData);
                
                // If already authenticated and on login page, redirect to dashboard
                // Avoid redirect loop - only redirect from root path, not dashboard
                if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
                    console.log('User already authenticated, redirecting to dashboard...');
                    window.location.href = '/dashboard';
                }
            } catch (error) {
                console.error('Erro ao carregar dados do usuário:', error);
                this.logout();
            }
        } else if (window.location.pathname.includes('/dashboard')) {
            // If on dashboard without auth, redirect to login
            console.log('No authentication found, redirecting to login...');
            window.location.href = '/';
        }
    }

    async makeRequest(method, endpoint, data = null, headers = {}) {
        const url = this.apiBase + endpoint;
        
        const config = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        };

        if (data) {
            config.body = JSON.stringify(data);
        }

        const response = await fetch(url, config);
        
        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('Credenciais inválidas');
            } else if (response.status === 403) {
                throw new Error('Acesso negado');
            } else if (response.status === 429) {
                throw new Error('Muitas tentativas. Tente novamente em alguns minutos.');
            } else if (response.status >= 500) {
                throw new Error('Erro interno do servidor');
            } else {
                throw new Error(`Erro HTTP ${response.status}`);
            }
        }

        const result = await response.json();
        return result;
    }

    showMessage(message, type = 'info') {
        const container = document.getElementById('statusMessages');
        if (!container) return;

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show status-message`;
        alertDiv.innerHTML = `
            <i class="fas fa-${this.getMessageIcon(type)} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        container.appendChild(alertDiv);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }

    showLoading(message = 'Processando...') {
        // Remove existing loading overlay
        this.hideLoading();

        const overlay = document.createElement('div');
        overlay.id = 'loadingOverlay';
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            <div class="loading-content">
                <div class="spinner-border text-primary mb-3" role="status">
                    <span class="visually-hidden">Carregando...</span>
                </div>
                <p class="mb-0">${message}</p>
            </div>
        `;

        document.body.appendChild(overlay);
    }

    hideLoading() {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.remove();
        }
    }

    getMessageIcon(type) {
        const icons = {
            'info': 'info-circle',
            'success': 'check-circle',
            'warning': 'exclamation-triangle',
            'danger': 'exclamation-circle'
        };
        return icons[type] || 'info-circle';
    }

    getErrorMessage(error) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            return 'Erro de conexão. Verifique sua internet.';
        }
        return error.message || 'Erro desconhecido';
    }

    generateSessionId() {
        return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    isAuthenticated() {
        return !!this.currentToken && !!this.currentUser;
    }

    getCurrentUser() {
        return this.currentUser;
    }

    hasRole(role) {
        return this.currentUser && this.currentUser.roles && this.currentUser.roles.includes(role);
    }
}

// Global auth manager instance
window.authManager = null;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    window.authManager = new AuthManager();
});