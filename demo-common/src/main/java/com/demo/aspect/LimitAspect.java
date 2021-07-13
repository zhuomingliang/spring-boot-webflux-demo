/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.demo.aspect;

import com.google.common.collect.ImmutableList;
import com.demo.annotation.Limit;
import com.demo.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author /
 */
@Aspect
@Component
public class LimitAspect {

//    private final RedisTemplate<Object,Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LimitAspect.class);

//    public LimitAspect(RedisTemplate<Object,Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;
//
//    public <T> T runLua(String fileClasspath, Class<T> returnType, List<String> keys, Object ... values){
//        DefaultRedisScript<T> redisScript =new DefaultRedisScript<>();
//        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(fileClasspath)));
//        redisScript.setResultType(returnType);
//        return stringRedisTemplate.execute(redisScript, keys, values);
//    }


    @Pointcut("@annotation(com.demo.annotation.Limit)")
    public void pointcut() {
    }

    @Before(value = "@annotation(com.demo.annotation.Limit)")
    public void before(JoinPoint joinPoint) throws Throwable {
        ServerHttpRequest request = null;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method signatureMethod = signature.getMethod();

        Limit limit = signatureMethod.getAnnotation(Limit.class);
        LimitType limitType = limit.limitType();
        String key = limit.key();
        if (StringUtils.isEmpty(key)) {
            if (limitType == LimitType.IP) {
                Object[] args = joinPoint.getArgs();

                for (Object arg : args) {
                    if (arg instanceof ServerHttpRequest) {
                        request = (ServerHttpRequest) arg;
                        break;
                    }

                    if (arg instanceof ServerWebExchange) {
                        request = ((ServerWebExchange)arg).getRequest();
                        break;
                    }
                }

                if (request == null) {
                    logger.warn("ServerHttpRequest 参数为空，无法限流！");
                    return;
                }
                key = StringUtils.getIp(request);
            } else {
                key = signatureMethod.getName();
            }
        }

        ImmutableList<Object> keys = ImmutableList.of(StringUtils.join(limit.prefix(), "_", key, "_", request.getURI().getRawPath().replaceAll("/","_")));
//        String luaScript = buildLuaScript();
//        logger.debug(luaScript);
//        RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
//        Number count = redisTemplate.execute(redisScript, keys, limit.count(), limit.period());
//        if (null != count && count.intValue() <= limit.count()) {
            logger.info("第{}次访问key为 {}，描述为 [{}] 的接口", 111, keys, limit.name());
//            return joinPoint.proceed();
//        } else {
//            throw new BadRequestException("访问次数受限制");
//        }
    }

    /**
     * 限流脚本
     */
    private String buildLuaScript() {
        return """
                local c
                c = redis.call('get',KEYS[1])
                if c and tonumber(c) > tonumber(ARGV[1]) then
                return c;
                end
                c = redis.call('incr',KEYS[1])
                if tonumber(c) == 1 then
                redis.call('expire',KEYS[1],ARGV[2])
                end
                return c;""";
    }
}
