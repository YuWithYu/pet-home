package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Address;

import java.util.List;

public interface AddressService {
    IPage<Address> getAddressListByUserId(Page<Address> page, Long userId);
    Address createAddress(Address address);
    Address updateAddress(Address address);
    boolean deleteAddress(Long id);
    Address getAddressById(Long id);
    Address setDefaultAddress(Long userId, Long addressId);
    List<Address> getUserAddresses(Long userId);
    Address getDefaultAddress(Long userId);
}


