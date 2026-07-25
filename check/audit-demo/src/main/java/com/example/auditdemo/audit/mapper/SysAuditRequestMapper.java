package com.example.auditdemo.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditdemo.audit.entity.SysAuditRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditRequestMapper extends BaseMapper<SysAuditRequest> {
}
