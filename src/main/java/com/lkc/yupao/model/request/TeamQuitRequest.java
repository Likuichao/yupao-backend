package com.lkc.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lkc
 * @version 1.0
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 1298260117822768170L;

    /**
     * 队伍id
     */
    private Long teamId;
}
