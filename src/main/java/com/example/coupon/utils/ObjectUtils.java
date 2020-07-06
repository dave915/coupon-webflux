package com.example.coupon.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ObjectUtils {
    public static final String FIELD_SEPARATOR = ",";
    @SuppressWarnings("serial")
    private static final Map<Class<?>, PropertyEditor> defaultEditors = new HashMap<Class<?>, PropertyEditor>() {
        {
            put(boolean.class, new CustomBooleanEditor(false));
            put(Boolean.class, new CustomBooleanEditor(true));


            put(byte.class, new CustomNumberEditor(Byte.class, false));
            put(Byte.class, new CustomNumberEditor(Byte.class, true));
            put(int.class, new CustomNumberEditor(Integer.class, false));
            put(Integer.class, new CustomNumberEditor(Integer.class, true));
            put(long.class, new CustomNumberEditor(Long.class, false));
            put(Long.class, new CustomNumberEditor(Long.class, true));
        }
    };

    public static <T> T csvRowToObject(String string, List<String> fields, Class<T> tClass) throws ReflectiveOperationException {
        try {
            String[] valueString = string.split(FIELD_SEPARATOR);
            Object[] fieldValues = new Object[valueString.length];
            Class<?>[] fieldTypes = new Class[valueString.length];

            for (int i = 0; i < fields.size(); i++) {
                Field field = tClass.getDeclaredField(fields.get(i).trim());
                field.setAccessible(true);
                fieldTypes[i] = field.getType();
                fieldValues[i] = convertFiledValue(field, valueString[i]);
            }

            return tClass.getConstructor(fieldTypes).newInstance(fieldValues);
        } catch (ReflectiveOperationException e) {
            log.error("csv string 컨버팅 중 오류가 발생하였습니다. ", e);
            throw e;
        }
    }

    private static Object convertFiledValue(Field field, String valueString) {
        if (field.getType().isAssignableFrom(String.class)) {
            return valueString;
        }

        PropertyEditor propertyEditor = defaultEditors.get(field.getType());
        propertyEditor.setAsText(valueString);
        return propertyEditor.getValue();
    }
}
