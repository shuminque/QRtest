package com.depository_manage.entity;

import lombok.Data;

@Data
public class BoxNumberGap {
    private int iter;
    private String beforeNumber;
    private String afterNumber;
    private String missingNumber;
}
