package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.setting.SettingResponseDto;
import org.grnet.status.dtos.setting.SettingUpdateDto;
import org.grnet.status.entities.Setting;
import org.grnet.status.mappers.SettingMapper;
import org.grnet.status.repositories.SettingRepository;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SettingService {

    @Inject
    SettingRepository settingRepository;


    /**
     * Fetches all settings, decrypting any sensitive values before returning them.
     *
     * @return list of all settings (with decrypted fields when applicable)
     */
    public List<SettingResponseDto> getAllSettings() {
        var settings = settingRepository.listAll()
                .stream()
                .sorted(Comparator.comparing(Setting::getId))
                .collect(Collectors.toList());

        return SettingMapper.INSTANCE.settingsToDto(settings);
    }

     /*
     * Updates a setting by merging new data into the existing JSON structure.
     * Sensitive keys are encrypted before saving.
     *
     * @param id      The setting ID
     * @param request New data to update
     * @param userId  Who is making the update
     * @return updated SettingResponseDto
     */
    @Transactional
    public SettingResponseDto updateSetting(String id, SettingUpdateDto request, String userId) {
        var setting = settingRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Setting with id " + id + " not found."));

        Map<String, Object> existingData = new HashMap<>(setting.getData());

        // Block changes to reserved keys
        for (String reserved : List.of("id", "label", "description")) {
            if (request.data.containsKey(reserved)) {
                throw new BadRequestException("Field " + reserved + " cannot be modified.");
            }
        }

        var labelObj = existingData.get("label");
        var label = (labelObj instanceof String) ? (String) labelObj : null;

        // Validate config keys
        validateAllowedConfigKeys(request.data, existingData);


        // Merge config
        if (request.data.containsKey("config") && existingData.containsKey("config")) {
            Map<String, Object> existingConfig = (Map<String, Object>) existingData.get("config");
            Map<String, Object> requestConfig = (Map<String, Object>) request.data.get("config");

            for (Map.Entry<String, Object> entry : requestConfig.entrySet()) {
                existingConfig.put(entry.getKey(), entry.getValue());
            }

            // Prevent overwriting the entire config map
            request.data.remove("config");
        }

        // Persist changes
        setting.setData(existingData);
        setting.setUpdatedBy(userId);
        setting.setUpdatedOn(Timestamp.from(Instant.now()));
        setting.setEnabled(request.enabled);
        settingRepository.persist(setting);

        return SettingMapper.INSTANCE.settingToDto(setting);
    }


    /**
     * Retrieves a single setting by ID.
     * @param id setting ID
     * @return setting as DTO
     */
    public SettingResponseDto getSettingById(String id) {
        var setting = settingRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Setting with id " + id + " not found."));

        return SettingMapper.INSTANCE.settingToDto(setting);
    }


    /**
     * Fetches a config value by key from a specific setting.
     * Performs recursive search across the JSON data.
     * Decrypts value if it is marked sensitive.
     *
     * @param id  setting ID
     * @param key key to search for (e.g., "zenodo.api.key")
     * @return Optional decrypted value
     */
    @Transactional
    public Optional<String> getSettingConfig(String id, String key) {
        return settingRepository.findByIdOptional(id)
                .filter(Setting::isEnabled)
                .map(Setting::getData)
                .flatMap(data -> findKey(data, key))
                .filter(StringUtils::isNotBlank);
    }

    /**
     * Recursively searches for a key in a nested map and returns it.
     * Decrypts it if it's sensitive.
     *
     * @param map       the JSON data
     * @param targetKey the key to find
     * @return optional decrypted value
     */
    @SuppressWarnings("unchecked")
    private Optional<String> findKey(Map<String, Object> map, String targetKey) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equalsIgnoreCase(targetKey)) {
                if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                    String stringValue = (String) value;

                    return Optional.of(stringValue);
                }
            }

            if (value instanceof Map) {
                Optional<String> result = findKey((Map<String, Object>) value, targetKey);
                if (result.isPresent()) return result;
            }
        }

        return Optional.empty();
    }

    /**
     * Checks whether a setting is enabled.
     *
     * @param id setting ID
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String id) {
        return settingRepository.findByIdOptional(id)
                .map(Setting::isEnabled)
                .orElse(false);
    }

    @SuppressWarnings("unchecked")
    private void validateAllowedConfigKeys(Map<String, Object> requestData, Map<String, Object> existingData) {
        Object reqConfigObj = requestData.get("config");
        Object existingConfigObj = existingData.get("config");

        if (reqConfigObj != null && existingConfigObj instanceof Map && reqConfigObj instanceof Map) {
            Map<String, Object> existingConfig = (Map<String, Object>) existingConfigObj;
            Map<String, Object> requestConfig = (Map<String, Object>) reqConfigObj;


            for (String key : requestConfig.keySet()) {
                if (!existingConfig.containsKey(key)) {
                    throw new BadRequestException("Invalid config key: \"" + key + "\". Allowed keys: " + existingConfig.keySet());
                }
            }
        }
    }

    /**
     * Retrieve specifically the Performance Monitoring Setting
     * @return
     */
    public SettingResponseDto getPerformanceSetting() {
        Setting setting = settingRepository.findPerformanceSetting()
                .orElseThrow(() ->
                        new NotFoundException("Performance setting not found"));

        return SettingMapper.INSTANCE.settingToDto(setting);
    }

}

