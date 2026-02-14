package com.dangeboer.raindream.service;

import com.dangeboer.raindream.model.form.OssPresignForm;
import com.dangeboer.raindream.model.vo.OssPresignVO;

public interface OssService {
    OssPresignVO presignPutUrl(Long userId, OssPresignForm form);

    String presignReadUrl(Long userId, String storeUrl);
}
