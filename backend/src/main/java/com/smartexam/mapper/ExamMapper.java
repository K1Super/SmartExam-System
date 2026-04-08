package com.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartexam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
