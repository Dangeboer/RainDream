package com.dangeboer.raindream.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.model.dto.LoginResponse;
import com.dangeboer.raindream.model.entity.User;

public interface AuthenticationService extends IService<User> {
    Integer register(String username, String password, String phone);
    LoginResponse login(String username, String password);
}
