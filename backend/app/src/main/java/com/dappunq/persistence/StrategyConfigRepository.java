package com.dappunq.persistence; import com.dappunq.model.valuation.*; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface StrategyConfigRepository extends JpaRepository<StrategyConfigEntity,Long>{Optional<StrategyConfigEntity> findFirstByUserIdAndEstado(Long id,StrategyStatus e);}
