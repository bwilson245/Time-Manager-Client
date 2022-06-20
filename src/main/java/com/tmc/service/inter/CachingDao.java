package com.tmc.service.inter;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CachingDao<O> {
    O get(String id);
    List<O> get(List<String> ids);
}
