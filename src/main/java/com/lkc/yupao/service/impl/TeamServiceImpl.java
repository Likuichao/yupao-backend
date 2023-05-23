package com.lkc.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkc.yupao.common.ErrorCode;
import com.lkc.yupao.constant.RedisConstant;
import com.lkc.yupao.constant.UserConstant;
import com.lkc.yupao.exception.BusinessException;
import com.lkc.yupao.mapper.TeamMapper;
import com.lkc.yupao.model.domain.Team;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.model.domain.UserTeam;
import com.lkc.yupao.model.dto.TeamQuery;
import com.lkc.yupao.model.enums.TeamStatusEnum;
import com.lkc.yupao.model.request.TeamJoinRequest;
import com.lkc.yupao.model.request.TeamQuitRequest;
import com.lkc.yupao.model.request.TeamUpdateRequest;
import com.lkc.yupao.model.vo.TeamUserVO;
import com.lkc.yupao.model.vo.UserVO;
import com.lkc.yupao.service.TeamService;
import com.lkc.yupao.service.UserService;
import com.lkc.yupao.service.UserTeamService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.lkc.yupao.constant.RedisConstant.*;

/**
* @author 19733
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-05-14 15:45:43
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class) //开启事务 保证一致性(要么都执行成功，要么都不执行)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //2.是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //获取用户id
        final long userId = loginUser.getId();
        //3.校检信息
        //3.1 队伍人数>1 且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1 || maxNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //3.2 队伍标题<=20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题不满足要求");
        }
        //3.3 描述<512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述不满足要求");
        }
        //3.4 status 是否公开（int） 不传默认0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumValue(status);
        if(statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //3.5 如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum) && (StringUtils.isBlank(password) || password.length()>32)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不满足要求");
        }
        //3.6 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(expireTime == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"过期时间没有设置");
        }
        if(new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间<当前时间");
        }
        //3.7 检验用户最多创建5个队伍
        //TODO bug 可能同时创建100个队伍
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(teamQueryWrapper);
        if(hasTeamNum > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        //3.8 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        //获取队伍id
        final long teamId = team.getId();
        //3.9 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        // 组合查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id>0) {
                queryWrapper.eq("id",id);
            }
            //通过id列表查询
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id",idList);
            }
            //可以通过某个关键词同时对名称和描述查询
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)) {
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)) {
                queryWrapper.like("description",description);
            }
            //查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum",maxNum);
            }
            //根据创建人查询
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0) {
                queryWrapper.eq("userId",userId);
            }
            // 根据状态来查询：是否为管理员，只有管理员能查询不公开的队伍
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnums = TeamStatusEnum.getEnumValue(status);
            if (statusEnums == null) {
                // 如果没设置状态就默认为公开的
                statusEnums = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnums.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (!isAdmin) {
                queryWrapper.and(qw ->
                        qw.and(q -> q.eq("status", TeamStatusEnum.PUBLIC.getValue()).or().eq("status", TeamStatusEnum.SECRET.getValue()))
                );
            }
        }
        //不展示已经过期的队伍 expireTime is null or expireTime > new Date()
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //todo 查询队伍和已加入队伍用户的信息(建议y用SQL，很耗费性能)
        //查询队伍和创建人的信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            //获取创建人id
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            UserVO userVO = new UserVO();
            //脱敏用户信息
            try {
                BeanUtils.copyProperties(teamUserVO,team);
                if(user != null){
                    BeanUtils.copyProperties(userVO,user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //判断请求参数是否为空
        if(teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if(id==null||id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR,"队伍不存在");
        }
        //只有管理员或者队伍的创建者可以修改 ??
        if(oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //todo 如果用户传入的新值和老值一致，就不用update了（可自行实现，降低数据库使用次数）
        //如果队伍状态改为加密，必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)) {
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }
        //更新
        Team updateTeam = new Team();
        try {
            BeanUtils.copyProperties(updateTeam,teamUpdateRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean result = this.updateById(updateTeam);
        return result;
    }

    @Override //注意并发请求时可能出现问题
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {
        if(teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //队伍必须存在
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        //只能加入未过期的队伍
        if(team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR,"队伍已过期");
        }
        //禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"禁止加入私有队伍");
        }
        //如果队伍是加密的，必须和密码匹配才可以
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
        Long userId = loginUser.getId();
        //不能加入自己的队伍
        if(team.getUserId()==userId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能重复加入自己的队伍");
        }
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");
        boolean result = false;
        try {
            while(true) {//每个用户都要抢到锁，所以while(true)
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {//写锁  l:等待时间(等待时间0，拿到锁就执行，拿不到就走)  l1:锁的过期时间（锁到达时间就过期） 看门狗机制
                    /*notice 需要数据库查询的条件放在后边查询*/
                    //用户最多加入5个队伍 （获取某个人加入队伍数量）
                    long hasJoinNum = this.countUserTeamByUserId(userId);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多加入和创建五个队伍");
                    }
                    //只能加入未满的队伍 （获取某个队伍当前人数）
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum > team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //不能重复加入已加入的队伍（幂等性）
                    boolean isJoinTeam = this.isJoinTeam(userId, teamId);
                    if (isJoinTeam) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    //新增用户-队伍关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    result = userTeamService.save(userTeam);
                    return result;
                }
            }
        }catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.debug("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)//如果有多个数据库操作，记得加注解
    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //校验请求参数
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //检验是否加入队伍
        Long userId = loginUser.getId();
        boolean isJoinTeam = this.isJoinTeam(userId, teamId);
        if(!isJoinTeam) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        //获取某个队伍当前人数
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        //获取用户-队伍关系
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId).eq("userId",userId);
        //队伍只剩一人
        if(teamHasJoinNum == 1) {
            //删除队伍和用户-队伍的关系
            this.removeById(teamId);
        } else {
            //如果是队长退出队伍，权限转移给第二早加入的用户一先来后到(只取id最小的两条数据)
            if(team.getUserId() == userId) {
                //查询已经加入队伍所有用户和加入时间
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");//SQL拼接在最后
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size()<=1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserrId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserrId);
                boolean result = this.updateById(updateTeam);
                if(!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
            }
        }
        //非队长，自己退出队伍(删除加入队伍的用户-队伍关系)
        //删除加入队伍的用户-队伍关系
        return userTeamService.remove(userTeamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)//队伍和用户队伍表都要删除，事务可以保证要么都删除要么都不删除
    public boolean deleteTeam(long id, User loginUser) {
        //检验队伍
        Team team = getTeamById(id);
        long userId = team.getUserId();
        //坑：如果一个包装类和一个基本数据类型比较，会把基本数据类型转成包装类，进而比较地址
        if(userId!=loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH,"不是队长,无访问权限");
        }
        //移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",id);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //删除队伍
        return this.removeById(id);
    }

    /**
     * 根据id获取队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if(teamId==null || teamId<=0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验队伍是否存在
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 获取某个队伍当前人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 获取某个人加入队伍数量
     * @param userId
     * @return
     */
    private long countUserTeamByUserId(long userId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 是否加入队伍
     * @param userId
     * @param teamId
     * @return
     */
    private boolean isJoinTeam(long userId, long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        userTeamQueryWrapper.eq("userId",userId);
        long count =  userTeamService.count(userTeamQueryWrapper);
        if(count>0) {
            return true;
        }
        return false;
    }

}




