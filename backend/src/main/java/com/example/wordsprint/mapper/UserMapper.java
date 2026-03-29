package com.example.wordsprint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wordsprint.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
