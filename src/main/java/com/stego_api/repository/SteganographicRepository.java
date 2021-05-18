package com.stego_api.repository;

import com.stego_api.entity.SteganographicEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SteganographicRepository extends CrudRepository<SteganographicEntity, Long> {
    List<SteganographicEntity> findByName(String name);
}
