package com.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartexam.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
