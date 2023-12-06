package com.depository_manage.mapper;

import com.depository_manage.entity.ProductId;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductIdMapper {
    ProductId selectBoxNumberByBoxTextAndDepositoryId(
            @Param("boxText") String boxText,
            @Param("depositoryId") int depositoryId
    );
    List<ProductId> selectAllBoxNumbers();
    void insertOrUpdateBoxNumber(ProductId productId);
}
