package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.status.dtos.status.StatusGroupRequestDto;
import org.grnet.status.dtos.status.StatusGroupResponseDto;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.grnet.status.services.utils.EncryptUtil;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class StatusService {

    @Inject
    EncryptUtil encryptUtil;

    @Inject
    ArgoWebApiClientFactory argoWebApiClientFactory;

    @Inject
    StatusPageRepository statusPageRepository;

    public List<StatusGroupResponseDto> getStatusGroups(StatusGroupRequestDto request) {

        var decryptedSecret = encryptUtil.decrypt(request.secret);

        var client = argoWebApiClientFactory.buildClient(request.api);

        var argoResponse = client.fetchStatusGroups(decryptedSecret, request.report);

        var list = new ArrayList<StatusGroupResponseDto>();

        if (argoResponse != null && argoResponse.groups != null) {
            for (var group : argoResponse.groups) {
                var dto = new StatusGroupResponseDto();
                dto.name = group.name;

                if (group.statuses != null && !group.statuses.isEmpty()) {
                    // take the latest status
                    dto.status = group.statuses.get(group.statuses.size() - 1).value;
                }
                list.add(dto);
            }
        }

        return list;
    }


    @Transactional
    public StatusPageConfigDto getConfigBySlug(String slug) {
        var statusPage = statusPageRepository.find("slug", slug)
                .firstResultOptional()
                .orElseThrow(() -> new IllegalArgumentException("Status page not found for slug: " + slug));

        var statusPageDto = StatusPageMapper.INSTANCE.entityToDto(statusPage);

        var request = new StatusGroupRequestDto();
        request.api = statusPageDto.api;
        request.secret = statusPageDto.secret;
        request.report = statusPageDto.report;

        var allGroups = getStatusGroups(request);
        var config = StatusPageMapper.INSTANCE.map(statusPage.config);

        // Update each group in the config with the live status
        config.groups.forEach(group -> group.list.forEach(item -> {
            allGroups.stream()
                    .filter(live -> live.name.equals(item.name))
                    .findFirst()
                    .ifPresent(live -> item.status = live.status);
        }));

        return config;
    }

}