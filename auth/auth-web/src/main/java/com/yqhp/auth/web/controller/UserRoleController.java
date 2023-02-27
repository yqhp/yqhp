package com.yqhp.auth.web.controller;

import com.yqhp.auth.model.param.UserRoleParam;
import com.yqhp.auth.repository.entity.UserRole;
import com.yqhp.auth.web.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/userRole")
@Validated
@PreAuthorize("hasAuthority('admin')")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping
    public void createUserRole(@Valid @RequestBody UserRoleParam userRoleParam) {
        userRoleService.createUserRole(userRoleParam);
    }

    @GetMapping
    public List<UserRole> listByUserId(@NotBlank(message = "用户id不能为空") String userId) {
        return userRoleService.listByUserId(userId);
    }

    @DeleteMapping
    public void deleteUserRole(@Valid @RequestBody UserRoleParam userRoleParam) {
        userRoleService.deleteUserRole(userRoleParam);
    }
}
