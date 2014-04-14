package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.*;
import com.apichallenge.common.espn.bbc.*;
import com.apichallenge.common.espn.bbc.entity.*;
import com.apichallenge.common.espn.bbc.enums.*;
import com.apichallenge.common.espn.bbc.repository.*;
import org.apache.commons.logging.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import javax.persistence.*;
import java.util.*;
import java.util.regex.*;

@Service
public class BbcSlurp {
	private static final Pattern SCHEDULE_NAME_PATTERN = Pattern.compile("team=(\\w+)");
	private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("[A-Z]\\w+,\\s*([A-Z]\\w+)\\s+(\\d+)");
	private static final Pattern SCHEDULE_NAME_PATTERN_TWO = Pattern.compile("/team/_/name/(\\w+)/");
	private static final Pattern ESPN_GAME_ID_PATTERN = Pattern.compile("recap.id=(\\d+)");
	private static final Pattern POSITION_PLAYER_BOX_PATTERN = Pattern.compile("/id/(\\d+)/[^>]+>([^<]+)</a>\\s*(\\w+)[^<]*(?:</div>)?</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*<td[^>]*>\\s*([0-9.]+)\\s*</td>\\s*");
	private static final Pattern PITCHER_BOX_PATTERN = Pattern.compile("/id/(\\d+)/[^>]+>([^<]+)</a>.*?\\s*<td>([0-9]+)\\.([0-9])</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*<td>([0-9.]+)</td>\\s*");
	private static final Pattern EXTRA_BOX_PATTERN = Pattern.compile("<strong>(2B|3B|HR|SB):</strong>(.*?)<", Pattern.DOTALL);
	private static final Pattern CHUNK_BOX_PATTERN = Pattern.compile("([^(]+)\\s+(\\d+\\s*)\\(");
	private static final Pattern WIN_PATTERN = Pattern.compile("\\(W,\\s*\\d+-\\d+\\)");
	private static final Pattern SCHEDULE_PITCHER_PATTERN = Pattern.compile("/id/(\\d+)/");

	private static final Log LOG = LogFactory.getLog(BbcSlurp.class);

	private static final SlotId PITCHING_SLOT_ID = new SlotId(10);
	private static final Map<String, Integer> EXTRA_POINTS = new HashMap<String, Integer>();

	static {
		EXTRA_POINTS.put("2B", 1);
		EXTRA_POINTS.put("3B", 2);
		EXTRA_POINTS.put("HR", 3);
		EXTRA_POINTS.put("SB", 1);
	}

	private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>();

	@Autowired
	private BbcTeamRepository bbcTeamRepository;

	@Autowired
	private BbcPlayerRepository bbcPlayerRepository;

	@Autowired
	private BbcGameRepository bbcGameRepository;

	@Autowired
	private BbcPointsRepository bbcPointsRepository;

	@Autowired
	private BbcGameService bbcGameService;

	@Autowired
	private BbcPlayerService bbcPlayerService;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@PostConstruct
	private void init() {
		List<BbcTeam> bbcTeams = new ArrayList<BbcTeam>();

		for (BbcTeamEnum bbcTeamEnum : BbcTeamEnum.values()) {
			BbcTeam bbcTeam = bbcTeamRepository.findByShortName(bbcTeamEnum.getTeam().getShortName());

			if (bbcTeam == null) {
				BbcTeam thisBbcTeam = bbcTeamEnum.getTeam();
				bbcTeams.add(new BbcTeam(thisBbcTeam.getName(), thisBbcTeam.getShortName(), thisBbcTeam.getScheduleName()));
			}
		}

		if (bbcTeams.size() > 0) {
			bbcTeamRepository.save(bbcTeams);
		}
	}

	static {
		MONTHS.put("Jan", 0);
		MONTHS.put("Feb", 1);
		MONTHS.put("Mar", 2);
		MONTHS.put("Apr", 3);
		MONTHS.put("May", 4);
		MONTHS.put("Jun", 5);
		MONTHS.put("Jul", 6);
		MONTHS.put("Aug", 7);
		MONTHS.put("Sep", 8);
		MONTHS.put("Oct", 9);
		MONTHS.put("Nov", 10);
		MONTHS.put("Dec", 11);
	}

