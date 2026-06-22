package com.commutecarpool.dto.emergencycontact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名不能超过50个字符")
    private String contactName;

    @NotBlank(message = "联系人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String contactPhone;

    @NotBlank(message = "与联系人关系不能为空")
    @Size(max = 20, message = "关系描述不能超过20个字符")
    private String relationship;
}
