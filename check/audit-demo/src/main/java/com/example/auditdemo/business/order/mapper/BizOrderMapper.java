package com.example.auditdemo.business.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditdemo.business.order.entity.BizOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrder> {
}
