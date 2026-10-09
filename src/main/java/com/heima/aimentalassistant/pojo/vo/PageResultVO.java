package com.heima.aimentalassistant.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResultVO<T> {
    /** 总记录数 */
    private Long total;
    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResultVO<T> of(List<T> records, Long total) {
        return PageResultVO.<T>builder()
                .total(total)
                .records(records)
                .build();
    }
}