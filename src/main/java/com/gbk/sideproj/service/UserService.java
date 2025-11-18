package com.gbk.sideproj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gbk.sideproj.domain.User;
import com.gbk.sideproj.mapper.UserMapper;

@Service
public class UserService {
	
	  @Autowired
	    private UserMapper userMapper;

	    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	    public void register(User user) {
	        user.setPassword(encoder.encode(user.getPassword())); // 비밀번호 암호화
	        userMapper.insertUser(user);
	    }

	    public User login(String username, String rawPassword) {
	        User user = userMapper.findByUsername(username);
	        if (user != null && encoder.matches(rawPassword, user.getPassword())) {
	            return user; // 로그인 성공
	        }
	        return null; // 실패
	    }
}
