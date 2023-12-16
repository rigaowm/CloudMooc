package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分页参数
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {

    //当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;

    @ApiModelProperty("每页记录数")
    //每页记录数默认值
    private Long pageSize =30L;

}
