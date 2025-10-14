package br.gov.mma.facial.service;

import br.gov.mma.facial.dto.PendingRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage pending user registrations (before facial enrollment)
 * Stores user data temporarily in memory until facial biometric is enrolled
 */
@Service
public class PendingRegistrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PendingRegistrationService.class);
    
    // In-memory storage for pending registrations (session token -> pending data)
    // In production, use Redis or similar distributed cache
    private final Map<String, PendingRegistrationRequest> pendingRegistrations = new ConcurrentHashMap<>();
    
    /**
     * Creates a pending registration session
     * @return session token
     */
    public String createPendingRegistration(String nome, String email, String matricula, String orgao, String password) {
        String sessionToken = UUID.randomUUID().toString();
        
        PendingRegistrationRequest pending = new PendingRegistrationRequest(
            sessionToken, nome, email, matricula, orgao, password
        );
        
        pendingRegistrations.put(sessionToken, pending);
        
        logger.info("Created pending registration session: {} for email: {}", sessionToken, email);
        
        return sessionToken;
    }
    
    /**
     * Retrieves pending registration by session token
     */
    public PendingRegistrationRequest getPendingRegistration(String sessionToken) {
        PendingRegistrationRequest pending = pendingRegistrations.get(sessionToken);
        
        if (pending == null) {
            logger.warn("Pending registration not found for session token: {}", sessionToken);
            return null;
        }
        
        if (pending.isExpired()) {
            logger.warn("Pending registration expired for session token: {}", sessionToken);
            pendingRegistrations.remove(sessionToken);
            return null;
        }
        
        return pending;
    }
    
    /**
     * Completes and removes pending registration
     */
    public PendingRegistrationRequest completePendingRegistration(String sessionToken) {
        PendingRegistrationRequest pending = getPendingRegistration(sessionToken);
        
        if (pending != null) {
            pendingRegistrations.remove(sessionToken);
            logger.info("Completed and removed pending registration: {}", sessionToken);
        }
        
        return pending;
    }
    
    /**
     * Cleans up expired pending registrations
     * Should be called periodically by a scheduled task
     */
    public void cleanupExpiredRegistrations() {
        int removed = 0;
        for (Map.Entry<String, PendingRegistrationRequest> entry : pendingRegistrations.entrySet()) {
            if (entry.getValue().isExpired()) {
                pendingRegistrations.remove(entry.getKey());
                removed++;
            }
        }
        
        if (removed > 0) {
            logger.info("Cleaned up {} expired pending registrations", removed);
        }
    }
}
