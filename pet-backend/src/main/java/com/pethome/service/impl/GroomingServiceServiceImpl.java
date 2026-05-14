package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.GroomingService;
import com.pethome.mapper.GroomingServiceMapper;
import com.pethome.service.GroomingServiceService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class GroomingServiceServiceImpl extends ServiceImpl<GroomingServiceMapper, GroomingService> implements GroomingServiceService {

    @Override
    public IPage<GroomingService> getGroomingServicePage(Page<GroomingService> page, QueryWrapper<GroomingService> queryWrapper) {
        IPage<GroomingService> result = this.page(page, queryWrapper);
        if (result != null && !CollectionUtils.isEmpty(result.getRecords())) {
            result.getRecords().forEach(this::fillFromLegacyFields);
        }
        return result;
    }

    @Override
    public GroomingService createGroomingService(GroomingService groomingService) {
        prepareServiceForPersist(groomingService, true);
        this.save(groomingService);
        return groomingService;
    }

    @Override
    public GroomingService updateGroomingService(GroomingService groomingService) {
        prepareServiceForPersist(groomingService, false);
        this.updateById(groomingService);
        return groomingService;
    }

    @Override
    public boolean deleteGroomingService(Long id) {
        return this.removeById(id);
    }

    @Override
    public GroomingService getGroomingServiceById(Long id) {
        GroomingService service = this.getById(id);
        fillFromLegacyFields(service);
        return service;
    }

    private void prepareServiceForPersist(GroomingService groomingService, boolean isCreate) {
        if (groomingService == null) {
            return;
        }

        fillFromLegacyFields(groomingService);

        if (!StringUtils.hasText(groomingService.getStatus())) {
            groomingService.setStatus("active");
        }

        if (groomingService.getIsRecommended() == null) {
            groomingService.setIsRecommended(Boolean.FALSE);
        }

        if (groomingService.getSortOrder() == null) {
            groomingService.setSortOrder(0);
        }

        LocalDateTime now = LocalDateTime.now();
        if (isCreate && groomingService.getCreatedAt() == null) {
            groomingService.setCreatedAt(now);
        }
        groomingService.setUpdatedAt(now);
        if (groomingService.getIsDeleted() == null) {
            groomingService.setIsDeleted(Boolean.FALSE);
        }
    }

    private void fillFromLegacyFields(GroomingService service) {
        if (service == null) {
            return;
        }

        if (CollectionUtils.isEmpty(service.getIntroduction())) {
            service.setIntroduction(convertToList(service.getIntroduction(), service.getDescription()));
        } else {
            service.setIntroduction(cleanList(service.getIntroduction()));
        }

        if (CollectionUtils.isEmpty(service.getInstructions())) {
            service.setInstructions(new ArrayList<>());
        } else {
            service.setInstructions(cleanList(service.getInstructions()));
        }

        if (!CollectionUtils.isEmpty(service.getTags())) {
            service.setTags(cleanList(service.getTags()));
        }
    }

    private List<String> convertToList(Object value, String fallback) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            ((List<?>) value).stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(result::add);
            return result;
        }

        String source = value != null ? String.valueOf(value) : fallback;
        if (!StringUtils.hasText(source)) {
            return result;
        }

        source = source.trim();
        if (source.startsWith("[") && source.endsWith("]")) {
            source = source.substring(1, source.length() - 1);
        }

        Arrays.stream(source.split("[\\n,;；，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(result::add);
        return result;
    }

    private List<String> cleanList(List<String> source) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(source)) {
            return result;
        }
        source.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(result::add);
        return result;
    }
}
