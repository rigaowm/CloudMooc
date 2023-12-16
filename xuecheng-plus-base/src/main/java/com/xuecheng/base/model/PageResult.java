package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Rigao
 * @Title: PageResult
 * @Date: 2023/12/16 20:13
 * @Version 1.0
 * @Description: 分页查询结果
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    // 数据列表
    private List<T> items;

    //总记录数
    private long counts;

    //当前页码
    private long page;

    //每页记录数
    private long pageSize;

}
