package com.dangeboer.raindream.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OssPresignForm implements Serializable {
    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;

    @NotNull
    private Long size;

    private Integer mediaType;
}
