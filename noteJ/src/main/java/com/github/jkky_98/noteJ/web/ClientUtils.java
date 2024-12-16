package com.github.jkky_98.noteJ.web;

import jakarta.servlet.http.HttpServletRequest;

public class ClientUtils {
    public static String getRemoteIP(HttpServletRequest request){
        String ip = request.getHeader("X-FORWARDED-FOR");

        //proxy 환경일 경우
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        //웹로직 서버일 경우
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr() ;
        }

        return ip;
    }
}
