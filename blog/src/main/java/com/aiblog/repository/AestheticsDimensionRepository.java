package com.aiblog.repository;

import com.aiblog.model.AestheticsDimension;
import com.aiblog.model.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AestheticsDimensionRepository extends JpaRepository<AestheticsDimension, Long> {

    List<AestheticsDimension> findByTypeOrderByDisplayOrderAscNameAsc(DimensionType type);

    List<AestheticsDimension> findAllByOrderByTypeAscDisplayOrderAscNameAsc();

    boolean existsByTypeAndName(DimensionType type, String name);
}
