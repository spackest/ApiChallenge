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
import org.jsoup.helper.*;
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
	BbcGameService bbcGameService;

	@Autowired
	BbcPlayerService bbcPlayerService;

	@Autowired
	BbcSlurp bbcSlurp;

	private static final String BBC_FRONT_PAGE = String.format("http://games.espn.go.com/baseball-challenge/%d/en/", Constants.YEAR);

	private static final String SET_ROSTER_URL = BBC_FRONT_PAGE + "util/api/setRoster";

	private static final Pattern DOUBLE_HEADER_OPP_PATTERN = Pattern.compile("^2 Gms.+\\s+(@?[A-Z]{2,})$");
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
			bbcSlurp.slurpSchedule(Constants.YEAR);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("slurp issue", e);
		}

		try {
			myConnection = EspnUtil.login(username, password, BBC_FRONT_PAGE, "espn_bbc");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("could not login");
		}

		setEspnEntry(fantasyTeam.getEntryName());

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

	public Starters getStarters() throws IOException {
		Starters starters = new Starters();

		Integer spid = myConnection.getSpid();

		if (spid == null) {
			throw new IllegalStateException("no spid");
		}

		String url = BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId() + "&spid=" + spid;
		Connection.Response response = myConnection.get(url, null);

		Document document = response.parse();

		Elements playerTable = document.select("[class*=player-table]");

		for (Element positionElement : playerTable.select("[id^=plyr]")) {
			SlotId slotId = null;
			Matcher matcher = SLOT_ID2_PATTERN.matcher(positionElement.toString());
			if (matcher.find()) {
				slotId = new SlotId(Integer.valueOf(matcher.group(1)));
			}

			if (slotId == null) {
				throw new IllegalStateException("no slotId for " + positionElement);
			}

			BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(Constants.YEAR, slotId, positionElement);

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

	public BbcLeague getLeague(Date date) throws IOException {
		Connection.Response response = myConnection.get(espnEntry.getUrl(), null);

		Document frontDocument = response.parse();

		String leagueUrl = frontDocument.select("[class$=available").select("[class=sp-nav-scoring-period-link]").attr("abs:href");

		for (Element element : frontDocument.select("[class$=available")) {
			if (StringUtil.isBlank(leagueUrl)) {
				leagueUrl = element.attr("abs:href");
			}

			for (Element thisElement : element.select("[class$=ajaxify")) {
				leagueUrl = thisElement.attr("abs:href");
			}
		}

		if (StringUtil.isBlank(leagueUrl)) {
			throw new IllegalStateException("null leagueUrl");
		}

		Integer spid = null;

		Matcher matcher = SPID_ID_PATTERN.matcher(leagueUrl);
		if (matcher.find()) {
			spid = Integer.valueOf(matcher.group(1));
		}

		if (spid == null) {
			throw new IllegalArgumentException("no spid for " + leagueUrl);
		}

		myConnection.setSpid(spid);

		response = myConnection.get(leagueUrl, null);

		Document leagueDocument = response.parse();

		Map<BbcPositionEnum, List<BbcPlayerDay>> positionPlayerMap = new HashMap<BbcPositionEnum, List<BbcPlayerDay>>();

		Elements elements = null;

		if (!leagueDocument.select("[id^=playerLink]").isEmpty()) {
			elements = leagueDocument.select("[id^=playerLink]");
		} else if (!leagueDocument.select("[id^=edit_slot]").isEmpty()) {
			elements = leagueDocument.select("[id^=edit_slot]");
		}

		if (elements == null) {
			throw new IllegalStateException("no elements: " + leagueDocument);
		}

		for (Element leagueElement : elements) {
			String url = leagueElement.attr("abs:href");

			if (!url.contains("spid")) {
				url += "&spid=" + spid;
			}

			response = myConnection.get(url, null);

			Document positionDocument = response.parse();

			Elements tableElements = positionDocument.select("[id^=playertablebody]");

			if (tableElements.size() != 1) {
				throw new IllegalStateException("bad tableElements.size(): " + tableElements.size());
			}

			SlotId slotId = null;

			matcher = SLOT_ID_PATTERN.matcher(url);
			if (matcher.find()) {
				slotId = new SlotId(Integer.valueOf(matcher.group(1)));
			}

			if (slotId == null) {
				throw new IllegalArgumentException("no slotId for " + url);
			}

			BbcPositionEnum position = BbcPositionEnum.getBbcPositionBySlotId(slotId);

			if (!positionPlayerMap.containsKey(position)) {
				positionPlayerMap.put(position, new ArrayList<BbcPlayerDay>());
			}

			for (Element positionElement : tableElements.select("[id^=plyr]")) {
				BbcPlayerDay bbcPlayerDay = getPlayingPlayerFromElement(DateUtil.getYear(date), slotId, positionElement);
				if (bbcPlayerDay == null || bbcPlayerDay.getOpponent() == null || bbcPlayerDay.isInjured()) {
					continue;
				}

				positionPlayerMap.get(position).add(bbcPlayerDay);
			}

		}

		Map<Long, BbcPlayer> startingPitchers = new HashMap<Long, BbcPlayer>();

		for (BbcPlayerDay bbcPlayerDay : positionPlayerMap.get(BbcPositionEnum.PITCHING_STAFF)) {
			BbcPlayer thisBbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(bbcPlayerDay.getEspnId().getId());
			BbcPlayer bbcPlayer = new BbcPlayer(bbcPlayerDay.getBbcId(), bbcPlayerDay.getEspnId(), BbcPositionEnum.PITCHER.getSlotId(), bbcPlayerDay.getTeamId(), thisBbcPlayer == null ? "no name" : thisBbcPlayer.getName());
			startingPitchers.put(bbcPlayer.getTeamId(), bbcPlayer);
			startingPitchers.put(bbcPlayerDay.getOpponent().getId(), bbcPlayer);
		}

		BbcLeague bbcLeague = new BbcLeague();

		for (Map.Entry<BbcPositionEnum, List<BbcPlayerDay>> entry : positionPlayerMap.entrySet()) {
			BbcPositionEnum bbcPositionEnum = entry.getKey();

			Map<BbcPlayerDay, List<BbcGame>> map = new HashMap<BbcPlayerDay, List<BbcGame>>();

			for (BbcPlayerDay bbcPlayerDay : entry.getValue()) {

				Long homeTeamId = null;
				Long awayTeamId = null;

				bbcPlayerDay.setTeamStartingPitcher(startingPitchers.get(bbcPlayerDay.getTeamId()));
				bbcPlayerDay.setOpposingStartingPitcher(startingPitchers.get(bbcPlayerDay.getOpponentId()));

				if (bbcPlayerDay.isHomeGame()) {
					homeTeamId = bbcPlayerDay.getTeamId();
					awayTeamId = bbcPlayerDay.getOpponentId();
				} else {
					homeTeamId = bbcPlayerDay.getOpponentId();
					awayTeamId = bbcPlayerDay.getTeamId();
				}

				List<BbcGame> bbcGames = bbcGameRepository.getBbcGames(date, homeTeamId, awayTeamId);

				map.put(bbcPlayerDay, bbcGames);
			}

			LeagueSlot leagueSlot = new LeagueSlot(bbcPositionEnum, map);

			bbcLeague.addLeagueSlot(leagueSlot);
		}

		return bbcLeague;
	}

	private String getIr() {
		String ir = null;

		try {
			Starters starters = getStarters();
			ir = String.valueOf(starters.getStarter(BbcPositionEnum.getBbcPositionBySlotId(new SlotId(1))).getBbcId().getId());

			for (int slotId = 2; slotId <= 10; slotId++) {
				ir += "|" + starters.getStarter(BbcPositionEnum.getBbcPositionBySlotId(new SlotId(slotId))).getBbcId().getId();
			}
		} catch (IOException e) {
			throw new IllegalStateException("nope on starters for " + espnEntry.getName(), e);
		}

		return ir;
	}

	public Starters tradeForStarters(Starters starters) throws IOException {
		Map<String, String> form = new HashMap<String, String>();
		for (Map.Entry<BbcPositionEnum, BbcPlayer> entry : starters.getEntrySet()) {
			BbcPlayer bbcPlayer = entry.getValue();

			BbcId bbcId = bbcPlayer.getBbcId();

			if (bbcId == null) {
				if (entry.getKey().getSlotId().getId() == BbcPositionEnum.PITCHING_STAFF.getSlotId().getId()) {
					bbcPlayer = bbcPlayerRepository.getPitchingStaff(bbcPlayer.getTeamId(), BbcPositionEnum.PITCHING_STAFF.getSlotId().getId());
					bbcId = bbcPlayer.getBbcId();
				}
			}

			if (bbcId == null) {
				throw new IllegalStateException("no bbcId for " + bbcPlayer);
			}

			System.out.println(entry.getKey().getShortName() + " starting " + bbcPlayer.getName());

			form.put("playerInSlot_" + entry.getKey().getSlotId().getId(), String.valueOf(bbcId.getId()));
		}
		form.put("entryID", String.valueOf(espnEntry.getId()));
		form.put("redirect", BBC_FRONT_PAGE + "entry?entryID=" + espnEntry.getId());
		form.put("creationSource", String.valueOf(1));
		form.put("teams", String.valueOf(-1));
		form.put("spid", String.valueOf(myConnection.getSpid()));
		//form.put("ir", getIr());

		myConnection.post(SET_ROSTER_URL, form);

		return getStarters();
	}

	private BbcPlayerDay getPlayingPlayerFromElement(int year, SlotId slotId, Element element) {
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

		String averageString = element.select("[class*=st-favg]").text();
		String pointsString = element.select("[class*=st-fpts]").text();
		float average = StringUtil.isBlank(averageString) ? 0 : Float.valueOf(averageString);
		int points = StringUtil.isBlank(pointsString) ? 0 : Integer.valueOf(pointsString);

		Elements injuryStatuses = element.select("[class=injury-status]");

		boolean injured = injuryStatuses.size() > 0;

		BbcTeam team = null;

		String teamShortName = element.select("[class=player_team]").text();
		Elements opponentElements = element.select("[class^=player_opp]");

		String opponentShortName = null;
		if (opponentElements != null) {
			opponentShortName = opponentElements.get(0).text();
		}

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
		} else {
			Matcher matcher = DOUBLE_HEADER_OPP_PATTERN.matcher(opponentShortName);
			if (matcher.find()) {
				opponentShortName = matcher.group(1);
			}
		}

		bbcPlayerService.perhapsInsertPlayer(year, bbcId, espnId, slotId, team, name);

		boolean homeGame = false;

		BbcTeam bbcOpponent = null;

		if (opponentShortName != null) {
			homeGame = !opponentShortName.startsWith("@");
			opponentShortName = opponentShortName.replaceAll("@", "");
			bbcOpponent = bbcTeamRepository.findByShortName(opponentShortName);
		}

		if (slotId.getId() == BbcPositionEnum.PITCHING_STAFF.getSlotId().getId()) {
			Matcher matcher = PLAYER_ID_PATTERN.matcher(element.toString());

			if (matcher.find()) {
				espnId = new EspnId(Integer.valueOf(matcher.group(1)));
			}
		}

		BbcPlayerDay bbcPlayerDay = new BbcPlayerDay(espnId, bbcId, injured, team, bbcOpponent, homeGame, average, points);

		return bbcPlayerDay;
	}
}
