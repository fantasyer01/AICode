package com.ithistory.repository;

import com.ithistory.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {
    
    Optional<Configuration> findByConfigKey(String configKey);
}
