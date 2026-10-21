package com.dappunq.persistence; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface ValuationHistoryRepository extends JpaRepository<ValuationHistoryEntity,Long>{List<ValuationHistoryEntity> findByUserIdOrderByEjecutadoEnDesc(Long id);}
