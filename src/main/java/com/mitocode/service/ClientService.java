package com.mitocode.service;

import com.mitocode.entity.ClientEntity;
import com.mitocode.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repo;

    public List<ClientEntity> findAll() { return repo.findAll(); }
    public ClientEntity findById(Integer id) { return repo.findById(id).orElseThrow(); }
    public ClientEntity save(ClientEntity c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
