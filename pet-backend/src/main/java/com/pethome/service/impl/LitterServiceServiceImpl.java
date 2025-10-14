package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterService;
import com.pethome.mapper.LitterServiceMapper;
import com.pethome.service.LitterServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LitterServiceServiceImpl implements LitterServiceService {

    @Autowired
    private LitterServiceMapper litterServiceMapper;

    @Override
    public IPage<LitterService> getLitterServiceList(Page<LitterService> page) {
        return litterServiceMapper.selectPage(page, null);
    }

    @Override
    public LitterService createLitterService(LitterService litterService) {
        litterServiceMapper.insert(litterService);
        return litterService;
    }

    @Override
    public LitterService updateLitterService(LitterService litterService) {
        litterServiceMapper.updateById(litterService);
        return litterService;
    }

    @Override
    public boolean deleteLitterService(Long id) {
        return litterServiceMapper.deleteById(id) > 0;
    }

    @Override
    public LitterService getLitterServiceById(Long id) {
        return litterServiceMapper.selectById(id);
    }
}
