package com.yqhp.console.web.controller;

import com.yqhp.console.model.param.CreateUserProjectParam;
import com.yqhp.console.model.param.DeleteUserProjectParam;
import com.yqhp.console.model.param.UpdateUserProjectParam;
import com.yqhp.console.repository.entity.UserProject;
import com.yqhp.console.web.service.UserProjectService;
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
@RequestMapping("/userProject")
@Validated
@PreAuthorize("hasAuthority('admin')")
public class UserProjectController {

    @Autowired
    private UserProjectService userProjectService;

    @PostMapping
    public void createUserProject(@Valid @RequestBody CreateUserProjectParam param) {
        userProjectService.createUserProject(param);
    }

    @PutMapping("/{id}")
    public void updateUserProject(@PathVariable String id, @Valid @RequestBody UpdateUserProjectParam param) {
        userProjectService.updateUserProject(id, param);
    }

    @GetMapping
    public List<UserProject> listByUserId(@NotBlank(message = "用户id不能为空") String userId) {
        return userProjectService.listByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserProjectById(@PathVariable String id) {
        userProjectService.deleteUserProjectById(id);
    }

    @DeleteMapping
    public void deleteUserProject(@Valid @RequestBody DeleteUserProjectParam param) {
        userProjectService.deleteUserProject(param);
    }
}
