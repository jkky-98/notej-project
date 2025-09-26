// utils/cookie.js
import Cookies from 'js-cookie';
/**
 * 인증 관련 쿠키가 존재하는지 확인하는 함수
 *
 * @returns {boolean} 인증 쿠키 존재 여부
 */
export function hasAuthCookies () {
  // 1. 일반 쿠키 확인 (js-cookie 라이브러리 사용 시)
  if (typeof Cookies !== 'undefined') {
    // 실제 프로젝트의 쿠키 이름으로 수정 필요
    if (Cookies.get('access_token') || Cookies.get('refresh_token')) {
      return true;
    }
  }

  // 2. HTTP Only 쿠키는 직접 내용을 확인할 수 없지만
  // 쿠키 문자열에 특정 이름이 포함되어 있는지 확인 가능
  const cookieString = document.cookie;

  // 실제 프로젝트의 쿠키 이름으로 수정 필요
  return cookieString.includes('access_token=') ||
         cookieString.includes('refresh_token=') ||
         cookieString.includes('auth=');
}
