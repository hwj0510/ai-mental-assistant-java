package com.heima.aimentalassistant.pojo.dto;

import lombok.Data;

@Data
public class EmotionDiaryQueryDTO {
    /** 默认 1 */
    private Integer pageNum = 1;
    /** 默认 10 */
    private Integer pageSize = 10;
    /** YYYY-MM-DD，为空则不限 */
    private String startDate;
    /** YYYY-MM-DD，为空则不限 */
    private String endDate;
}
