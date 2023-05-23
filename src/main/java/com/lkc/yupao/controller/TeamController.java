package com.lkc.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lkc.yupao.common.BaseResponse;
import com.lkc.yupao.common.ErrorCode;
import com.lkc.yupao.common.ResultUtils;
import com.lkc.yupao.exception.BusinessException;
import com.lkc.yupao.model.domain.Team;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.model.domain.UserTeam;
import com.lkc.yupao.model.dto.TeamQuery;
import com.lkc.yupao.model.request.*;
import com.lkc.yupao.model.vo.TeamUserVO;
import com.lkc.yupao.service.TeamService;
import com.lkc.yupao.service.UserService;
import com.lkc.yupao.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;


/**
 *
 * @author lkc
 * @version 1.0
 */
@RestController
@RequestMapping("/team")
//后端解决跨域问题
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials ="true")
@Slf4j //打印日志
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        //teamAddRequest赋值给team
        try {
            BeanUtils.copyProperties(team,teamAddRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //增加队伍
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest==null||deleteRequest.getId()<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(deleteRequest.getId(),loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,admin);
        //判断当前user加入哪些team
        this.joinTeams(teamList,loginUser);
        //查询加入队伍的用户信息（人数）
        this.joinUserInfo(teamList);
        return ResultUtils.success(teamList);
    }

    //todo 查询分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPages(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        Team team = new Team();
        try {
            //导入工具类commons-beanutils 把一个对象copy给目标对象
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Page page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if(teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if(teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 查询创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/create")
    public BaseResponse<List<TeamUserVO>> listCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,admin);
        this.joinTeams(teamList,loginUser);
        //查询加入队伍的用户信息（人数）
        this.joinUserInfo(teamList);
        return ResultUtils.success(teamList);
    }

    /**
     *查询已加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/join")
    public BaseResponse<List<TeamUserVO>> listJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAM_NULL_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        //设置查询
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        //取出不重复的队伍id ( 1,2  1,3 2,3  -> 1->2,3  2->3)
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,admin);
        this.joinTeams(teamList,loginUser);
        //查询加入队伍的用户信息（人数）
        this.joinUserInfo(teamList);
        return ResultUtils.success(teamList);
    }

    /**
     * 判断当前user加入哪些team
     * @param teamList
     * @param loginUser
     */
    private void joinTeams(List<TeamUserVO> teamList, User loginUser){
        //判断当前用户是否已加入队伍(便于前端显示加入队伍按钮)
        //1.获取当前用户已经加入的队伍id集合
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            //2.当前用户加入的队伍是否出现在查询出来的队伍中teamQuery
            teamList.forEach(team->{
                boolean hashJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hashJoin);
            });
        } catch (Exception e) {

        }
    }

    /**
     * 查询加入队伍的用户信息（人数）
     * @param teamList
     */
    private void joinUserInfo(List<TeamUserVO> teamList) {
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        //队伍id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team->
                team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size()));//getOrDefault如果team.getId()为空，getOrDefault的值为,new ArrayList<>()
    }

}

