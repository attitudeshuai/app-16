package com.commutecarpool.dto.restriction;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionReviewAppealRequest {

    @NotBlank(message = "审核结果不能为空")
    private String appealStatus;

    private String remark;
}
