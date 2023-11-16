package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.service.BearingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bearings")
public class BearingController {

    @Autowired
    private BearingService bearingService;

    @GetMapping("/{boxNumber}")
    public ResponseEntity<Bearing> getBearingByBoxNumber(@PathVariable String boxNumber) {
        Bearing bearing = bearingService.getBearingByBoxNumber(boxNumber);
        if (bearing != null) {
            return ResponseEntity.ok(bearing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Bearing>> getAllBearings() {
        List<Bearing> bearings = bearingService.getAllBearings();
        if (bearings != null && !bearings.isEmpty()) {
            return ResponseEntity.ok(bearings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
