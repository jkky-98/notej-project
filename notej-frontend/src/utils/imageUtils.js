// src/utils/imageUtils.js
import imageCompression from 'browser-image-compression';

/**
 * 이미지 파일 압축 유틸리티
 */
export async function compressImage (file, options = {}) {
  const defaultOptions = {
    maxSizeMB: 1, // 최대 1MB로 압축
    maxWidthOrHeight: 800, // 최대 800px
    useWebWorker: true, // 백그라운드 처리
    fileType: 'image/jpeg', // JPEG로 통일
  };

  const compressionOptions = { ...defaultOptions, ...options };

  console.log('압축 시작 - 원본 크기:', (file.size / 1024 / 1024).toFixed(2), 'MB');
  const compressedFile = await imageCompression(file, compressionOptions);
  console.log('압축 완료 - 압축 후 크기:', (compressedFile.size / 1024 / 1024).toFixed(2), 'MB');

  return compressedFile;
}

/**
 * 이미지 유효성 검증
 */
export function validateImage (file, maxSizeMB = 10) {
  // 파일 없으면 에러
  if (!file) {
    return { valid: false, error: '파일이 선택되지 않았습니다' };
  }

  // 이미지 타입 체크
  if (!file.type.match('image.*')) {
    return { valid: false, error: '이미지 파일만 업로드 가능합니다' };
  }

  // 용량 체크
  if (file.size > maxSizeMB * 1024 * 1024) {
    return { valid: false, error: `이미지 크기는 ${maxSizeMB}MB 이하여야 합니다` };
  }

  return { valid: true };
}

/**
 * 이미지 업로드용 FormData 생성
 */
export function createImageFormData (file, fieldName = 'profileImage', fileName = 'profile.jpg') {
  const formData = new FormData();
  formData.append(fieldName, file, fileName);
  return formData;
}
