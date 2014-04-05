package com.apichallenge.common.espn.bbc.repository;

import com.apichallenge.common.espn.bbc.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface BbcTeamRepository extends JpaRepository<BbcTeam, Long> {
	BbcTeam findByName(String name);

	BbcTeam findByShortName(String shortName);

	BbcTeam findByScheduleName(String scheduleName);
}