	private List<BbcTeam> getTeams() {
		String html = SimpleHttpCache.getHtmlFromUrl("http://espn.go.com/mlb/schedule");

		List<BbcTeam> teams = new ArrayList<BbcTeam>();

		Document document = Jsoup.parse(html);
		Elements options = document.select("select > option");
		for (Element option : options) {
			String value = option.attr("value");
			if (value == null || value.length() == 0 || value.equals("#")) {
				continue;
			}

			String scheduleName = null;

			Matcher matcher = SCHEDULE_NAME_PATTERN.matcher(value);
			if (matcher.find()) {
				scheduleName = matcher.group(1);
			}

			if (scheduleName == null) {
				throw new IllegalArgumentException("no schedule name for " + value);
			}

			BbcTeam bbcTeam = bbcTeamRepository.findByScheduleName(scheduleName);

			if (bbcTeam == null) {
				throw new IllegalArgumentException("no bbcTeam for " + scheduleName);
			}

			teams.add(bbcTeam);

		}

		return teams;
	}

	private static String getScheduleUrl(int year, int half, String teamScheduleName) {
		return String.format("http://espn.go.com/mlb/team/schedule/_/name/%s/year/%d/seasontype/2/half/%d/", teamScheduleName, year, half);
	}

	private Date getGameDateFromSchedule(int year, String row) {
		Date date = null;

		Matcher matcher = MONTH_DAY_PATTERN.matcher(row);
		if (matcher.find()) {
			int month = MONTHS.get(matcher.group(1));
			int day = Integer.valueOf(matcher.group(2));
			Calendar calendar = new GregorianCalendar(year, month, day);

			date = calendar.getTime();
		} else {
			LOG.debug("no date");
		}

		return date;
	}

	private BbcGame getBbcGamePojo(int year, BbcTeam team, Element element) {
		String row = element.toString();

		if (row.contains("POSTPONED")) {
			return null;
		}

		Date date = getGameDateFromSchedule(year, row);

		String gameStatus = element.select("[class=game-status]").text();

		BbcTeam homeTeam;
		BbcTeam awayTeam;

		BbcTeam opponentTeam = null;

		Matcher matcher = SCHEDULE_NAME_PATTERN_TWO.matcher(row);
		if (matcher.find()) {
			String opponentName = matcher.group(1);
			opponentTeam = bbcTeamRepository.findByShortName(opponentName);
		}

		if (opponentTeam == null) {
			throw new IllegalArgumentException("no opponent team for '" + row + "'");
		}

		boolean homeGame;

		if (gameStatus.equals("vs")) {
			homeTeam = team;
			awayTeam = opponentTeam;
			homeGame = true;
		} else if (gameStatus.equals("@")) {
			homeTeam = opponentTeam;
			awayTeam = team;
			homeGame = false;
		} else {
			throw new IllegalArgumentException("no game status for '" + row + "'");
		}

		String gameExistsKey = date.toString() + "." + homeTeam.getId() + "." + awayTeam.getId();

		EspnId teamStartingPitcherId = null;
		EspnId opponentStartingPitcherId = null;

		EspnId homeStartingPitcherEspnId = null;
		EspnId awayStartingPitcherEspnId = null;

		matcher = SCHEDULE_PITCHER_PATTERN.matcher(element.toString());
		if (matcher.find()) {
			teamStartingPitcherId = new EspnId(Integer.valueOf(matcher.group(1)));
		}
		if (matcher.find()) {
			opponentStartingPitcherId = new EspnId(Integer.valueOf(matcher.group(1)));
		}

		if (teamStartingPitcherId != null && opponentStartingPitcherId != null) {
			if (homeGame) {
				homeStartingPitcherEspnId = teamStartingPitcherId;
				awayStartingPitcherEspnId = opponentStartingPitcherId;
			} else {
				homeStartingPitcherEspnId = opponentStartingPitcherId;
				awayStartingPitcherEspnId = teamStartingPitcherId;
			}
		}

		EspnGameId espnGameId = null;

		matcher = ESPN_GAME_ID_PATTERN.matcher(element.select("[class=score]").toString());
		if (matcher.find()) {
			espnGameId = new EspnGameId(Integer.valueOf(matcher.group(1)));
		}

		int gameNumber = bbcGameService.getGameNumber(date, espnGameId, homeTeam, awayTeam);

		return new BbcGame(date, espnGameId, homeTeam, awayTeam, homeStartingPitcherEspnId, awayStartingPitcherEspnId, gameNumber);
	}

