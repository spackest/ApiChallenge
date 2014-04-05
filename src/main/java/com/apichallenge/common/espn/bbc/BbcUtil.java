package com.apichallenge.common.espn.bbc;

import com.apichallenge.common.*;
import com.apichallenge.common.espn.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public final class BbcUtil {
	private static final String BBC_FRONT_PAGE = String.format("http://games.espn.go.com/baseball-challenge/%d/en/", Constants.YEAR);
	private static final String SET_ROSTER_URL = BBC_FRONT_PAGE + "util/api/setRoster";

	private static final Pattern SLOT_ID_PATTERN = Pattern.compile("slotID=(\\d+)");
	private static final Pattern SLOT_ID2_PATTERN = Pattern.compile("slot_id=.(\\d+).");
	private static final Pattern SPID_ID_PATTERN = Pattern.compile("spid=(\\d+)");

	private static final Log LOG = LogFactory.getLog(BbcUtil.class);

	public static MyConnection login(String username, String password) throws Exception {
		MyConnection myConnection = EspnUtil.login(username, password, BBC_FRONT_PAGE, "espn_bbc");
		return myConnection;
	}

	public static Starters getStarters(BbcTeamRepository bbcTeamRepository, BbcPlayerRepository bbcPlayerRepository, MyConnection myConnection, String entryName) throws IOException {
		Starters starters = new Starters();

		EspnEntry espnEntry = EspnUtil.getEspnEntry(myConnection, entryName);

		String url = BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId();
		Connection.Response response = myConnection.get(url, null);
		Document frontDocument = response.parse();

		String leagueUrl = null;

		int count = 0;

		for (Element element : frontDocument.select("[class$=available")) {
			for (Element thisElement : element.select("[class$=ajaxify")) {
				leagueUrl = thisElement.attr("abs:href");

				if (++count > 1) {
					leagueUrl.toString();
				}
			}
		}

		response = myConnection.get(leagueUrl, null);
		Document document = response.parse();

		Elements playerTable = document.select("[class*=player-table]");

		for (Element positionElement : playerTable.select("[id^=plyr]")) {
			Integer slotId = null;
			Matcher matcher = SLOT_ID2_PATTERN.matcher(positionElement.toString());
			if (matcher.find()) {
				slotId = Integer.valueOf(matcher.group(1));
			}

			if (slotId == null) {
				throw new IllegalStateException("no slotId for " + positionElement);
			}

			BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(bbcTeamRepository, bbcPlayerRepository, slotId, positionElement, true);

			if (bbcPlayerDay == null) {
				throw new IllegalStateException("no bbcPlayerDay for " + positionElement);
			}

			BbcPositionEnum bbcPositionEnum = null;

			for (Element slotElement : positionElement.select("[class*=games-left]")) {
				String slotShortName = slotElement.text();

				if (slotShortName == null || slotShortName.length() > 2) {
					continue;
				}

				bbcPositionEnum = BbcPositionEnum.getBbcPositionBySlotShortName(slotShortName);
			}

			starters.addStarter(bbcPositionEnum, bbcPlayerDay.getBbcPlayer());
		}

		return starters;
	}

	public static BbcLeague getLeague(BbcTeamRepository bbcTeamRepository, BbcPlayerRepository bbcPlayerRepository, MyConnection myConnection, String entryName) throws IOException {
		EspnEntry espnEntry = EspnUtil.getEspnEntry(myConnection, entryName);

		Connection.Response response = myConnection.get(espnEntry.getUrl(), null);

		Document frontDocument = response.parse();

		String leagueUrl = null;

		int count = 0;

		for (Element element : frontDocument.select("[class$=available")) {
			for (Element thisElement : element.select("[class$=ajaxify")) {
				leagueUrl = thisElement.attr("abs:href");

				if (++count > 1) {
					leagueUrl.toString();
				}
			}
		}

		response = myConnection.get(leagueUrl, null);

		Document leagueDocument = response.parse();

		int slots = 0;

		BbcLeague bbcLeague = new BbcLeague(espnEntry);

		Integer spid = null;

		for (Element leagueElement : leagueDocument.select("[id^=edit_slot]")) {
			String url = leagueElement.attr("abs:href");

			response = myConnection.get(url, null);

			Document positionDocument = response.parse();

			Elements tableElements = positionDocument.select("[id^=playertablebody]");

			assert (tableElements.size() == 1);

			List<BbcPlayerDay> bbcPlayerDays = new ArrayList<BbcPlayerDay>();

			Integer slotId = null;

			Matcher matcher = SLOT_ID_PATTERN.matcher(url);
			if (matcher.find()) {
				slotId = Integer.valueOf(matcher.group(1));
			}

			if (slotId == null) {
				throw new IllegalArgumentException("no slotId for " + url);
			}

			if (spid == null) {
				matcher = SPID_ID_PATTERN.matcher(url);
				if (matcher.find()) {
					spid = Integer.valueOf(matcher.group(1));
				}

				if (spid == null) {
					throw new IllegalArgumentException("no spid for " + url);
				}
			}

			BbcPositionEnum position = BbcPositionEnum.getBbcPositionBySlotId(slotId);

			for (Element positionElement : tableElements.select("[id^=plyr]")) {
				BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(bbcTeamRepository, bbcPlayerRepository, slotId, positionElement, false);
				if (bbcPlayerDay == null) {
					continue;
				}

				bbcPlayerDays.add(bbcPlayerDay);
			}

			bbcLeague.addLeagueSlot(new LeagueSlot(position, bbcPlayerDays));
			slots++;
		}

		if (spid == null) {
			throw new IllegalArgumentException("no spid, darn it");
		}

		myConnection.setSpid(spid);

		return bbcLeague;
	}

	public static Starters tradeForStarters(BbcTeamRepository bbcTeamRepository, BbcPlayerRepository bbcPlayerRepository, MyConnection myConnection, EspnEntry espnEntry, Starters starters) throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		for (Map.Entry<BbcPositionEnum, BbcPlayer> entry : starters.getEntrySet()) {
			BbcPlayer bbcPlayer = entry.getValue();

			LOG.info(entry.getKey().getShortName() + " starting " + bbcPlayer.getName());

			form.put("playerInSlot_" + entry.getKey().getSlotId(), String.valueOf(bbcPlayer.getEspnId()));
		}
		form.put("entryID", String.valueOf(espnEntry.getId()));
		form.put("redirect", BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId());
		form.put("creationSource", String.valueOf(1));
		form.put("teams", String.valueOf(-1));
		form.put("spid", String.valueOf(myConnection.getSpid()));

		myConnection.post(SET_ROSTER_URL, form);

		return getStarters(bbcTeamRepository, bbcPlayerRepository, myConnection, espnEntry.getName());
	}


	private static BbcPlayerDay getPlayingPlayerFromElement(BbcTeamRepository bbcTeamRepository, BbcPlayerRepository bbcPlayerRepository, int slotId, Element element, boolean allowNullOpponent) {
		int espnId = Integer.valueOf(element.attr("player_id").toString());
		String name = element.select("[id=pFN]").text() + " " + element.select("[id=pLN]").text();
		name = name.trim();
		float average = Float.valueOf(element.select("[class*=st-favg]").text());
		int points = Integer.valueOf(element.select("[class*=st-fpts]").text());

		Elements injuryStatuses = element.select("[class=injury-status]");

		// skip injured folks
		if (injuryStatuses.size() > 0) {
			return null;
		}

		BbcTeam team = null;

		String teamShortName = element.select("[class=player_team]").text();
		String opponentShortName = element.select("[class=player_opp]").text();

		if (teamShortName == null || teamShortName.length() == 0) {
			team = bbcTeamRepository.findByName(name);
		} else {
			team = bbcTeamRepository.findByShortName(teamShortName);
		}

		if (team == null) {
			throw new IllegalArgumentException("no team from '" + element.toString() + "'");
		}

		if (opponentShortName == null || opponentShortName.length() == 0 || opponentShortName.equals("--")) {
			opponentShortName = null;
		}

		if (!allowNullOpponent && opponentShortName == null) {
			return null;
		}

		BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId);

		if (bbcPlayer == null) {
			bbcPlayer = new BbcPlayer(espnId, slotId, team.getId(), name);
			bbcPlayerRepository.save(bbcPlayer);
		}

		boolean homeGame = false;

		BbcTeam bbcOpponent = null;

		if (opponentShortName != null) {
			homeGame = !opponentShortName.startsWith("@");
			opponentShortName = opponentShortName.replaceAll("@", "");
			bbcOpponent = bbcTeamRepository.findByShortName(opponentShortName);
		}

		return new BbcPlayerDay(bbcPlayer, team, bbcOpponent, homeGame, average, points);
	}
}
