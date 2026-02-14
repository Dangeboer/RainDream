package com.dangeboer.raindream.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dangeboer.raindream.model.entity.Plt;
import com.dangeboer.raindream.model.form.PltForm;
import com.dangeboer.raindream.model.vo.PltVO;

import java.util.List;

public interface PltService extends IService<Plt> {
    List<PltVO> getPlt(Long userId);
    Long createPlt(Long userId, PltForm pltForm);
    Long updatePlt(Long userId, Long pltId, PltForm pltForm, Boolean force);
    Long deletePlt(Long userId, Long pltId);
}
