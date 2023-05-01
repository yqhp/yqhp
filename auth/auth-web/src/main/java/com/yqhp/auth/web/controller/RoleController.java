package com.yqhp.auth.web.controller;

import com.yqhp.auth.model.param.RoleParam;
import com.yqhp.auth.repository.entity.Role;
import com.yqhp.auth.web.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author jiangyitao
 */
@RestController
@RequestMapping("/role")
@PreAuthorize("hasAuthority('admin')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/all")
    public List<Role> getAllRoles() {
        return roleService.list();
    }

    @PostMapping
    public void createRole(@Valid @RequestBody RoleParam param) {
        roleService.createRole(param);
    }

    @PutMapping("/{id}")
    public void updateRole(@PathVariable String id, @Valid @RequestBody RoleParam param) {
        roleService.updateRole(id, param);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        roleService.deleteById(id);
    }
}
