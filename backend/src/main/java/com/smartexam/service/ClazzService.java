package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Clazz;
import com.smartexam.entity.User;
import com.smartexam.mapper.ClazzMapper;
import com.smartexam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClazzService extends ServiceImpl<ClazzMapper, Clazz> {
    
    @Autowired
    private UserMapper userMapper;
    
    public List<Clazz> findAll() {
        return list();
    }
    
    public Clazz findById(Long id) {
        return getById(id);
    }
    
    public Clazz create(Clazz clazz) {
        save(clazz);
        return clazz;
    }
    
    public Clazz update(Clazz clazz) {
        updateById(clazz);
        return clazz;
    }
    
    public void delete(Long id) {
        removeById(id);
    }
    
    public List<User> getStudentsByClazzId(Long clazzId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("clazz_id", clazzId).eq("role", 3);
        return userMapper.selectList(wrapper);
    }
    
    public User addStudentToClazz(Long clazzId, User student) {
        student.setClazzId(clazzId);
        student.setRole(3);
        student.setStatus(1);
        if (student.getPassword() == null) {
            student.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        }
        userMapper.insert(student);
        updateStudentCount(clazzId);
        return student;
    }
    
    public User updateStudent(User student) {
        userMapper.updateById(student);
        return student;
    }
    
    public void removeStudentFromClazz(Long clazzId, Long studentId) {
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", studentId).set("clazz_id", null);
        userMapper.update(null, wrapper);
        updateStudentCount(clazzId);
    }
    
    private void updateStudentCount(Long clazzId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("clazz_id", clazzId).eq("role", 3);
        Integer count = Math.toIntExact(userMapper.selectCount(wrapper));
        Clazz clazz = new Clazz();
        clazz.setId(clazzId);
        clazz.setStudentCount(count);
        updateById(clazz);
    }
}
