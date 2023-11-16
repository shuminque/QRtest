package com.depository_manage.entity;

import lombok.Data;

@Data
public class BearingInventory {
    private String boxNumber;
    private String productId;
    private int quantityInStock;

    // 构造方法、getter 和 setter
}
