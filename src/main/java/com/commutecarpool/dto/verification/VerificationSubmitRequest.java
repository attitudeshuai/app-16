package com.commutecarpool.dto.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerificationSubmitRequest {

    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFront;

    @NotBlank(message = "身份证反面照片不能为空")
    private String idCardBack;

    @NotBlank(message = "驾驶证正面照片不能为空")
    private String drivingLicenseFront;

    @NotBlank(message = "驾驶证反面照片不能为空")
    private String drivingLicenseBack;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度应在2-20个字符之间")
    private String realName;

    @NotBlank(message = "身份证号码不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号码格式不正确")
    private String idCardNumber;

    @NotBlank(message = "驾驶证号码不能为空")
    @Size(min = 10, max = 20, message = "驾驶证号码长度应在10-20个字符之间")
    private String drivingLicenseNumber;
}
