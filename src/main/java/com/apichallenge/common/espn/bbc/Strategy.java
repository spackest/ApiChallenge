package com.apichallenge.common.espn.bbc;

import java.util.*;

public interface Strategy {
	public Starters pickStarters(Date date, BbcLeague bbcLeague);

	public String getName();
}
