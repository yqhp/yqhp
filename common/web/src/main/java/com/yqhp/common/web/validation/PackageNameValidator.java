package com.yqhp.common.web.validation;

import com.yqhp.common.web.validation.annotation.PackageName;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

/**
 * @author jiangyitao
 */
public class PackageNameValidator implements ConstraintValidator<PackageName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 只检查非空字符串
        if (!StringUtils.hasLength(value)) {
            return true;
        }

        if (value.endsWith(".")) {
            return false;
        }

        return Stream.of(value.split("\\.")).allMatch(IdentifierValidator::valid);
    }
}