	public void slurpSchedule(int year) {
		List<BbcTeam> teams = bbcTeamRepository.findAll();

		int totalNewBbcGames = 0;

		for (BbcTeam team : teams) {
			int newBbcGames = 0;
			int totalBbcGames = 0;

			for (int half : Arrays.asList(1, 2)) {
				String url = getScheduleUrl(year, half, team.getScheduleName());

				boolean fresh = false;

				if (Constants.YEAR == year) {
					int currentHalf = DateUtil.getNow().before(Constants.SEASON_MID_POINT) ? 1 : 2;

					if (half != currentHalf) {
						fresh = false;
					} else {
						if (DateUtil.getCurrentHour() == 0) {
							//fresh = true;
							fresh = false;
						}
					}
				}

				String html = SimpleHttpCache.getHtmlFromUrl(url, fresh);

				Document document = Jsoup.parse(html);

				for (Element element : document.select("[class*=row team]")) {
					BbcGame bbcGame = getBbcGamePojo(year, team, element);
					if (bbcGame == null) {
						continue;
					}

					bbcGameService.sync(bbcGame);

					if (bbcGame.getEspnGameId() != null) {
						handleEspnGameId(bbcGame);
					}
				}
			}

			LOG.info(team.getName() + " had " + newBbcGames + " new games, " + totalBbcGames + " total games");
		}

		if (totalNewBbcGames > 0) {
			handleIncomingPoints(year);
		}
	}

	public void handleIncomingPoints(int year) {
		EspnId lastEspnId = null;
		int incomingPoints = 0;
		int incomingGames = 0;

		for (BbcPoints bbcPoints : bbcPointsRepository.getSeason(year)) {
			EspnId espnId = bbcPoints.getEspnId();

			if (lastEspnId == null || !espnId.equals(lastEspnId)) {
				incomingPoints = 0;
				incomingGames = 0;
				lastEspnId = espnId;
				System.out.println("incoming points for " + espnId);
			}

			bbcPoints.setIncomingGames(incomingGames);

			int points = bbcPoints.getPoints();
			bbcPoints.setIncomingTotalPoints(incomingPoints);

			float average = (incomingGames == 0) ? 0 : ((float) incomingPoints / incomingGames);
			bbcPoints.setIncomingAveragePoints(average);
			bbcPointsRepository.save(bbcPoints);

			incomingGames++;
			incomingPoints += points;
		}


	}

