package com.commutecarpool.dto.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationReviewRequest {

    @NotBlank(message = "审核状态不能为空")
    private String status;

    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
