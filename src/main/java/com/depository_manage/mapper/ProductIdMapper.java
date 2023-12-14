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
    ProductId selectProductIdByBoxTextAndDepositoryId(@Param("boxText") String boxText, @Param("boxNumber") String boxNumber, @Param("depositoryId") int depositoryId);
    void updateStockedStatus(@Param("boxText") String boxText,
                             @Param("boxNumber") String boxNumber,
                             @Param("depositoryId") int depositoryId,
                             @Param("isStocked") int isStocked);

    // 检查产品是否已入库的方法
    Integer checkIfStocked(@Param("boxText") String boxText,
                           @Param("boxNumber") String boxNumber,
                           @Param("depositoryId") int depositoryId);
}
