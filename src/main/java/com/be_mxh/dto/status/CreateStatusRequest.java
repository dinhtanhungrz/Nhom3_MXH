package com.be_mxh.dto.status;

import com.be_mxh.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateStatusRequest {

    @NotBlank
    @Size(max = 3000)
    private String content;

    private Status.Visibility visibility = Status.Visibility.PUBLIC;
}