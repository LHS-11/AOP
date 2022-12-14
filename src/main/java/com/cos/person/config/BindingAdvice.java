package com.cos.person.config;

import com.cos.person.domain.CommonDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

// @Controller, @RestController, @Component, @Configuration
// @Configuration 설정 => 컨트롤러 진입하기 전에 설정이 필요할 때 그 외에 것들 @Component
@Component // 컴포넌트는 컨트롤러가 메모리에 뜨고나서 뜸
@Aspect
public class BindingAdvice { // 공통기능 : advice(aspect) 라고 부름

    private static final Logger log=LoggerFactory.getLogger(BindingAdvice.class);


    // Jointpoint : advice 를 적용해야 하는 부분, Pointcut : joint 의 부분으로 실제로 advice 가 적용된 부분
    // Weaving : Advice 가 핵심 기능에 적용하는 행위

    // 함수 : 앞, 뒤
    // 함수 : 앞 (username 이 안들어왔을 때 내가 강제로 넣어주고 실행)
    // 함수 : 뒤 (응답만 관리)

    // filter : 전처리할 때 , AOP : 앞 뒤 처리하고 싶을 때
    // filter 와 AOP (@Before) 차이점 : filter 는 전체, @Before 는 부분적 전처리


    @Before("execution(* com.cos.person.web..*Controller.*(..))")
    public void testCheck() {

        //request 값 처리 못하나요
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        System.out.println("주소 : " + request.getRequestURI());
        //log 처리는? 파일로 어떻게 남기죠?
        System.out.println("로그를 남겼습니다.");
    }


    @After("execution(* com.cos.person.web..*Controller.*(..))")
    public void testCheck1() {
        System.out.println("로그를 남겼습니다.");
    }

    //@Before
//    @After()
    @Around("execution(* com.cos.person.web..*Controller.*(..))")
    public Object validCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable { // ProceedingJoinPoint 메서드 가지고옴
        String type = proceedingJoinPoint.getSignature().getDeclaringTypeName();
        String method = proceedingJoinPoint.getSignature().getName();
        System.out.println("type = " + type);
        System.out.println("method = " + method);

        Object[] args = proceedingJoinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;

                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();
                    for (FieldError error : bindingResult.getFieldErrors()) {
                        errorMap.put(error.getField(), error.getDefaultMessage());

                        // 로그 레벨 error > warn > info > debug
                        log.warn(type+"."+method+"()=>필드: "+error.getField()+", 메시지:"+error.getDefaultMessage());
                        // DB 연결 -> DB 남기기
                        // File file=new File();
                        // sentry 이용시 log 를 다른 사이트에 올릴 수 있음
                    }
                    return new CommonDto<>(HttpStatus.BAD_REQUEST.value(), errorMap);
                }
            }
        }
        return proceedingJoinPoint.proceed();
    }

}
