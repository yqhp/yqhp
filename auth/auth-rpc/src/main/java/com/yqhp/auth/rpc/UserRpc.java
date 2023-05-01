package com.yqhp.auth.rpc;

import com.yqhp.auth.model.dto.UserInfo;
import com.yqhp.auth.model.param.UpdateUserParam;
import com.yqhp.auth.model.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangyitao
 */
@FeignClient(name = "auth-service", path = "/auth/user", contextId = "user")
public interface UserRpc {
    @PostMapping("/changePassword")
    void changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword);

    @GetMapping("/info")
    UserInfo info();

    @PutMapping
    void updateUser(@RequestBody UpdateUserParam updateUserParam);

    @GetMapping("/{userId}")
    UserVO getVOById(@PathVariable("userId") String userId);

    @PostMapping("/users")
    List<UserVO> listVOByIds(@RequestBody Set<String> userIds);

    @PostMapping("/usersMap")
    Map<String, UserVO> getVOMapByIds(@RequestBody Set<String> userIds);
}
