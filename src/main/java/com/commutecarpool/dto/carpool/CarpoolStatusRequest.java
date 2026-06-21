package com.commutecarpool.dto.carpool;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolStatusRequest {

    @NotBlank
    private String status;
}