	private void handleBoxScoreElement(Map<EspnId, Integer> playerPoints, Map<EspnId, SlotId> playerSlot, Map<String, EspnId> playerNameMap, Map<EspnId, BbcTeam> playerTeam, Set<String> pitchers, Map<BbcTeam, Integer> pitchingPoints, Map<BbcTeam, EspnId> startingPitchers, Element boxScoreElement) {
		boolean hitters = boxScoreElement.toString().contains("Hitters");

		String teamName = boxScoreElement.select("[class=team-color-strip]").get(0).text();
		BbcTeam bbcTeam = bbcTeamRepository.findByName(teamName);

		if (bbcTeam == null) {
			throw new IllegalStateException("can't find team in " + boxScoreElement);
		}

		if (!hitters && !pitchingPoints.containsKey(bbcTeam)) {
			if (WIN_PATTERN.matcher(boxScoreElement.toString()).find()) {
				pitchingPoints.put(bbcTeam, 5);
			} else {
				pitchingPoints.put(bbcTeam, 0);
			}
		}

		for (Element playerElement : boxScoreElement.select("[class*=player]")) {
			if (playerElement.select("[href]").isEmpty()) {
				LOG.debug("no links in " + playerElement);
				continue;
			}

			Matcher matcher = POSITION_PLAYER_BOX_PATTERN.matcher(playerElement.toString());

			EspnId espnId = null;
			String name = null;
			String lcName = null;

			if (matcher.find()) {
				espnId = new EspnId(Integer.valueOf(matcher.group(1)));
				name = matcher.group(2);
				lcName = name.toLowerCase();
				String slot = matcher.group(3);

				if (slot.equals("P")) {
					continue;
				}

				bbcPlayerService.perhapsInsertPlayer(null, espnId, null, bbcTeam, name);

				playerTeam.put(espnId, bbcTeam);

				Integer runs = Integer.valueOf(matcher.group(5));
				Integer hits = Integer.valueOf(matcher.group(6));
				Integer rbis = Integer.valueOf(matcher.group(7));
				Integer walks = Integer.valueOf(matcher.group(8));
				int points = runs + hits + rbis + walks;
				playerPoints.put(espnId, points);
				SlotId slotId = BbcPositionEnum.getBbcPositionBySlotShortName(slot).getSlotId();

				if (slotId != null) {
					playerSlot.put(espnId, slotId);
				}

				playerNameMap.put(lcName, espnId);
			} else {
				//The following points will be awarded/deducted for each instance one of them take place: 1 out = +1 (complete IP = +3), ER = -3, Hit = -1, BB = -1, K = +1, Win = +5.
				matcher = PITCHER_BOX_PATTERN.matcher(playerElement.toString());
				if (matcher.find()) {
					espnId = new EspnId(Integer.valueOf(matcher.group(1)));
					name = matcher.group(2).trim();
					pitchers.add(name);

					if (!startingPitchers.containsKey(bbcTeam)) {
						bbcPlayerService.perhapsInsertPlayer(null, espnId, PITCHING_SLOT_ID, bbcTeam, name);
						startingPitchers.put(bbcTeam, espnId);
					}

					int outs = Integer.valueOf(matcher.group(3)) * 3 + Integer.valueOf(matcher.group(4));
					int hits = Integer.valueOf(matcher.group(5));
					int walks = Integer.valueOf(matcher.group(8));
					int earnedRuns = Integer.valueOf(matcher.group(7));
					int strikeouts = Integer.valueOf(matcher.group(9));

					int pointsSoFar = pitchingPoints.get(bbcTeam);
					pointsSoFar += (outs - (earnedRuns * 3) - hits - walks + strikeouts);
					pitchingPoints.put(bbcTeam, pointsSoFar);
				}
			}

			if (espnId == null) {
				throw new IllegalStateException("no espnId for " + playerElement);
			}
		}
	}

	private void handleExtraBox(Map<EspnId, Integer> playerPoints, Map<String, EspnId> playerNameMap, Set<String> pitchers, String html) {
		Matcher matcher = EXTRA_BOX_PATTERN.matcher(html);

		while (matcher.find()) {
			String type = matcher.group(1).toUpperCase();
			String rest = matcher.group(2);

			if (!EXTRA_POINTS.containsKey(type)) {
				throw new IllegalStateException("no extra points for " + type);
			}

			for (String chunk : rest.split("\\s*;\\s*")) {
				Matcher chunkMatcher = CHUNK_BOX_PATTERN.matcher(chunk);

				while (chunkMatcher.find()) {
					String name = chunkMatcher.group(1).trim();
					String lcName = name.toLowerCase();

					String rawCount = chunkMatcher.group(2);

					int count = rawCount == null ? 1 : Integer.valueOf(rawCount.trim());

					if (!playerNameMap.containsKey(lcName)) {
						if (pitchers.contains(name)) {
							continue;
						}
						LOG.info("nope on " + name);
						continue;
					}

					EspnId espnId = playerNameMap.get(lcName);
					if (!playerPoints.containsKey(espnId)) {
						if (pitchers.contains(name)) {
							continue;
						}
						throw new IllegalStateException("nope on " + espnId);
					}

					int points = playerPoints.get(espnId);
					points += count * EXTRA_POINTS.get(type);

					playerPoints.put(espnId, points);
				}
			}
		}

	}

