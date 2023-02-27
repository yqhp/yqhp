package com.yqhp.common.web.validation.annotation;

import com.yqhp.common.web.validation.IdentifierValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author jiangyitao
 */
@Documented
@Constraint(validatedBy = IdentifierValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Identifier {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
