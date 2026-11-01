package com.pethome.service.order.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethome.service.order.LogisticsService;
import com.pethome.service.order.support.LogisticsMockDataGenerator;
import com.pethome.service.order.support.LogisticsTrackParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    private static final Logger log = LoggerFactory.getLogger(LogisticsServiceImpl.class);

    @Value("${logistics.kuaidi100.customer:}")
    private String kuaidi100Customer;
    
    @Value("${logistics.kuaidi100.key:}")
    private String kuaidi100Key;
    
    @Value("${logistics.kuaidi100.enabled:false}")
    private boolean kuaidi100Enabled;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LogisticsTrackParser trackParser;

    @Autowired
    private LogisticsMockDataGenerator mockDataGenerator;

    @Override
    public Map<String, Object> queryLogistics(String shippingCompany, String shippingNumber, String receiverAddress, String warehouseAddress, java.time.LocalDateTime shippingTime) {
        if (shippingCompany == null || shippingNumber == null || shippingNumber.trim().isEmpty()) {
            return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, shippingTime);
        }

        // 如果启用了快递100且配置了key，则调用真实API
        if (kuaidi100Enabled && kuaidi100Key != null && !kuaidi100Key.isEmpty()) {
            try {
                Map<String, Object> result = queryKuaidi100(shippingCompany, shippingNumber, receiverAddress, warehouseAddress);
                // 如果真实API返回空数据，使用模拟数据
                if (result == null || !result.containsKey("tracks") || 
                    ((List<?>) result.get("tracks")).isEmpty()) {
                    return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, shippingTime);
                }
                return result;
            } catch (Exception e) {
                log.error("查询快递100失败，使用模拟数据", e);
                // 如果API调用失败，返回模拟数据
                return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, shippingTime);
            }
        } else {
            // 未配置或未启用，返回模拟数据
            return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, shippingTime);
        }
    }

    /**
     * 调用快递100 API查询物流信息
     */
    private Map<String, Object> queryKuaidi100(String shippingCompany, String shippingNumber, String receiverAddress, String warehouseAddress) {
        String companyCode = trackParser.resolveCompanyCode(shippingCompany);
        if (companyCode == null) {
            // 如果无法识别物流公司，使用模拟数据（使用当前时间作为发货时间）
            return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, java.time.LocalDateTime.now());
        }

        try {
            // 快递100 API URL
            String url = "https://poll.kuaidi100.com/poll/query.do";
            
            // 构建请求参数
            Map<String, Object> param = new HashMap<>();
            param.put("com", companyCode);
            param.put("num", shippingNumber);
            param.put("phone", ""); // 手机号（可选）
            param.put("from", ""); // 出发地（可选）
            param.put("to", ""); // 目的地（可选）
            param.put("resultv2", 1); // 开启行政区域解析
            
            String paramJson = objectMapper.writeValueAsString(param);
            String sign = trackParser.sign(paramJson, kuaidi100Key, kuaidi100Customer);
            
            // 构建请求体
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("customer", kuaidi100Customer);
            requestBody.put("param", paramJson);
            requestBody.put("sign", sign);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            
            // 构建表单数据
            StringBuilder formData = new StringBuilder();
            formData.append("customer=").append(kuaidi100Customer);
            formData.append("&param=").append(paramJson);
            formData.append("&sign=").append(sign);
            
            HttpEntity<String> entity = new HttpEntity<>(formData.toString(), headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            
            Map<String, Object> result = response.getBody();
            if (result != null && "200".equals(String.valueOf(result.get("status")))) {
                return trackParser.parseKuaidi100Response(result);
            } else {
                // API返回错误，使用模拟数据（使用当前时间作为发货时间）
                return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, java.time.LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("调用快递100 API异常", e);
            return mockDataGenerator.generate(shippingCompany, shippingNumber, receiverAddress, warehouseAddress, java.time.LocalDateTime.now());
        }
    }

    @Override
    public List<Map<String, String>> getSupportedCompanies() {
        List<Map<String, String>> companies = new ArrayList<>();
        for (String name : trackParser.supportedCompanyNames()) {
            Map<String, String> company = new HashMap<>();
            company.put("name", name);
            company.put("code", trackParser.resolveCompanyCode(name));
            companies.add(company);
        }
        return companies;
    }
}
