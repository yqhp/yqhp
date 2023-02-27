package com.yqhp.common.web.validation;

import com.yqhp.common.web.validation.annotation.Identifier;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author jiangyitao
 * 校验是否符合java标识符规范
 */
public class IdentifierValidator implements ConstraintValidator<Identifier, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 只检查非空字符串
        return !StringUtils.hasText(value) || valid(value);
    }

    public static boolean valid(String value) {
        // 校验首字符
        if (!Character.isJavaIdentifierStart(value.charAt(0))) {
            return false;
        }

        int len = value.length();
        // 校验首字符之后的字符
        for (int i = 1; i < len; i++) {
            if (!Character.isJavaIdentifierPart(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

}
