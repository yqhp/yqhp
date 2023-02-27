package com.yqhp.console.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yqhp.console.model.param.UserProjectParam;
import com.yqhp.console.repository.entity.UserProject;

import java.util.List;

/**
 * @author jiangyitao
 */
public interface UserProjectService extends IService<UserProject> {
    UserProject createUserProject(UserProjectParam userProjectParam);

    void deleteUserProject(UserProjectParam userProjectParam);

    List<String> listProjectIdByUserId(String userId);

    List<UserProject> listByUserId(String userId);
}
