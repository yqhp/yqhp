package com.yqhp.auth.web.controller;

import com.yqhp.auth.model.param.CreateUserRoleParam;
import com.yqhp.auth.model.param.DeleteUserRoleParam;
import com.yqhp.auth.model.param.UpdateUserRoleParam;
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
    public void createUserRole(@Valid @RequestBody CreateUserRoleParam param) {
        userRoleService.createUserRole(param);
    }

    @PutMapping("/{id}")
    public void updateUserRole(@PathVariable String id, @Valid @RequestBody UpdateUserRoleParam param) {
        userRoleService.updateUserRole(id, param);
    }

    @GetMapping
    public List<UserRole> listByUserId(@NotBlank(message = "用户id不能为空") String userId) {
        return userRoleService.listByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        userRoleService.deleteById(id);
    }

    @DeleteMapping
    public void deleteUserRole(@Valid @RequestBody DeleteUserRoleParam param) {
        userRoleService.deleteUserRole(param);
    }
}
