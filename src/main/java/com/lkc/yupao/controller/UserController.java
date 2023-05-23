package com.lkc.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lkc.yupao.common.BaseResponse;
import com.lkc.yupao.common.ErrorCode;
import com.lkc.yupao.common.ResultUtils;
import com.lkc.yupao.exception.BusinessException;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.model.request.UserLoginRequest;
import com.lkc.yupao.model.request.UserResisterRequest;
import com.lkc.yupao.model.vo.UserVO;
import com.lkc.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lkc.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 * @author lkc
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
//后端解决跨域问题
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials ="true")
@Slf4j //打印日志
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;//spring data redis操作redis

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
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
        if(!userService.isAdmin(request)){
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

    //todo 推荐多个未实现
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        List<User> list = null;
        //如果有缓存，直接读缓存
        User loginUser = userService.getLoginUser(request);

        String redisKey = String.format("yupao:user:recommend:%s:%s", loginUser.getId(),pageSize);//建议格式:systemld:moduleld:func:options
        //操作字符串
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if(userPage != null){
            //用户信息脱敏 【getRecords()获取用户列表  setRecords(list)设置脱敏后的用户列表】
            list = userPage.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
            userPage.setRecords(list);
            return ResultUtils.success(userPage);
        }
        //如果无缓存,查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //分页查询
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //用户信息脱敏
        list = userPage.getRecords().stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        userPage.setRecords(list);
        //写缓存(redis)
        //使用try-catch可以捕获错误，同时也可以将查询的结果返回给前端（虽然reid没有存储成功）
        try {
            valueOperations.set(redisKey,userPage,50000, TimeUnit.MILLISECONDS);//同时设置过期时间为50s
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        List<User> userList = userService.searchUserByTags(tagNameList);
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if(!userService.isAdmin(request)){
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
        //获取用户的登录态
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

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        //1.校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取登录用户信息
        User loginUser = userService.getLoginUser(request);
        //检验权限，触发更新
        Integer result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取最匹配的用户
     * @param pageSize
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long pageSize, HttpServletRequest request) {
        if(pageSize<=0||pageSize>20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求匹配数量过大");
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(pageSize,loginUser));
    }

}
