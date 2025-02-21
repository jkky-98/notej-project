package com.github.jkky_98.noteJ;

import java.lang.reflect.Field;

public class TestUtils {
    public static void setField(Object target, String fieldName, Object value) {
        Field field = getFieldRecursively(target.getClass(), fieldName);
        if (field == null) {
            throw new RuntimeException("Field '" + fieldName + "' not found on " + target.getClass().getName());
        }
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "' on " + target.getClass().getName(), e);
        }
    }

    private static Field getFieldRecursively(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getFieldRecursively(clazz.getSuperclass(), fieldName);
        }
    }
}