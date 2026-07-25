package com.example.auditdemo.business.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditdemo.business.user.entity.BizUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizUserMapper extends BaseMapper<BizUser> {
}
