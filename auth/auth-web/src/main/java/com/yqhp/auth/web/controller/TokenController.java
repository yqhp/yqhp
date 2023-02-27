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
