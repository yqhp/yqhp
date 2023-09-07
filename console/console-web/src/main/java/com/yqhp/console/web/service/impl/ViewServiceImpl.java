/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
    public View createView(CreateViewParam param) {
        View view = param.convertTo();
        view.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        view.setCreateBy(currUid);
        view.setUpdateBy(currUid);

        if (!save(view)) {
            throw new ServiceException(ResponseCodeEnum.SAVE_VIEW_FAILED);
        }

        return getById(view.getId());
    }

    @Override
    public void deleteById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_VIEW_FAILED);
        }
    }

    @Override
    public View updateView(String id, UpdateViewParam param) {
        View view = getViewById(id);
        param.update(view);
        view.setUpdateBy(CurrentUser.id());
        view.setUpdateTime(LocalDateTime.now());

        if (!updateById(view)) {
            throw new ServiceException(ResponseCodeEnum.UPDATE_VIEW_FAILED);
        }
        return getById(id);
    }

    @Override
    public View getViewById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.VIEW_NOT_FOUND));
    }

    @Override
    public List<View> listByDocId(String docId) {
        Assert.hasText(docId, "docId must has text");
        LambdaQueryWrapper<View> query = new LambdaQueryWrapper<>();
        query.eq(View::getDocId, docId);
        return list(query);
    }
}
