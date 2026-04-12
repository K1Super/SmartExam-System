package com.smartexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartexam.entity.Clazz;
import com.smartexam.entity.User;
import com.smartexam.mapper.ClazzMapper;
import com.smartexam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ClazzMapper clazzMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User findByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }
    
    public User findById(Long id) {
        return userMapper.selectById(id);
    }
    
    public List<User> findAll() {
        return userMapper.selectList(null);
    }
    
    public List<User> findByRole(Integer role) {
        return userMapper.selectList(new QueryWrapper<User>().eq("role", role));
    }
    
    public List<User> findByConditions(Long clazzId, Integer role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (clazzId != null) {
            wrapper.eq("clazz_id", clazzId);
        }
        if (role != null) {
            wrapper.eq("role", role);
        }
        return userMapper.selectList(wrapper);
    }
    
    public User create(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        
        // 如果创建用户时指定了班级，更新班级人数
        if (user.getClazzId() != null) {
            updateClazzStudentCount(user.getClazzId());
        }
        
        return user;
    }
    
    public void resetAllStudentPasswords() {
        String encodedPassword = passwordEncoder.encode("123456");
        List<User> students = findByRole(3);
        for (User student : students) {
            student.setPassword(encodedPassword);
            userMapper.updateById(student);
        }
    }
    
    public User update(User user) {
        try {
            User existingUser = userMapper.selectById(user.getId());
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }
            
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(existingUser.getPassword());
            }
            
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                user.setUsername(existingUser.getUsername());
            }
            if (user.getRealName() == null) {
                user.setRealName(existingUser.getRealName());
            }
            if (user.getPhone() == null) {
                user.setPhone(existingUser.getPhone());
            }
            if (user.getEmail() == null) {
                user.setEmail(existingUser.getEmail());
            }
            if (user.getRole() == null) {
                user.setRole(existingUser.getRole());
            }
            if (user.getStatus() == null) {
                user.setStatus(existingUser.getStatus());
            }
            
            Long oldClazzId = existingUser.getClazzId();
            Long newClazzId = user.getClazzId();
            
            // 单独处理clazzId（支持设置为null）
            boolean clazzIdChanged = oldClazzId == null ? newClazzId != null : !oldClazzId.equals(newClazzId);
            if (clazzIdChanged) {
                UpdateWrapper<User> wrapper = new UpdateWrapper<>();
                wrapper.eq("id", user.getId()).set("clazz_id", newClazzId);
                userMapper.update(null, wrapper);
                
                // 更新班级人数
                updateClazzStudentCount(oldClazzId);
                updateClazzStudentCount(newClazzId);
            }
            
            // 更新其他字段时保留原来的clazzId
            user.setClazzId(oldClazzId);
            userMapper.updateById(user);
            
            return user;
        } catch (Exception e) {
            System.err.println("更新用户失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void updateClazzStudentCount(Long clazzId) {
        if (clazzId == null) {
            return;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("clazz_id", clazzId).eq("role", 3);
        Integer count = Math.toIntExact(userMapper.selectCount(wrapper));
        Clazz clazz = new Clazz();
        clazz.setId(clazzId);
        clazz.setStudentCount(count);
        clazzMapper.updateById(clazz);
    }
    
    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        Long clazzId = user != null ? user.getClazzId() : null;
        
        userMapper.deleteById(id);
        
        // 如果删除的用户属于某个班级，更新该班级的人数
        if (clazzId != null) {
            updateClazzStudentCount(clazzId);
        }
        
        rearrangeIds();
    }
    
    private void rearrangeIds() {
        List<User> users = userMapper.selectList(new QueryWrapper<User>().orderByAsc("id"));
        
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        try {
            jdbcTemplate.execute("TRUNCATE TABLE user");
            
            long newId = 1;
            for (User user : users) {
                user.setId(newId++);
                userMapper.insert(user);
            }
            
            jdbcTemplate.execute("ALTER TABLE user AUTO_INCREMENT = " + (users.size() + 1));
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
