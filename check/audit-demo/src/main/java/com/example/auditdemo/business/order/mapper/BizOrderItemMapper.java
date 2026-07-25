package com.example.auditdemo.business.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auditdemo.business.order.entity.BizOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizOrderItemMapper extends BaseMapper<BizOrderItem> {
}
