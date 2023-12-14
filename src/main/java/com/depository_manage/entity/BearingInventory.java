package com.depository_manage.entity;

import lombok.Data;

@Data
public class BearingInventory {
    private String boxText;
    private String boxNumber;
    private int quantityInStock;
    private String operationType;
    private int depositoryId; // 将 depositoryId 改为 String 类型

    // Lombok @Data 注解将自动生成构造方法、getter 和 setter
}
