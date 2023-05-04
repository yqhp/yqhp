package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateViewParam;
import com.yqhp.console.model.param.UpdateViewParam;
import com.yqhp.console.repository.entity.View;
import com.yqhp.console.repository.mapper.ViewMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author jiangyitao
 */
@Service
public class ViewServiceImpl extends ServiceImpl<ViewMapper, View> implements ViewService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public View createView(CreateViewParam createViewParam) {
        View view = createViewParam.convertTo();
        view.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        view.setCreateBy(currUid);
        view.setUpdateBy(currUid);

        if (!save(view)) {
            throw new ServiceException(ResponseCodeEnum.SAVE_VIEW_RESOURCE_FAIL);
        }

        return getById(view.getId());
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_VIEW_RESOURCE_FAIL);
        }
    }

    @Override
    public View updateView(String id, UpdateViewParam updateViewParam) {
        View view = getViewById(id);
        updateViewParam.update(view);
        view.setUpdateBy(CurrentUser.id());
        view.setUpdateTime(LocalDateTime.now());

        if (!updateById(view)) {
            throw new ServiceException(ResponseCodeEnum.UPDATE_VIEW_RESOURCE_FAIL);
        }
        return getById(id);
    }

    @Override
    public View getViewById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.VIEW_RESOURCE_NOT_FOUND));
    }

    @Override
    public List<View> listByDocId(String docId) {
        Assert.hasText(docId, "docId must has text");
        LambdaQueryWrapper<View> query = new LambdaQueryWrapper<>();
        query.eq(View::getDocId, docId);
        return list(query);
    }
}
