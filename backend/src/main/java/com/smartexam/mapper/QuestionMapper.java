package com.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartexam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
