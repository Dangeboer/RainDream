package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.UserMapper;
import com.dangeboer.raindream.model.dto.LoginResponse;
import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.security.JwtHandler;
import com.dangeboer.raindream.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl extends ServiceImpl<UserMapper, User> implements AuthenticationService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtHandler jwtHandler;

    @Override
    public Integer register(String username, String password, String phone) {
        if (userMapper.findUserByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User(username, passwordEncoder.encode(password), phone, null);
        return userMapper.insert(user);
    }

    @Override
    public LoginResponse login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return new LoginResponse(jwtHandler.generateToken(username));
    }
}
