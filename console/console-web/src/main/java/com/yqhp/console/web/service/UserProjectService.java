package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.CreateUserProjectParam;
import com.yqhp.console.model.param.DeleteUserProjectParam;
import com.yqhp.console.model.param.UpdateUserProjectParam;
import com.yqhp.console.repository.entity.UserProject;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface UserProjectService extends IService<UserProject> {
    void createUserProject(CreateUserProjectParam param);

    void updateUserProject(String id, UpdateUserProjectParam param);

    void deleteById(String id);

    UserProject getUserProjectById(String id);

    List<String> listProjectIdByUserId(String userId);

    List<UserProject> listByUserId(String userId);

    void deleteUserProject(DeleteUserProjectParam param);
}
