package com.depository_manage.entity;

import lombok.Data;

@Data
public class MaterialState {
    private Integer id;
    private String sname;

    public Integer getId() {
        return id;
    }

    public String getSname() {
        return sname;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }
}
