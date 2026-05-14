package com.pethome.dto;

import lombok.Data;
import java.util.List;

/**
 * 权限设置请求DTO
 */
@Data
public class PermissionRequest {
    private List<PermissionItem> permissions;
    
    @Data
    public static class PermissionItem {
        private String permissionCode;
        private String permissionName;
        private Integer status;
    }
}
