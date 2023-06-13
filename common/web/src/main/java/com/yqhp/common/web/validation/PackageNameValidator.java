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
