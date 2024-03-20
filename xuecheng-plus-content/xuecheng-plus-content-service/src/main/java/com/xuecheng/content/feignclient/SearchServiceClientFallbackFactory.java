package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Rigao
 * @Title: MediaServiceClientFallbackFactory
 * @Date: 2024/2/20 19:37
 * @Version 1.0
 * @Description:
 */

@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {


    @Override
    public SearchServiceClient create(Throwable cause) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.debug("远程信息调用发生熔断：{}",cause.toString(),cause);
                return false;
            }
        };
    }
}
