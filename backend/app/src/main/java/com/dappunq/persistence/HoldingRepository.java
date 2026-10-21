package com.dappunq.persistence; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface HoldingRepository extends JpaRepository<HoldingEntity,Long>{List<HoldingEntity> findByUserId(Long id); Optional<HoldingEntity> findByUserIdAndPlayerId(Long u,Long p);}
