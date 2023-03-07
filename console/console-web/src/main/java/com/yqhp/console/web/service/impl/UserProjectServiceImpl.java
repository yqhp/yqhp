package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateUserProjectParam;
import com.yqhp.console.model.param.DeleteUserProjectParam;
import com.yqhp.console.model.param.UpdateUserProjectParam;
import com.yqhp.console.repository.entity.UserProject;
import com.yqhp.console.repository.mapper.UserProjectMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.UserProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class UserProjectServiceImpl
        extends ServiceImpl<UserProjectMapper, UserProject>
        implements UserProjectService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public void createUserProject(CreateUserProjectParam param) {
        UserProject userProject = param.convertTo();
        userProject.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        userProject.setCreateBy(currUid);
        userProject.setUpdateBy(currUid);

        try {
            if (!save(userProject)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_USER_PROJECT_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_USER_PROJECT);
        }
    }

    @Override
    public void updateUserProject(String id, UpdateUserProjectParam param) {
        UserProject userProject = getUserProjectById(id);
        param.update(userProject);
        userProject.setUpdateBy(CurrentUser.id());
        userProject.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(userProject)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_USER_PROJECT_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_USER_PROJECT);
        }
    }

    @Override
    public void deleteUserProjectById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_PROJECT_FAIL);
        }
    }

    @Override
    public void deleteUserProject(DeleteUserProjectParam param) {
        LambdaQueryWrapper<UserProject> query = new LambdaQueryWrapper<>();
        query.eq(UserProject::getUserId, param.getUserId());
        query.eq(UserProject::getProjectId, param.getProjectId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_PROJECT_FAIL);
        }
    }

    @Override
    public UserProject getUserProjectById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.USER_PROJECT_NOT_FOUND));
    }

    @Override
    public List<String> listProjectIdByUserId(String userId) {
        return listByUserId(userId).stream()
                .map(UserProject::getProjectId)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProject> listByUserId(String userId) {
        LambdaQueryWrapper<UserProject> query = new LambdaQueryWrapper<>();
        query.eq(UserProject::getUserId, userId);
        return list(query);
    }

}
