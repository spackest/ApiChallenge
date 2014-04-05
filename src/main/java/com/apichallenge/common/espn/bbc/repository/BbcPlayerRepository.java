package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface BbcPlayerRepository extends JpaRepository<BbcPlayer, Long> {
	BbcPlayer getBbcPlayerByEspnId(int espnId);
}
