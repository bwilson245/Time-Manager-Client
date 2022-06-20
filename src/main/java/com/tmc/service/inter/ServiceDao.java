package com.tmc.service.inter;

import com.tmc.model.TypeEnum;

import java.util.List;

public interface ServiceDao<O, X, Y> {
    List<O> search(TypeEnum type, String id, String name, String email, Boolean isActive);
    List<O> search(TypeEnum type, String id, String workType, String department, String orderNum, Long before,
                   Long after, Boolean complete, Boolean validated);
    O create(X request);
    O edit(String id, Y request);
    O deactivate(String id);
}
