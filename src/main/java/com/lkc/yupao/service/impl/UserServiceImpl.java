package com.lkc.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkc.yupao.common.ErrorCode;
import com.lkc.yupao.exception.BusinessException;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.model.vo.UserVO;
import com.lkc.yupao.service.UserService;
import com.lkc.yupao.mapper.UserMapper;
import com.lkc.yupao.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lkc.yupao.constant.UserConstant.ADMIN_ROLE;
import static com.lkc.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 19733
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-04-10 21:12:50
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 混淆密码
     */
    private static final String SALT = "lkc";

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8||checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //账户不能包含特殊字符
        String regEx =  ".*[\\s`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+.*";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号包含特殊字符");
        }
        //密码不同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //星球编号不重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            //todo 修改为自定义异常
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //账户不能包含特殊字符
        String regEx =  ".*[\\s`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+.*";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if(user==null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //登录尝试次数过多，如何处理？
        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser==null) return null;
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(0);
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 内存查询版
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){
        //判断传入参数是否为空
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //第二种方式 内存查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //先查询所有用户  连接数据库也需要一定的时间
        List<User> userList = userMapper.selectList(queryWrapper);
        //序列化
        Gson gson = new Gson();
        //在内存判断是否包含要求的标签  |  filter为false表示该user被过滤掉，true被保留
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            //如果该user没有标签，返回false被过滤
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            //java-->json
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            //对集合判空
            //ofNullable封装一个可能为空的对象，如果是空对象，则使用orElse中的对象给tempTagNameSet默认值
            Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if(userId == null || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //todo 补充校验， 如果用户没有传递任何更新的值，就直接报错，不执行更新

        //如果是管理员，允许更新任意用户
        //user=前端传递的修改的用户信息
        //如果不是管理员，只允许更新自己
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw  new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        //获取原始用户信息
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        //根据user的id更新用户信息
        return userMapper.updateById(user);
    }

    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        //获取用户登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ////判断是否为空
        if(userObj == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //获取用户登录态
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user!=null && user.getUserRole()==ADMIN_ROLE;
    }


    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        //不为空且为管理员
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取最匹配的用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //只查需要的数据（id，tags） 时间优化
        userQueryWrapper.select("id","tags");
        //查询标签不为空的数据  时间优化
        userQueryWrapper.isNotNull("tags");
        List<User> userList = this.list(userQueryWrapper);
        String tags = loginUser.getTags();
        //json=>List<String>
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>(){}.getType());
        //用户列表的下标=>相似度
        List<Pair<User,Long>> list = new ArrayList<>();
        for(int i=0;i<userList.size();i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //如果无标签或者为用户自己
            if(StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            long minDistance = AlgorithmUtils.minDistance(tagList, userTagsList);
            list.add(new Pair<>(user,minDistance));
        }
        //取出前num条数据（下标）按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //取出前num个用户id（pair转成只有pair的key的方法）
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        //查询用户信息
        QueryWrapper<User> userQueryWrapper1 = new QueryWrapper<>();
        userQueryWrapper1.in("id",userIdList);//in取出的数据不按照id顺序,因此要排序！！！
        //脱敏
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper1)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        //按照原始顺序取出用户
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户 SQL查询版
     * @param tagNameList
     * @return
     */
    @Deprecated
    private List<User> searchUserByTagsBySQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //第一种方式 SQL查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接 and查询
        //like '%java%' and like '%php%'
        for (String tagName : tagNameList) {
            queryWrapper=queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());//用户脱敏，防止密码泄露
    }
}




