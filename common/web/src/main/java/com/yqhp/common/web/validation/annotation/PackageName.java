package com.yqhp.common.web.validation.annotation;

import com.yqhp.common.web.validation.PackageNameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author jiangyitao
 */
@Documented
@Constraint(validatedBy = PackageNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PackageName {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
