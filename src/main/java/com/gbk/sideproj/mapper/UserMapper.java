package com.gbk.sideproj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gbk.sideproj.domain.User;

@Mapper
public interface UserMapper {
	
	void insertUser(User user);

    User findByUsername(@Param("username") String username);
}
