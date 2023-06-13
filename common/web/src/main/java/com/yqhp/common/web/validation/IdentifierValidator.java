/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
