package com.example.auditdemo.audit.model;

/**
 * Enum representing the type of database operation
 */
public enum OperationType {
    INSERT("新增"),
    UPDATE("修改"),
    DELETE("删除");

    private final String desc;

    OperationType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
