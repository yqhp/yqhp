package com.yqhp.common.web.converter;

import com.yqhp.common.base.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.StringUtils;

/**
 * @author jiangyitao
 * 只能处理非application/json请求
 */
public class EnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends BaseEnum> implements Converter<String, T> {
        private final Class<T> enumType;

        StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (!StringUtils.hasText(source)) {
                return null;
            }

            T[] enums = enumType.getEnumConstants();
            if (enums != null) {
                for (T e : enums) {
                    if (source.equals(String.valueOf(e.getValue()))) {
                        return e;
                    }
                }
            }

            throw new IllegalArgumentException("unknown " + source);
        }
    }
}
