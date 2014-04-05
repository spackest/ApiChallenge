package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class BbcGameService {
	@Autowired
	BbcGameRepository bbcGameRepository;

	public boolean gameExists(int espnGameId) {
		BbcGame bbcGame = bbcGameRepository.getBbcGameByEspnGameId(espnGameId);
		return bbcGame != null;
	}

	public boolean gameExists(Date date, BbcTeam homeTeam, BbcTeam awayTeam, int gameNumber) {
		BbcGame bbcGame = bbcGameRepository.getBbcGame(date, homeTeam.getId(), awayTeam.getId(), gameNumber);
		return bbcGame != null;
	}
}
