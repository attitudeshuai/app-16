package com.commutecarpool.dto.restriction;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionSubmitAppealRequest {

    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;

    private String appealMaterial;
}
