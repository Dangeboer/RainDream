package com.dangeboer.raindream.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dangeboer.raindream.mapper.PltMapper;
import com.dangeboer.raindream.model.entity.Plt;
import com.dangeboer.raindream.service.PltService;
import org.springframework.stereotype.Service;

@Service
public class PltServiceImpl extends ServiceImpl<PltMapper, Plt> implements PltService {
}
