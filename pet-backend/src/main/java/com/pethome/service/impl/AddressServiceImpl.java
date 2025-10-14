package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Address;
import com.pethome.mapper.AddressMapper;
import com.pethome.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        addressMapper.insert(address);
        return address;
    }

    @Override
    public Address updateAddress(Address address) {
        addressMapper.updateById(address);
        return address;
    }

    @Override
    public boolean deleteAddress(Long id) {
        return addressMapper.deleteById(id) > 0;
    }

    @Override
    public Address getAddressById(Long id) {
        return addressMapper.selectById(id);
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
}


