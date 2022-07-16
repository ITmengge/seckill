package com.simon.seckill.config;

import com.simon.seckill.pojo.User;

/**
 * ThreadLocal:让每个用户拥有属于自己的线程,用户的值只在当前线程有效,避免线程安全问题
 */
public class UserContext {
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }
}