	private void handleEspnGameId(BbcGame bbcGame) {
		EspnGameId espnGameId = bbcGame.getEspnGameId();

		Date date = bbcGame.getDate();

		System.out.println("starting " + espnGameId);

		if (bbcGameService.gameComplete(espnGameId)) {
			LOG.info("already complete " + espnGameId);
			return;
		}

		bbcPointsRepository.clearOutEspnGameId(espnGameId.getId());

		String url = "http://espn.go.com/mlb/boxscore?gameId=" + espnGameId.getId();

		String html = SimpleHttpCache.getHtmlFromUrl(url);

		Document document = Jsoup.parse(html);

		Elements gameFinalElements = document.select("[class=game-state]");
		if (gameFinalElements.size() == 0) {
			LOG.info("game with id " + espnGameId + " doesn't appear to be final");
			SimpleHttpCache.wipeCache(url);
			return;
		}

		Map<EspnId, Integer> playerPoints = new HashMap<EspnId, Integer>();
		Map<EspnId, SlotId> playerSlot = new HashMap<EspnId, SlotId>();
		Map<String, EspnId> playerNameMap = new HashMap<String, EspnId>();
		Map<EspnId, BbcTeam> playerTeam = new HashMap<EspnId, BbcTeam>();
		Set<String> pitchers = new HashSet<String>();
		Map<BbcTeam, EspnId> startingPitchers = new HashMap<BbcTeam, EspnId>();

		Map<BbcTeam, Integer> pitchingPoints = new HashMap<BbcTeam, Integer>();

		for (Element boxScoreElement : document.select("[class$=mlb-box]")) {
			handleBoxScoreElement(playerPoints, playerSlot, playerNameMap, playerTeam, pitchers, pitchingPoints, startingPitchers, boxScoreElement);
		}

		handleExtraBox(playerPoints, playerNameMap, pitchers, html);

		Map<Long, Long> teamIds = new HashMap<Long, Long>();
		teamIds.put(bbcGame.getHomeTeamId(), bbcGame.getAwayTeamId());
		teamIds.put(bbcGame.getAwayTeamId(), bbcGame.getHomeTeamId());

		List<BbcPoints> bbcPointses = new ArrayList<BbcPoints>();

		for (Map.Entry<EspnId, Integer> entry : playerPoints.entrySet()) {
			EspnId espnId = entry.getKey();
			int points = entry.getValue();

			long teamId = playerTeam.get(espnId).getId();
			long opponentId = teamIds.get(teamId);

			Boolean homeGame = teamId == bbcGame.getHomeTeamId();

			SlotId slotId = playerSlot.get(espnId);
			if (slotId == null || slotId.equals(0)) {
				BbcPlayer bbcPlayer = bbcPlayerRepository.getBbcPlayerByEspnId(espnId.getId());
				if (bbcPlayer != null) {
					slotId = bbcPlayer.getSlotId();
				}
			}


			BbcPoints bbcPoints = new BbcPoints(date, espnGameId, teamId, opponentId, homeGame, espnId, slotId, points);
			bbcPointses.add(bbcPoints);
		}

		for (Map.Entry<BbcTeam, EspnId> entry : startingPitchers.entrySet()) {
			BbcTeam bbcTeam = entry.getKey();
			EspnId espnId = entry.getValue();

			long teamId = bbcTeam.getId();
			long opponentId = teamIds.get(teamId);

			Boolean homeGame = teamId == bbcGame.getHomeTeamId();

			BbcPoints bbcPoints = new BbcPoints(date, espnGameId, teamId, opponentId, homeGame, espnId, PITCHING_SLOT_ID, pitchingPoints.get(bbcTeam));
			bbcPointses.add(bbcPoints);
		}

		if (bbcPointses.size() > 0) {
			bbcPointsRepository.save(bbcPointses);
		}
	}
}
