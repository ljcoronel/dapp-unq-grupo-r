package com.dappunq.service;

import com.dappunq.persistence.ApiKeyEntity;
import com.dappunq.persistence.ApiKeyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApiKeyService {
    public static final String SCRAPING_PERMISSION = "scraping";

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, PasswordEncoder passwordEncoder) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String rotateScrapingApiKey() {
        String plainKey = generateScrapingApiKey();
        String keyHash = passwordEncoder.encode(plainKey);
        ApiKeyEntity existing = apiKeyRepository.findByPermissionForUpdate(SCRAPING_PERMISSION).orElse(null);

        if (existing == null) {
            ApiKeyEntity created = new ApiKeyEntity(SCRAPING_PERMISSION, keyHash, true, LocalDateTime.now(), LocalDateTime.now());
            apiKeyRepository.save(created);
            return plainKey;
        }

        existing.setKeyHash(keyHash);
        existing.setActive(true);
        existing.setUpdatedAt(LocalDateTime.now());
        if (existing.getCreatedAt() == null) {
            existing.setCreatedAt(LocalDateTime.now());
        }

        ApiKeyEntity saved = apiKeyRepository.save(existing);
        if (!passwordEncoder.matches(plainKey, saved.getKeyHash())) {
            throw new IllegalStateException("La API key de scraping no pudo validarse tras la rotación");
        }

        return plainKey;
    }

    @Transactional(readOnly = true)
    public boolean isValidScrapingApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        String normalized = apiKey.trim();
        if (!normalized.matches("^[A-Za-z0-9._-]{16,128}$")) {
            return false;
        }

        return apiKeyRepository.findByPermissionAndActiveTrue(SCRAPING_PERMISSION)
                .map(entity -> passwordEncoder.matches(normalized, entity.getKeyHash()))
                .orElse(false);
    }

    public String generateScrapingApiKey() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
