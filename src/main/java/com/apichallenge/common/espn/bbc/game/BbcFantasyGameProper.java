package com.apichallenge.common.espn.bbc.game;

import com.apichallenge.common.*;
import com.apichallenge.common.espn.*;
import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import com.apichallenge.common.espn.bbc.service.*;
import org.apache.commons.logging.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

@Service
public class BbcFantasyGameProper extends FantasyGame {
	@Autowired
	BbcGameRepository bbcGameRepository;

	@Autowired
	BbcTeamRepository bbcTeamRepository;

	@Autowired
	BbcPlayerRepository bbcPlayerRepository;

	@Autowired
	BbcPointsRepository bbcPointsRepository;

	@Autowired
	BbcPlayerService bbcPlayerService;

	private static final String BBC_FRONT_PAGE = String.format("http://games.espn.go.com/baseball-challenge/%d/en/", Constants.YEAR);

	private static final String SET_ROSTER_URL = BBC_FRONT_PAGE + "util/api/setRoster";

	private static final Pattern SLOT_ID_PATTERN = Pattern.compile("slotID=(\\d+)");
	private static final Pattern SLOT_ID2_PATTERN = Pattern.compile("slot_id=.(\\d+).");
	private static final Pattern SPID_ID_PATTERN = Pattern.compile("spid=(\\d+)");
	private static final Pattern PLAYER_ID_PATTERN = Pattern.compile("playerId=(\\d+)", Pattern.MULTILINE);

	private static final Log LOG = LogFactory.getLog(BbcFantasyGameProper.class);

	private MyConnection myConnection;
	private EspnEntry espnEntry;

	public BbcFantasyGameProper() {
		super();
	}

	public void init(String username, String password, FantasyTeam fantasyTeam) {
		try {
			myConnection = EspnUtil.login(username, password, BBC_FRONT_PAGE, "espn_bbc");
			setEspnEntry(fantasyTeam.getEntryName());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("could not login");
		}

		this.fantasyTeam = fantasyTeam;
	}

	private void setEspnEntry(String entryName) {
		List<EspnEntry> espnEntries = myConnection.getEspnEntries();

		if (espnEntries == null || espnEntries.size() == 0) {
			throw new IllegalArgumentException("no entries found :(");
		}

		for (EspnEntry thisEspnEntry : espnEntries) {
			if (thisEspnEntry.getName().equals(entryName)) {
				espnEntry = thisEspnEntry;
				break;
			}
		}

		if (espnEntry == null) {
			throw new IllegalArgumentException("no espnEntry for " + entryName);
		}
	}

	public Starters getStarters(String entryName) throws IOException {
		Starters starters = new Starters();

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

			BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(slotId, positionElement, true);

			if (bbcPlayerDay == null) {
				continue;
			}

			BbcPositionEnum bbcPositionEnum = null;

			for (Element slotElement : positionElement.select("[class*=games-left]")) {
				String slotShortName = slotElement.text();

				if (slotShortName == null || slotShortName.length() > 2) {
					continue;
				}

				bbcPositionEnum = BbcPositionEnum.getBbcPositionBySlotShortName(slotShortName);
			}

			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(bbcPlayerDay.getEspnId().getId());
			starters.addStarter(bbcPositionEnum, bbcPlayer);
		}

