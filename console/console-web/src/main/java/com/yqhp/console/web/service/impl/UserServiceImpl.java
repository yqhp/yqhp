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

import com.yqhp.auth.model.vo.UserVO;
import com.yqhp.auth.rpc.UserRpc;
import com.yqhp.console.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRpc userRpc;

    @Override
    public UserVO getVOById(String id) {
        Assert.hasText(id, "id must has text");
        return userRpc.getVOById(id);
    }
}
