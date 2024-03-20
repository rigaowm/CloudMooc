package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Rigao
 * @Title: UserService
 * @Date: 2024/2/22 9:21
 * @Version 1.0
 * @Description:
 */

@Slf4j
@Component
public class UserService implements UserDetailsService {


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper xcMenuMapper;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        String authType = authParamsDto.getAuthType();
        String beanName = authType +"_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        UserDetails userDetails = getUserPrincipal(xcUserExt);
        return userDetails;
    }


    public UserDetails getUserPrincipal(XcUserExt xcUserExt){
        String userString = JSON.toJSONString(xcUserExt);
        String password = xcUserExt.getPassword();
        xcUserExt.setPassword(null);
        String id = xcUserExt.getId();
        List<XcMenu> menus = xcMenuMapper.selectPermissionByUserId(id);
        String[] authorities = {"test"};

        if(menus.size()>0){
            List<String> premissions = new ArrayList<>();
            menus.stream().forEach( menu -> premissions.add(menu.getCode()));
            authorities = premissions.toArray(new String[0]);
        }
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }
}
