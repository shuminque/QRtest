package com.depository_manage.entity;

import lombok.Data;

@Data
public class BearingInventory {
    private String boxText;
    private String boxNumber;
    private int quantityInStock;
    private String operationType;
    // 构造方法、getter 和 setter
}