		return starters;
	}

	public BbcLeague getFullLeague() throws IOException {
		return getLeague(DateUtil.getGameTomorrow(), true);
	}

	public BbcLeague getLeague(Date date, boolean getFullLeague) throws IOException {
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

		BbcLeague bbcLeague = new BbcLeague();

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
				BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(slotId, positionElement, getFullLeague);
				if (bbcPlayerDay == null) {
					continue;
				}

				bbcPlayerDays.add(bbcPlayerDay);
			}

			bbcLeague.addLeagueSlot(new LeagueSlot(position, bbcPlayerDays));
		}

		if (spid == null) {
			throw new IllegalArgumentException("no spid, darn it");
		}

		myConnection.setSpid(spid);

		return bbcLeague;
	}

	public Starters tradeForStarters(Starters starters) throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		for (Map.Entry<BbcPositionEnum, BbcPlayer> entry : starters.getEntrySet()) {
			EspnId espnId = entry.getValue().getEspnId();

			BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());
			LOG.info(entry.getKey().getShortName() + " starting " + bbcPlayer.getName());

			form.put("playerInSlot_" + entry.getKey().getSlotId(), String.valueOf(bbcPlayer.getBbcId().getId()));
		}
		form.put("entryID", String.valueOf(espnEntry.getId()));
		form.put("redirect", BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId());
		form.put("creationSource", String.valueOf(1));
		form.put("teams", String.valueOf(-1));
		form.put("spid", String.valueOf(myConnection.getSpid()));

		myConnection.post(SET_ROSTER_URL, form);

		return getStarters(espnEntry.getName());
	}

	private BbcPlayerDay getPlayingPlayerFromElement(int slotId, Element element, boolean getFullLeague) {
		Elements theseElements = element.select("[class=roster-plyr ppcLink]");

		BbcId bbcId = null;
		EspnId espnId = null;

		if (theseElements != null) {
			String bbcIdString = theseElements.attr("player_id");
			String espnIdString = theseElements.attr("player_eid");

			if (bbcIdString != null) {
				bbcId = new BbcId(Integer.valueOf(bbcIdString));
			}

			if (espnIdString != null) {
				espnId = new EspnId(Integer.valueOf(espnIdString));
			}
		}

		if (espnId == null) {
			throw new IllegalStateException("no espnId for " + element);
		}


		String name = element.select("[id=pFN]").text() + " " + element.select("[id=pLN]").text();
		name = name.trim();
		float average = Float.valueOf(element.select("[class*=st-favg]").text());
		int points = Integer.valueOf(element.select("[class*=st-fpts]").text());

		Elements injuryStatuses = element.select("[class=injury-status]");

		// skip injured folks
		if (!getFullLeague && injuryStatuses.size() > 0) {
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

		if (!getFullLeague && opponentShortName == null) {
			return null;
		}

		bbcPlayerService.perhapsInsertPlayer(bbcId, espnId, slotId, team, name);

		BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());

		boolean homeGame = false;

		BbcTeam bbcOpponent = null;

		if (opponentShortName != null) {
			homeGame = !opponentShortName.startsWith("@");
			opponentShortName = opponentShortName.replaceAll("@", "");
			bbcOpponent = bbcTeamRepository.findByShortName(opponentShortName);
		}

		if (!getFullLeague && bbcOpponent == null) {
			return null;
		}

		BbcPlayerDay bbcPlayerDay = new BbcPlayerDay(espnId, bbcId, team.getId(), bbcOpponent == null ? null : bbcOpponent.getId(), homeGame, average, points);

		if (slotId == BbcPositionEnum.PITCHING_STAFF.getSlotId()) {
			Matcher matcher = PLAYER_ID_PATTERN.matcher(element.toString());

			if (matcher.find()) {
				espnId = new EspnId(Integer.valueOf(matcher.group(1)));
				BbcPlayer thisBbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());

				if (thisBbcPlayer != null) {
					Integer thesePoints = bbcPointsRepository.getTotalPoints(Constants.YEAR, espnId.getId());
					Float thisAverage = bbcPointsRepository.getAveragePoints(Constants.YEAR, espnId.getId());

					BbcPlayerDay startingPitcher = new BbcPlayerDay(espnId, thisBbcPlayer.getBbcId(), team.getId(), bbcOpponent == null ? null : bbcOpponent.getId(), homeGame, thisAverage == null ? 0 : thisAverage, thesePoints == null ? 0 : thesePoints);
					bbcPlayerDay.setStartingPitcher(startingPitcher);
				}
			}
		}

		return bbcPlayerDay;
	}
}
