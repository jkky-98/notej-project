package com.github.jkky_98.noteJ.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientUtilsTest {
    @Test
    @DisplayName("[ClientUtils] X-FORWARDED-FOR 헤더가 있는 경우 IP 주소를 반환한다.")
    void getRemoteIP_withXForwardedForHeader() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-FORWARDED-FOR", "192.168.1.10");

        // When
        String ip = ClientUtils.getRemoteIP(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.10");
    }

    @Test
    @DisplayName("[ClientUtils] Proxy-Client-IP 헤더가 있는 경우 IP 주소를 반환한다.")
    void getRemoteIP_withProxyClientHeader() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Proxy-Client-IP", "192.168.1.11");

        // When
        String ip = ClientUtils.getRemoteIP(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.11");
    }

    @Test
    @DisplayName("[ClientUtils] WL-Proxy-Client-IP 헤더가 있는 경우 IP 주소를 반환한다.")
    void getRemoteIP_withWebLogicHeader() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("WL-Proxy-Client-IP", "192.168.1.12");

        // When
        String ip = ClientUtils.getRemoteIP(request);

        // Then
        assertThat(ip).isEqualTo("192.168.1.12");
    }

    @Test
    @DisplayName("[ClientUtils] 모든 헤더가 없을 때 request.getRemoteAddr()를 반환한다.")
    void getRemoteIP_withRemoteAddr() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        // When
        String ip = ClientUtils.getRemoteIP(request);

        // Then
        assertThat(ip).isEqualTo("127.0.0.1");
    }

}