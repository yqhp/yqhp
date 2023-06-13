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
package com.yqhp.auth.web.controller;

import com.yqhp.auth.model.CurrentUser;
import com.yqhp.auth.model.vo.TokenVO;
import com.yqhp.auth.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * @author jiangyitao
 */
@Validated
@RestController
@RequestMapping("/token")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public TokenVO getToken(@NotBlank(message = "username不能为空") String username,
                            @NotBlank(message = "password不能为空") String password) {
        return tokenService.getToken(username, password);
    }

    @DeleteMapping("/remove")
    public void removeToken() {
        tokenService.removeTokenByUserId(CurrentUser.id());
    }
}
