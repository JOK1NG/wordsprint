package com.example.wordsprint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wordsprint.entity.WrongWord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WrongWordMapper extends BaseMapper<WrongWord> {
}
