package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.OssPresignForm;
import com.dangeboer.raindream.model.vo.OssPresignVO;
import com.dangeboer.raindream.service.OssService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OssController {
    private final OssService ossService;

    @PostMapping("/presign")
    public OssPresignVO presign(@AuthenticationPrincipal User user, @Valid @RequestBody OssPresignForm form) {
        return ossService.presignPutUrl(user.getId(), form);
    }

    @DeleteMapping("/object")
    public Boolean deleteObject(@AuthenticationPrincipal User user, @RequestParam("storeUrl") String storeUrl) {
        ossService.deleteObject(user.getId(), storeUrl);
        return true;
    }
}
