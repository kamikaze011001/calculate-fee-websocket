package org.aibles.cal_eos_fee.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class Action {

    private String account;
    private String name;
    private List<PermissionLevel> authorization;
    private String data;
}
