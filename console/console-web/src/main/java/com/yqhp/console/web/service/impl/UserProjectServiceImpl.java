package com.yqhp.console.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.UserProjectParam;
import com.yqhp.console.repository.entity.UserProject;
import com.yqhp.console.repository.mapper.UserProjectMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.UserProjectService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class UserProjectServiceImpl extends ServiceImpl<UserProjectMapper, UserProject> implements UserProjectService {

    @Override
    public UserProject createUserProject(UserProjectParam userProjectParam) {
        UserProject userProject = userProjectParam.convertTo();

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

        return userProject;
    }

    @Override
    public void deleteUserProject(UserProjectParam userProjectParam) {
        LambdaQueryWrapper<UserProject> query = new LambdaQueryWrapper<>();
        query.eq(UserProject::getUserId, userProjectParam.getUserId())
                .eq(UserProject::getProjectId, userProjectParam.getProjectId());
        if (!remove(query)) {
            throw new ServiceException(ResponseCodeEnum.DEL_USER_PROJECT_FAIL);
        }
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
