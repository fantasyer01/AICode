package com.example.auditdemo.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.auditdemo.permission.entity.SysFieldPermission;
import com.example.auditdemo.permission.mapper.SysFieldPermissionMapper;
import com.example.auditdemo.permission.service.FieldPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Field Permission Service implementation
 */
@Service
@Slf4j
public class FieldPermissionServiceImpl implements FieldPermissionService {

    @Autowired
    private SysFieldPermissionMapper permissionMapper;

    @Override
    public Set<String> getHiddenFields(String tableName) {
        LambdaQueryWrapper<SysFieldPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFieldPermission::getTableName, tableName)
               .eq(SysFieldPermission::getQueryable, 0);
        
        List<SysFieldPermission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(SysFieldPermission::getFieldName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getQueryableFields(String tableName) {
        LambdaQueryWrapper<SysFieldPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFieldPermission::getTableName, tableName)
               .eq(SysFieldPermission::getQueryable, 1);
        
        List<SysFieldPermission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(SysFieldPermission::getFieldName)
                .collect(Collectors.toSet());
    }

    @Override
    public void filterObject(Object obj, String tableName) {
        if (obj == null) {
            return;
        }

        Set<String> hiddenFields = getHiddenFields(tableName);
        if (hiddenFields.isEmpty()) {
            return;
        }

        setFieldsToNull(obj, hiddenFields);
    }

    @Override
    public void filterObjectWithNested(Object obj, Map<Class<?>, String> tableMapping) {
        if (obj == null || tableMapping == null || tableMapping.isEmpty()) {
            return;
        }

        filterObjectRecursive(obj, tableMapping, new HashSet<>());
    }

    private void filterObjectRecursive(Object obj, Map<Class<?>, String> tableMapping, Set<Object> visited) {
        if (obj == null || visited.contains(obj)) {
            return;
        }
        visited.add(obj);

        Class<?> objClass = obj.getClass();
        
        // Check if this object's class is in the mapping
        String tableName = tableMapping.get(objClass);
        if (tableName != null) {
            Set<String> hiddenFields = getHiddenFields(tableName);
            setFieldsToNull(obj, hiddenFields);
        }

        // Process all fields for nested objects
        for (Field field : getAllFields(objClass)) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                if (fieldValue == null) {
                    continue;
                }

                // Handle Collection (e.g., List<BizOrderItem>)
                if (fieldValue instanceof Collection) {
                    Collection<?> collection = (Collection<?>) fieldValue;
                    for (Object item : collection) {
                        filterObjectRecursive(item, tableMapping, visited);
                    }
                }
                // Handle nested object that's in the mapping
                else if (tableMapping.containsKey(fieldValue.getClass())) {
                    filterObjectRecursive(fieldValue, tableMapping, visited);
                }
            } catch (IllegalAccessException e) {
                log.warn("Failed to access field {} in class {}", field.getName(), objClass.getName());
            }
        }
    }

    private void setFieldsToNull(Object obj, Set<String> fieldNames) {
        if (obj == null || fieldNames == null || fieldNames.isEmpty()) {
            return;
        }

        Class<?> clazz = obj.getClass();
        for (String fieldName : fieldNames) {
            try {
                Field field = findField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(obj, null);
                }
            } catch (IllegalAccessException e) {
                log.warn("Failed to set field {} to null in class {}", fieldName, clazz.getName());
            }
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    @Override
    public List<SysFieldPermission> getPermissionsByTable(String tableName) {
        LambdaQueryWrapper<SysFieldPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFieldPermission::getTableName, tableName)
               .orderByAsc(SysFieldPermission::getId);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public List<String> getAllTableNames() {
        LambdaQueryWrapper<SysFieldPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysFieldPermission::getTableName)
               .groupBy(SysFieldPermission::getTableName)
               .orderByAsc(SysFieldPermission::getTableName);
        
        List<SysFieldPermission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(SysFieldPermission::getTableName)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePermission(SysFieldPermission permission) {
        permission.setUpdateTime(null); // Let DB handle it
        permissionMapper.updateById(permission);
    }

    @Override
    public void batchUpdateQueryable(String tableName, List<String> fieldNames, Integer queryable) {
        if (fieldNames == null || fieldNames.isEmpty()) {
            return;
        }

        LambdaUpdateWrapper<SysFieldPermission> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysFieldPermission::getTableName, tableName)
               .in(SysFieldPermission::getFieldName, fieldNames)
               .set(SysFieldPermission::getQueryable, queryable);
        
        permissionMapper.update(null, wrapper);
    }

    @Override
    public SysFieldPermission getById(Long id) {
        return permissionMapper.selectById(id);
    }
}
