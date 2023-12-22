package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @Author Rigao
 * @Title: GlobalCorsConfig
 * @Date: 2023/12/20 21:20
 * @Version 1.0
 * @Description:
 */

@Configuration
public class GlobalCorsConfig {


    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config  = new CorsConfiguration();
        config .addAllowedOrigin("*");
        //允许跨越发送cookie
        config .setAllowCredentials(true);
        //放行全部原始头信息
        config .addAllowedHeader("*");
        //允许所有请求方法跨域调用
        config .addAllowedMethod("*");
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",config );
        return new CorsFilter(corsConfigurationSource);
    }


}
