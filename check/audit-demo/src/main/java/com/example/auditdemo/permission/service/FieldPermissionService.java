package com.example.auditdemo.permission.service;

import com.example.auditdemo.permission.entity.SysFieldPermission;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Field Permission Service interface
 */
public interface FieldPermissionService {

    /**
     * Get hidden field names for a table
     * @param tableName table name
     * @return set of field names that should be hidden
     */
    Set<String> getHiddenFields(String tableName);

    /**
     * Get queryable field names for a table
     * @param tableName table name
     * @return set of field names that are queryable
     */
    Set<String> getQueryableFields(String tableName);

    /**
     * Filter object by setting hidden fields to null
     * @param obj the object to filter
     * @param tableName the table name for permission lookup
     */
    void filterObject(Object obj, String tableName);

    /**
     * Filter object with nested object support
     * @param obj the object to filter
     * @param tableMapping mapping of class to table name for nested filtering
     */
    void filterObjectWithNested(Object obj, Map<Class<?>, String> tableMapping);

    /**
     * Get all permissions for a table
     * @param tableName table name
     * @return list of field permissions
     */
    List<SysFieldPermission> getPermissionsByTable(String tableName);

    /**
     * Get all distinct table names
     * @return list of table names
     */
    List<String> getAllTableNames();

    /**
     * Update field permission
     * @param permission the permission to update
     */
    void updatePermission(SysFieldPermission permission);

    /**
     * Batch update queryable status
     * @param tableName table name
     * @param fieldNames field names to update
     * @param queryable new queryable status
     */
    void batchUpdateQueryable(String tableName, List<String> fieldNames, Integer queryable);

    /**
     * Get permission by id
     * @param id permission id
     * @return permission
     */
    SysFieldPermission getById(Long id);
}
