package com.dangeboer.raindream.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class OssPresignVO implements Serializable {
    private String uploadUrl;
    private String objectKey;
    private String fileUrl;
}
