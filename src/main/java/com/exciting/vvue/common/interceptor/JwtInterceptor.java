package com.exciting.vvue.common.interceptor;

import com.exciting.vvue.auth.jwt.exception.InvalidTokenException;
import com.exciting.vvue.auth.jwt.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String HEADER_AUTH = "Authorization";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler)
        throws Exception {

        log.debug("request : " + request.getHeader(HEADER_AUTH));

        final String token = request.getHeader(HEADER_AUTH);

        log.debug("Interceptor token = " + token);

        if(token == null || token.isBlank()){
            throw new InvalidTokenException("토큰 잘못됨");
        }

        try{
            if (jwtUtil.validateToken(token)) {
                log.info("토큰 사용 가능 : {}", token);
                return true;
            }
        }catch (JwtException e){
            log.info("토큰 사용 불가능 : {}",token);

        }

        throw new InvalidTokenException("토큰 잘못됨");
    }
}