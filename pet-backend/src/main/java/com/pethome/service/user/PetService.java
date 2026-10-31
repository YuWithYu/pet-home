package com.pethome.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Pet;

import java.util.List;

public interface PetService {
    IPage<Pet> getPetListByUserId(Page<Pet> page, Long userId);
    
    /**
     * 根据用户ID获取宠物列表（不分页）
     * @param userId 用户ID
     * @return 宠物列表
     */
    List<Pet> getPetsByUserId(Long userId);

    /**
     * 分页查询宠物：指定 userId 时优先走缓存列表再手动分页，缓存失败降级数据库分页。
     * 兜底降级逻辑收敛在服务层，Controller 不再手写异常捕获。
     */
    IPage<Pet> getPetPageForOwner(Long userId, int pageNo, int pageSize);
    
    Pet createPet(Pet pet);
    Pet updatePet(Pet pet);
    boolean deletePet(Long id);
    Pet getPetById(Long id);
    
    /**
     * 清除指定用户的宠物列表缓存
     * @param userId 用户ID
     */
    void clearPetListCache(Long userId);
}
