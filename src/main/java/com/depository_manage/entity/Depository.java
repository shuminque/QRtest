package com.depository_manage.entity;

import lombok.Data;

public class Depository {
    /** 版本号 */
    private static final long serialVersionUID = -2259445638130429647L;

    /** id */
    private Integer id;

    /** 仓库名称 */
    private String dname;

    /** 仓库地址 */
    private String address;

    /** 仓库介绍 */
    private String introduce;

    /**
     * 获取id
     * 
     * @return id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * 设置id
     * 
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取仓库名称
     * 
     * @return 仓库名称
     */
    public String getDname() {
        return this.dname;
    }

    /**
     * 设置仓库名称
     * 
     * @param dname
     *          仓库名称
     */
    public void setDname(String dname) {
        this.dname = dname;
    }

    /**
     * 获取仓库地址
     * 
     * @return 仓库地址
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * 设置仓库地址
     * 
     * @param address
     *          仓库地址
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取仓库介绍
     * 
     * @return 仓库介绍
     */
    public String getIntroduce() {
        return this.introduce;
    }

    /**
     * 设置仓库介绍
     * 
     * @param introduce
     *          仓库介绍
     */
    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }


}