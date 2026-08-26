package com.depository_manage.controller;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.InventoryInfo;
import com.depository_manage.exception.InventoryOperationException;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.security.bean.UserToken;
import com.depository_manage.service.BearingInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bearingInventory")
public class BearingInventoryController {

    @Autowired
    private BearingInventoryService bearingInventoryService;

    // API端点，例如获取库存、更新库存等
    @PostMapping("/add")
    public ResponseEntity<?> addBearingInventory(@RequestBody BearingInventory bearingInventory) {
        bearingInventoryService.addBearingInventory(bearingInventory);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{boxText}")
    public ResponseEntity<BearingInventory> getBearingInventory(@PathVariable String boxText) {
        BearingInventory bearingInventory = bearingInventoryService.getBearingInventory(boxText);
        if (bearingInventory != null) {
            return ResponseEntity.ok(bearingInventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/stockIn")
    public ResponseEntity<?> stockIn(@RequestBody BearingInventory inventory, HttpServletRequest request) {
        ResponseEntity<?> depositoryValidation = bindLoggedInDepository(inventory, request);
        if (depositoryValidation != null) {
            return depositoryValidation;
        }
        try {
            bearingInventoryService.stockIn(inventory);
            return ResponseEntity.ok(Collections.singletonMap("message", "Stock-in successful"));
        } catch (OperationAlreadyDoneException e) {
            // 返回一个错误响应
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (InventoryOperationException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("transactionRolledBack", true);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }
    @PostMapping("/stockTransferIn")
    public ResponseEntity<?> stockTransferIn(@RequestBody BearingInventory inventory) {
        try {
            bearingInventoryService.tranIn(inventory); // 注意这里调用的是tranIn方法
            return ResponseEntity.ok(Collections.singletonMap("message", "Transfer-in successful"));
        } catch (OperationAlreadyDoneException | IllegalStateException e) {
            // 返回一个错误响应
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (InventoryOperationException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("transactionRolledBack", true);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }
    @PostMapping("/stockOut")
    public ResponseEntity<?> stockOut(@RequestBody BearingInventory inventory, HttpServletRequest request) {
        ResponseEntity<?> depositoryValidation = bindLoggedInDepository(inventory, request);
        if (depositoryValidation != null) {
            return depositoryValidation;
        }
        try {
            bearingInventoryService.stockOut(inventory);
            return ResponseEntity.ok(Collections.singletonMap("message", "Stock-out successful"));
        } catch (OperationAlreadyDoneException e) {
            // 返回一个错误响应
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (InventoryOperationException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("transactionRolledBack", true);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }

    /**
     * 库存操作必须以登录账号所属仓库为准。请求体中的 depositoryId 属于客户端输入，
     * 不能决定库存落库仓；扫码页仍可用它保留二维码的来源仓记录。
     */
    private ResponseEntity<?> bindLoggedInDepository(BearingInventory inventory, HttpServletRequest request) {
        UserToken userToken = (UserToken) request.getAttribute("userToken");
        if (userToken == null || userToken.getUser() == null || userToken.getUser().getDepositoryId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "未获取到登录账号的仓库信息"));
        }
        int depositoryId = userToken.getUser().getDepositoryId();
        if (depositoryId != 1 && depositoryId != 2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("message", "当前账号未绑定可操作仓库"));
        }
        inventory.setDepositoryId(depositoryId);
        return null;
    }
    @PostMapping("/stockOutForPC")
    public ResponseEntity<?> stockOutForPC(@RequestBody BearingInventory inventory) {
        try {
            bearingInventoryService.stockOutForPC(inventory);
            return ResponseEntity.ok(Collections.singletonMap("message", "Stock-out successful"));
        } catch (OperationAlreadyDoneException e) {
            // 返回一个错误响应
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (InventoryOperationException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", e.getMessage());
            body.put("transactionRolledBack", true);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }
    @GetMapping("/panKu/{boxText}/{boxNumber}/{depositoryId}")
    public ResponseEntity<?> panKu(@PathVariable String boxText,
                                   @PathVariable String boxNumber,
                                   @PathVariable int depositoryId) {
        try {
            // 这里调用service层方法获取盘库相关的数据
            InventoryInfo inventoryInfo = bearingInventoryService.getInventoryInfo(boxText, boxNumber, depositoryId);
            System.out.println(inventoryInfo);
            if (inventoryInfo != null) {
                return ResponseEntity.ok(inventoryInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}
