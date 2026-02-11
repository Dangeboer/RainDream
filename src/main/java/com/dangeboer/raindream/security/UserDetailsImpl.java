package com.dangeboer.raindream.security;

import com.dangeboer.raindream.exception.CanNotFoundException;
import com.dangeboer.raindream.mapper.UserMapper;
import com.dangeboer.raindream.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new CanNotFoundException("用户不存在");
        }
        return user;
    }
}
