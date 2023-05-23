package com.lkc.yupao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lkc.yupao.model.domain.Team;
import com.lkc.yupao.model.domain.User;
import com.lkc.yupao.model.dto.TeamQuery;
import com.lkc.yupao.model.request.TeamJoinRequest;
import com.lkc.yupao.model.request.TeamQuitRequest;
import com.lkc.yupao.model.request.TeamUpdateRequest;
import com.lkc.yupao.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 19733
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-05-14 15:45:43
*/
public interface TeamService extends IService<Team> {

    /**
     * 增加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id,User loginUser);
}
