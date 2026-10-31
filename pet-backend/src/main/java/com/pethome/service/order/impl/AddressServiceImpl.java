package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Address;
import com.pethome.mapper.AddressMapper;
import com.pethome.service.order.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public IPage<Address> getAddressListByUserId(Page<Address> page, Long userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return addressMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address createAddress(Address address) {
        // 从前端数据设置到数据库字段
        address.setFromFrontendData();

        // 如果新地址要设为默认，先清除该用户其他地址的默认状态（用 UpdateWrapper 走单条 UPDATE，避免 N+1）
        if (address.getIsDefault() != null && address.getIsDefault()) {
            clearOtherDefaults(address.getUserId(), null);
        }

        if (address.getCreateTime() == null) {
            address.setCreateTime(java.time.LocalDateTime.now());
        }
        if (address.getUpdateTime() == null) {
            address.setUpdateTime(java.time.LocalDateTime.now());
        }

        addressMapper.insert(address);
        address.initDisplayFields();
        log.debug("地址创建成功: userId={}, addressId={}", address.getUserId(), address.getId());
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address updateAddress(Address address) {
        // 从前端数据设置到数据库字段
        address.setFromFrontendData();

        // 如果更新后的地址要设为默认，先清除该用户其他地址的默认状态
        if (address.getIsDefault() != null && address.getIsDefault()) {
            clearOtherDefaults(address.getUserId(), address.getId());
        }

        address.setUpdateTime(java.time.LocalDateTime.now());
        addressMapper.updateById(address);

        // 初始化非数据库字段
        address.initDisplayFields();
        log.debug("地址更新成功: addressId={}", address.getId());
        return address;
    }

    /**
     * 清除指定用户其他地址的默认状态（单条 UPDATE，避免循环 N+1 写入）
     * @param userId 用户 ID
     * @param excludeId 需要排除的地址 ID（当前正在操作的地址），可为 null
     */
    private void clearOtherDefaults(Long userId, Long excludeId) {
        UpdateWrapper<Address> uw = new UpdateWrapper<>();
        uw.eq("user_id", userId).eq("is_default", true).set("is_default", false);
        if (excludeId != null) {
            uw.ne("id", excludeId);
        }
        addressMapper.update(null, uw);
    }

    @Override
    public boolean deleteAddress(Long id) {
        return addressMapper.deleteById(id) > 0;
    }

    @Override
    public Address getAddressById(Long id) {
        Address address = addressMapper.selectById(id);
        if (address != null) {
            address.initDisplayFields();
        }
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address setDefaultAddress(Long userId, Long addressId) {
        // 先将该用户的所有地址设为非默认（单条 UPDATE，避免循环 N+1 写入）
        clearOtherDefaults(userId, null);

        // 再将指定地址设为默认
        Address address = addressMapper.selectById(addressId);
        if (address != null) {
            address.setIsDefault(true);
            addressMapper.updateById(address);
        }
        return address;
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("is_default");
        queryWrapper.orderByDesc("create_time");
        List<Address> addresses = addressMapper.selectList(queryWrapper);

        // 数据自愈：如果发现有多个默认地址，只保留最新的一个为默认
        // 性能修复：原实现 for 循环里逐条 updateById 构成 N+1 写入，改为收集 ID 后批量 UPDATE
        List<Long> idsToUnsetDefault = new ArrayList<>();
        Address keepDefault = null;
        for (Address address : addresses) {
            if (address.getIsDefault() != null && address.getIsDefault()) {
                if (keepDefault == null) {
                    keepDefault = address; // 第一个（按 createTime desc）保留为默认
                } else {
                    // 与 keepDefault 比较，保留 createTime 更新的那个
                    if (address.getCreateTime() != null
                            && (keepDefault.getCreateTime() == null
                                || address.getCreateTime().isAfter(keepDefault.getCreateTime()))) {
                        idsToUnsetDefault.add(keepDefault.getId());
                        keepDefault = address;
                    } else {
                        idsToUnsetDefault.add(address.getId());
                    }
                }
            }
        }
        if (!idsToUnsetDefault.isEmpty()) {
            UpdateWrapper<Address> uw = new UpdateWrapper<>();
            uw.in("id", idsToUnsetDefault).set("is_default", false);
            addressMapper.update(null, uw);
            // 同步内存对象状态
            for (Address address : addresses) {
                if (idsToUnsetDefault.contains(address.getId())) {
                    address.setIsDefault(false);
                }
            }
            log.info("用户 {} 检测到多个默认地址，已批量修复 {} 条", userId, idsToUnsetDefault.size());
        }

        // 初始化非数据库字段
        for (Address address : addresses) {
            address.initDisplayFields();
        }
        return addresses;
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_default", true);
        Address address = addressMapper.selectOne(queryWrapper);
        if (address != null) {
            address.initDisplayFields();
        }
        return address;
    }
}


