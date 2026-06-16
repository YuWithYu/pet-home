package com.pethome.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Pet;
import com.pethome.mapper.PetMapper;
import com.pethome.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PetServiceImpl implements PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetServiceImpl.class);
    
    // Redis 缓存 key 前缀
    private static final String CACHE_KEY_PREFIX = "pet:list:";
    // 缓存过期时间（分钟）
    private static final long CACHE_EXPIRE_MINUTES = 10;

    @Autowired
    private PetMapper petMapper;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<Pet> getPetListByUserId(Page<Pet> page, Long userId) {
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        // 不添加状态过滤，查询所有状态的宠物（包括active、inactive等）
        // 如果只想查询active的，可以取消下面的注释
        // queryWrapper.eq("status", "active");
        
        // 如果提供了 userId，则按 userId 查询；否则查询所有宠物
        if (userId != null) {
            // 将Long转换为Integer（数据库user_id字段是int类型）
            queryWrapper.eq("user_id", userId.intValue());
            logger.info("查询条件: user_id = {} (转换为int: {})", userId, userId.intValue());
        } else {
            logger.info("查询条件: 查询所有宠物（未指定userId）");
        }
        IPage<Pet> result = petMapper.selectPage(page, queryWrapper);
        logger.info("PetService查询结果 - 总数: {}, 记录数: {}", result.getTotal(), result.getRecords().size());
        if (result.getRecords().size() > 0) {
            Pet firstPet = result.getRecords().get(0);
            logger.info("第一条宠物记录 - id: {}, userId: {}, name: {}", firstPet.getId(), firstPet.getUserId(), firstPet.getName());
        }
        return result;
    }
    
    @Override
    public List<Pet> getPetsByUserId(Long userId) {
        if (userId == null) {
            logger.warn("getPetsByUserId: userId 为空，返回空列表");
            return List.of();
        }
        
        String cacheKey = CACHE_KEY_PREFIX + userId;
        
        // 尝试从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StringUtils.hasText(cacheValue)) {
                    logger.info("从 Redis 缓存获取宠物列表，userId: {}, cacheKey: {}", userId, cacheKey);
                    List<Pet> cachedPets = JSON.parseObject(cacheValue, new TypeReference<List<Pet>>() {});
                    logger.info("缓存命中，返回 {} 条宠物数据", cachedPets != null ? cachedPets.size() : 0);
                    return cachedPets != null ? cachedPets : List.of();
                } else {
                    logger.info("缓存未命中，从数据库查询，userId: {}, cacheKey: {}", userId, cacheKey);
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取缓存失败，降级到数据库查询，userId: {}, 错误: {}", userId, e.getMessage());
            }
        } else {
            logger.warn("StringRedisTemplate 未注入，跳过缓存，直接查询数据库");
        }
        
        // 从数据库查询
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId.intValue());
        List<Pet> pets = petMapper.selectList(queryWrapper);
        
        logger.info("从数据库查询到 {} 条宠物数据，userId: {}", pets.size(), userId);
        
        // 写入 Redis 缓存
        if (stringRedisTemplate != null && pets != null) {
            try {
                String jsonValue = JSON.toJSONString(pets);
                stringRedisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入 Redis 缓存，userId: {}, cacheKey: {}, 过期时间: {} 分钟", userId, cacheKey, CACHE_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入 Redis 缓存失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
            }
        }
        
        return pets != null ? pets : List.of();
    }
    
    @Override
    public void clearPetListCache(Long userId) {
        if (userId == null) {
            logger.warn("clearPetListCache: userId 为空，跳过缓存清除");
            return;
        }
        
        if (stringRedisTemplate == null) {
            logger.warn("StringRedisTemplate 未注入，跳过缓存清除");
            return;
        }
        
        String cacheKey = CACHE_KEY_PREFIX + userId;
        try {
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("成功清除宠物列表缓存，userId: {}, cacheKey: {}", userId, cacheKey);
            } else {
                logger.info("缓存不存在或已被清除，userId: {}, cacheKey: {}", userId, cacheKey);
            }
        } catch (Exception e) {
            logger.error("清除 Redis 缓存失败，userId: {}, cacheKey: {}, 错误: {}", userId, cacheKey, e.getMessage(), e);
        }
    }

    @Override
    public Pet createPet(Pet pet) {
        logger.info("PetService.createPet - 开始插入宠物");
        logger.info("宠物数据 - userId: {}, name: {}, species: {}", pet.getUserId(), pet.getName(), pet.getSpecies());
        
        // 验证必要字段
        if (pet.getUserId() == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (pet.getName() == null || pet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("宠物名称不能为空");
        }
        
        // 设置默认值
        if (pet.getStatus() == null || pet.getStatus().isEmpty()) {
            pet.setStatus("active");
        }
        
        // 执行插入
        int result = petMapper.insert(pet);
        logger.info("PetService.createPet - 插入结果: {}, 插入后的宠物ID: {}", result, pet.getId());
        
        // 清除对应用户的缓存
        clearPetListCache((long) pet.getUserId());
        
        return pet;
    }

    @Override
    public Pet updatePet(Pet pet) {
        logger.info("PetService.updatePet - 开始更新宠物，id: {}, userId: {}", pet.getId(), pet.getUserId());
        
        petMapper.updateById(pet);

        Pet fresh = petMapper.selectById(pet.getId());
        
        // 清除对应用户的缓存
        Integer uid = fresh != null ? fresh.getUserId() : pet.getUserId();
        if (uid != null) {
            clearPetListCache(uid.longValue());
        }
        
        logger.info("PetService.updatePet - 更新成功，id: {}", pet.getId());
        return fresh != null ? fresh : pet;
    }

    @Override
    public boolean deletePet(Long id) {
        logger.info("PetService.deletePet - 开始删除宠物，id: {}", id);
        
        // 先查询宠物信息，获取 userId，用于清除缓存
        Pet pet = petMapper.selectById(id.intValue());
        
        // 将Long转换为Integer（数据库id字段是int类型）
        boolean result = petMapper.deleteById(id.intValue()) > 0;
        
        if (result) {
            logger.info("PetService.deletePet - 删除成功，id: {}", id);
            // 清除对应用户的缓存
            if (pet != null && pet.getUserId() != null) {
                clearPetListCache((long) pet.getUserId());
            }
        } else {
            logger.warn("PetService.deletePet - 删除失败，id: {}", id);
        }
        
        return result;
    }

    @Override
    public Pet getPetById(Long id) {
        logger.info("PetService.getPetById - 查询宠物详情，id: {}", id);
        // 将Long转换为Integer（数据库id字段是int类型）
        Pet pet = petMapper.selectById(id.intValue());
        if (pet != null) {
            logger.info("PetService.getPetById - 查询成功，id: {}, name: {}", id, pet.getName());
        } else {
            logger.warn("PetService.getPetById - 未找到宠物，id: {}", id);
        }
        return pet;
    }
}
