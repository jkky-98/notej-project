package com.github.jkky_98.noteJ.web.util;

public class RefererUtil {

    public static String removeQueryStringInReferer(String referer) {
        if (referer != null) {
            int queryIndex = referer.indexOf("?");
            if (queryIndex != -1) {
                referer = referer.substring(0, queryIndex); // 쿼리 파라미터 제거
            }
        }
        return referer;
    }
}
