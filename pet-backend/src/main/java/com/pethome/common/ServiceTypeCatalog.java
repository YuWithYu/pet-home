package com.pethome.common;

/**
 * 服务类型字典。
 *
 * <p>服务类型的编码、中文名、部门名在多处使用，集中定义避免出现不一致的映射。</p>
 */
public final class ServiceTypeCatalog {

    public static final String DOOR_CLEANING = "door-cleaning";
    public static final String GROOMING = "grooming";
    public static final String HOSPITAL = "hospital";

    private ServiceTypeCatalog() {
    }

    /** 服务类型中文名，未知类型原样返回 */
    public static String typeName(String serviceType) {
        if (serviceType == null) {
            return "";
        }
        switch (serviceType) {
            case DOOR_CLEANING:
                return "上门铲屎";
            case GROOMING:
                return "宠物洗护";
            case HOSPITAL:
                return "宠物医院";
            default:
                return serviceType;
        }
    }

    /** 服务类型对应的部门名，未知类型原样返回 */
    public static String departmentName(String serviceType) {
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return "未分配部门";
        }
        switch (serviceType) {
            case DOOR_CLEANING:
                return "上门铲屎部门";
            case GROOMING:
                return "宠物洗护部门";
            case HOSPITAL:
                return "宠物医院部门";
            default:
                return serviceType;
        }
    }

    /** 是否是已知的服务类型 */
    public static boolean isKnown(String serviceType) {
        return DOOR_CLEANING.equals(serviceType) || GROOMING.equals(serviceType) || HOSPITAL.equals(serviceType);
    }
}
