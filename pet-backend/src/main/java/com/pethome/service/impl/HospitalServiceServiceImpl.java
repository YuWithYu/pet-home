package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.HospitalService;
import com.pethome.mapper.HospitalServiceMapper;
import com.pethome.service.HospitalServiceService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class HospitalServiceServiceImpl extends ServiceImpl<HospitalServiceMapper, HospitalService> implements HospitalServiceService {

    @Override
    public IPage<HospitalService> getHospitalServicePage(Page<HospitalService> page, QueryWrapper<HospitalService> queryWrapper) {
        return this.page(page, queryWrapper);
    }

    @Override
    public HospitalService createHospitalService(HospitalService hospitalService) {
        prepareServiceForPersist(hospitalService, true);
        this.save(hospitalService);
        return hospitalService;
    }

    @Override
    public HospitalService updateHospitalService(HospitalService hospitalService) {
        prepareServiceForPersist(hospitalService, false);
        this.updateById(hospitalService);
        return hospitalService;
    }

    @Override
    public boolean deleteHospitalService(Long id) {
        return this.removeById(id);
    }

    @Override
    public HospitalService getHospitalServiceById(Long id) {
        HospitalService service = this.getById(id);
        if (service != null) {
            fillFromLegacyFields(service);
        }
        return service;
    }

    private void prepareServiceForPersist(HospitalService hospitalService, boolean isCreate) {
        if (hospitalService == null) {
            return;
        }

        fillFromLegacyFields(hospitalService);

        if (!StringUtils.hasText(hospitalService.getStatus())) {
            hospitalService.setStatus("active");
        }

        if (hospitalService.getIsRecommended() == null) {
            hospitalService.setIsRecommended(Boolean.FALSE);
        }

        if (hospitalService.getSortOrder() == null) {
            hospitalService.setSortOrder(0);
        }

        LocalDateTime now = LocalDateTime.now();
        if (isCreate && hospitalService.getCreatedAt() == null) {
            hospitalService.setCreatedAt(now);
        }
        hospitalService.setUpdatedAt(now);
    }

    private void fillFromLegacyFields(HospitalService hospitalService) {
        if (hospitalService == null) {
            return;
        }

        if (CollectionUtils.isEmpty(hospitalService.getIntroduction())) {
            hospitalService.setIntroduction(convertStringToList(hospitalService.getProductIntroduction(), hospitalService.getDescription()));
        }

        if (CollectionUtils.isEmpty(hospitalService.getInstructions())) {
            List<String> combined = new ArrayList<>();
            combined.addAll(convertStringToList(hospitalService.getUsageInstructions(), null));
            combined.addAll(convertStringToList(hospitalService.getPrecautions(), null));
            combined.addAll(convertStringToList(hospitalService.getBookingRequirements(), null));
            combined.removeIf(item -> !StringUtils.hasText(item));
            if (!combined.isEmpty()) {
                hospitalService.setInstructions(combined);
            }
        }
    }

    private List<String> convertStringToList(String value, String fallback) {
        String source = StringUtils.hasText(value) ? value : fallback;
        if (!StringUtils.hasText(source)) {
            return new ArrayList<>();
        }
        // 优先尝试按JSON数组解析
        source = source.trim();
        if (source.startsWith("[") && source.endsWith("]")) {
            try {
                source = source.substring(1, source.length() - 1);
            } catch (Exception ignored) {
            }
        }

        String[] parts = source.split("[\\n,;；]");
        List<String> result = new ArrayList<>();
        Arrays.stream(parts)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(result::add);
        return result;
    }
}
