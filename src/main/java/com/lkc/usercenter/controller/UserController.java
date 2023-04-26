package com.lkc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lkc.usercenter.common.BaseResponse;
import com.lkc.usercenter.common.ErrorCode;
import com.lkc.usercenter.common.ResultUtils;
import com.lkc.usercenter.exception.BusinessException;
import com.lkc.usercenter.model.User;
import com.lkc.usercenter.model.request.UserLoginRequest;
import com.lkc.usercenter.model.request.UserResisterRequest;
import com.lkc.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.lkc.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.lkc.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author lkc
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserResisterRequest userResisterRequest){
        if(userResisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount=userResisterRequest.getUserAccount();
        String userPassword=userResisterRequest.getUserPassword();
        String checkPassword=userResisterRequest.getCheckPassword();
        String planetCode=userResisterRequest.getPlanetCode();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        Long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest==null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request){
        if(request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userService.userLogout(request);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list();
        List<User> list = userList.stream().map(user ->
                userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser =(User) userObj;
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = currentUser.getId();
        //todo校验用户是否合法
        User user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    private boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user!=null && user.getUserRole()==ADMIN_ROLE;
    }
}
