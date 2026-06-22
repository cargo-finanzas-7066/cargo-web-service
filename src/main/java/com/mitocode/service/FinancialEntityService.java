package com.mitocode.service;

import com.mitocode.entity.FinancialEntityEntity;
import com.mitocode.repository.FinancialEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialEntityService {
    private final FinancialEntityRepository repo;

    public List<FinancialEntityEntity> findAll() { return repo.findAll(); }
    public FinancialEntityEntity findById(Integer id) { return repo.findById(id).orElseThrow(); }
    public FinancialEntityEntity save(FinancialEntityEntity e) { return repo.save(e); }
    public void delete(Integer id) { repo.deleteById(id); }
}
