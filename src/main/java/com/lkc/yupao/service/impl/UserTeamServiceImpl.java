package com.lkc.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkc.yupao.mapper.UserTeamMapper;
import com.lkc.yupao.model.domain.UserTeam;
import com.lkc.yupao.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 19733
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-05-14 15:48:52
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




