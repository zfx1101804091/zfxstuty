package com.zfx.netty.simple.client.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * <b>  </b>
 * <p>
 * 功能描述:解决注入的方式获取不到bean的情况
 * 
 * </p>
 * <p/>
 * @author 朱维
 * @date 2018年11月2日
 * @time 下午4:54:24
 * @Path: com.xuebaclass.sato.socket.SpringUtil.java
 */
@Component
public class SpringUtil implements ApplicationContextAware {
 
    private static ApplicationContext applicationContext = null;
 
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       if(SpringUtil.applicationContext == null){
           SpringUtil.applicationContext  = applicationContext;
       }
    }
    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
       return applicationContext;
    }
 
    //通过name获取 Bean.
    public static Object getBean(String name){
       return getApplicationContext().getBean(name);
    }
 
    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
       return getApplicationContext().getBean(clazz);
 
    }
 
    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
       return getApplicationContext().getBean(name, clazz);
    }
 
}