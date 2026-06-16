package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Address;
import com.pethome.mapper.AddressMapper;
import com.pethome.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public IPage<Address> getAddressListByUserId(Page<Address> page, Long userId) {
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return addressMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Address createAddress(Address address) {
        try {
            System.out.println("[AddressServiceImpl] createAddress - 开始创建地址");
            System.out.println("[AddressServiceImpl] createAddress - userId: " + address.getUserId());
            System.out.println("[AddressServiceImpl] createAddress - contactName: " + address.getContactName());
            System.out.println("[AddressServiceImpl] createAddress - name: " + address.getName());
            System.out.println("[AddressServiceImpl] createAddress - contactPhone: " + address.getContactPhone());
            System.out.println("[AddressServiceImpl] createAddress - phone: " + address.getPhone());
            System.out.println("[AddressServiceImpl] createAddress - isDefault: " + address.getIsDefault());
            
            // 从前端数据设置到数据库字段
            address.setFromFrontendData();
            System.out.println("[AddressServiceImpl] createAddress - 转换后 contactName: " + address.getContactName() + ", contactPhone: " + address.getContactPhone());
            
            // 如果新地址要设为默认，先清除该用户其他地址的默认状态
            if (address.getIsDefault() != null && address.getIsDefault()) {
                System.out.println("[AddressServiceImpl] createAddress - 新地址要设为默认，先清除其他地址的默认状态");
                Address updateAddress = new Address();
                updateAddress.setIsDefault(false);
                QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", address.getUserId());
                addressMapper.update(updateAddress, queryWrapper);
            }
            
            if (address.getCreateTime() == null) {
                address.setCreateTime(java.time.LocalDateTime.now());
            }
            if (address.getUpdateTime() == null) {
                address.setUpdateTime(java.time.LocalDateTime.now());
            }
            
            System.out.println("[AddressServiceImpl] createAddress - 准备插入数据库");
            int result = addressMapper.insert(address);
            System.out.println("[AddressServiceImpl] createAddress - 插入结果: " + result);
            System.out.println("[AddressServiceImpl] createAddress - 插入后的地址ID: " + address.getId());
            
            // 初始化非数据库字段
            address.initDisplayFields();
            System.out.println("[AddressServiceImpl] createAddress - 创建成功，地址ID: " + address.getId());
            return address;
        } catch (Exception e) {
            System.out.println("[AddressServiceImpl] createAddress - 异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Address updateAddress(Address address) {
        try {
            System.out.println("[AddressServiceImpl] updateAddress - 开始更新地址，地址ID: " + address.getId());
            System.out.println("[AddressServiceImpl] updateAddress - isDefault: " + address.getIsDefault());
            
            // 从前端数据设置到数据库字段
            address.setFromFrontendData();
            
            // 如果更新后的地址要设为默认，先清除该用户其他地址的默认状态
            if (address.getIsDefault() != null && address.getIsDefault()) {
                System.out.println("[AddressServiceImpl] updateAddress - 地址要设为默认，先清除其他地址的默认状态");
                Address updateAddress = new Address();
                updateAddress.setIsDefault(false);
                QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", address.getUserId());
                queryWrapper.ne("id", address.getId()); // 排除当前地址
                addressMapper.update(updateAddress, queryWrapper);
            }
            
            address.setUpdateTime(java.time.LocalDateTime.now());
            addressMapper.updateById(address);
            
            // 初始化非数据库字段
            address.initDisplayFields();
            System.out.println("[AddressServiceImpl] updateAddress - 更新成功");
            return address;
        } catch (Exception e) {
            System.out.println("[AddressServiceImpl] updateAddress - 异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
    public Address setDefaultAddress(Long userId, Long addressId) {
        // 先将该用户的所有地址设为非默认
        Address updateAddress = new Address();
        updateAddress.setIsDefault(false);
        QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        addressMapper.update(updateAddress, queryWrapper);

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
        
        // 修复：如果发现有多个默认地址，只保留最新的一个为默认
        List<Address> defaultAddresses = new java.util.ArrayList<>();
        for (Address address : addresses) {
            if (address.getIsDefault() != null && address.getIsDefault()) {
                defaultAddresses.add(address);
            }
        }
        
        // 如果有多个默认地址，只保留最新的一个
        if (defaultAddresses.size() > 1) {
            System.out.println("[AddressServiceImpl] getUserAddresses - 发现 " + defaultAddresses.size() + " 个默认地址，修复中...");
            // 按创建时间排序，保留最新的
            defaultAddresses.sort((a, b) -> {
                if (a.getCreateTime() == null) return 1;
                if (b.getCreateTime() == null) return -1;
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
            
            // 将除了最新的之外的所有默认地址设为非默认
            for (int i = 1; i < defaultAddresses.size(); i++) {
                Address addr = defaultAddresses.get(i);
                addr.setIsDefault(false);
                addressMapper.updateById(addr);
                System.out.println("[AddressServiceImpl] getUserAddresses - 已将地址ID " + addr.getId() + " 设为非默认");
            }
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


