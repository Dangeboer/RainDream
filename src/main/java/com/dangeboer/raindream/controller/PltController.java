package com.dangeboer.raindream.controller;

import com.dangeboer.raindream.model.entity.User;
import com.dangeboer.raindream.model.form.PltForm;
import com.dangeboer.raindream.model.vo.PltVO;
import com.dangeboer.raindream.service.PltService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plt")
@RequiredArgsConstructor
public class PltController {
    private final PltService pltService;

    @GetMapping
    public List<PltVO> getPlt(@AuthenticationPrincipal User user) {
        return pltService.getPlt(user.getId());
    }

    @PostMapping("/create")
    public Long createPlt(@AuthenticationPrincipal User user, @RequestBody PltForm pltForm) {
        return pltService.createPlt(user.getId(), pltForm);
    }

    @PutMapping("/update/{pltId}")
    public Long updatePlt(@AuthenticationPrincipal User user, @PathVariable Long pltId, @RequestBody PltForm pltForm) {
        return pltService.updatePlt(user.getId(), pltId, pltForm);
    }

    @DeleteMapping("/delete/{pltId}")
    public Long deletePlt(@AuthenticationPrincipal User user, @PathVariable Long pltId) {
        return pltService.deletePlt(user.getId(), pltId);
    }
}
