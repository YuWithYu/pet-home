package com.pethome.service;

import com.pethome.entity.Product;
import com.pethome.entity.Post;
import com.pethome.entity.ServiceStore;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 首页搜索 - 搜索商品和服务
     * @param keyword 关键词
     * @return 包含商品和服务的结果
     */
    HomeSearchResult searchHome(String keyword);
    
    /**
     * 社区搜索 - 搜索帖子
     * @param keyword 关键词
     * @return 帖子列表
     */
    List<Post> searchCommunity(String keyword);
    
    /**
     * 获取热搜关键词
     * @return 热搜词列表
     */
    List<String> getHotKeywords();
    
    /**
     * 首页搜索结果
     */
    class HomeSearchResult {
        private List<Product> products;
        private List<ServiceStore> services;

        public HomeSearchResult() {
        }

        public HomeSearchResult(List<Product> products, List<ServiceStore> services) {
            this.products = products;
            this.services = services;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public List<ServiceStore> getServices() {
            return services;
        }

        public void setServices(List<ServiceStore> services) {
            this.services = services;
        }
    }
}

