package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.converter.PltConverter;
import com.dangeboer.raindream.exception.BadRequestException;
import com.dangeboer.raindream.exception.CanNotFoundException;
import com.dangeboer.raindream.exception.ForbiddenException;
import com.dangeboer.raindream.mapper.PltMapper;
import com.dangeboer.raindream.model.entity.Plt;
import com.dangeboer.raindream.model.form.PltForm;
import com.dangeboer.raindream.model.vo.PltVO;
import com.dangeboer.raindream.service.PltService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PltServiceImpl extends ServiceImpl<PltMapper, Plt> implements PltService {
    private final PltMapper pltMapper;
    private final PltConverter pltConverter;

    @Override
    public List<PltVO> getPlt(Long userId) {
        List<Plt> plts = pltMapper.selectList(new LambdaQueryWrapper<Plt>()
                .eq(Plt::getUserId, userId));

        if (plts == null || plts.isEmpty()) {
            throw new CanNotFoundException();
        }
        return plts.stream().map(pltConverter::toVO).toList();
    }

    @Override
    public Long createPlt(Long userId, PltForm pltForm) {
        if (pltForm == null || pltForm.getName() == null) {
            throw new BadRequestException();
        }
        Plt plt = new Plt(userId, pltForm.getName());
        return (long) pltMapper.insert(plt);
    }

    @Override
    public Long updatePlt(Long userId, Long pltId, PltForm pltForm) {
        Plt plt = pltMapper.selectOne(new LambdaQueryWrapper<Plt>().eq(Plt::getId, pltId));

        if (plt == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(plt.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        Plt toUpdate = new Plt(pltId, pltForm.getName());

        pltMapper.updateById(toUpdate);
        return pltId;
    }

    @Override
    public Long deletePlt(Long userId, Long pltId) {
        Plt plt = pltMapper.selectOne(new LambdaQueryWrapper<Plt>().eq(Plt::getId, pltId));
        if (plt == null) {
            throw new CanNotFoundException();
        } else if (!Objects.equals(plt.getUserId(), userId)) {
            throw new ForbiddenException();
        }

        return (long) pltMapper.deleteById(pltId);
    }
}
