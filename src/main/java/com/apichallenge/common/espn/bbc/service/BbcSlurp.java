package com.apichallenge.common.espn.bbc.service;

import com.apichallenge.common.*;
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

	private static final Log LOG = LogFactory.getLog(BbcSlurp.class);

	private static final int PITCHING_SLOT_ID = 10;
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

	public void slurpSchedule(int year) {
		List<BbcTeam> teams = bbcTeamRepository.findAll();

		for (BbcTeam team : teams) {
			int newBbcGames = 0;
			int totalBbcGames = 0;

			Set<String> gameExists = new HashSet<String>();

			for (int half : Arrays.asList(1, 2)) {
				String url = getScheduleUrl(year, half, team.getScheduleName());
				String html = SimpleHttpCache.getHtmlFromUrl(url, year == Constants.YEAR);

				Document document = Jsoup.parse(html);

				for (Element element : document.select("[class*=row team]")) {
					String row = element.toString();

					if (row.contains("POSTPONED")) {
						continue;
					}

					Date date = null;

					Matcher matcher = MONTH_DAY_PATTERN.matcher(row);
					if (matcher.find()) {
						int month = MONTHS.get(matcher.group(1));
						int day = Integer.valueOf(matcher.group(2));
						Calendar calendar = new GregorianCalendar(year, month, day);

						date = calendar.getTime();
					}

					String gameStatus = element.select("[class=game-status]").text();

					BbcTeam homeTeam;
					BbcTeam awayTeam;

					BbcTeam opponentTeam = null;

					matcher = SCHEDULE_NAME_PATTERN_TWO.matcher(row);
					if (matcher.find()) {
						String opponentName = matcher.group(1);
						opponentTeam = bbcTeamRepository.findByShortName(opponentName);
					}

					if (opponentTeam == null) {
						throw new IllegalArgumentException("no opponent team for '" + row + "'");
					}

					if (gameStatus.equals("vs")) {
						homeTeam = team;
						awayTeam = opponentTeam;
					} else if (gameStatus.equals("@")) {
						homeTeam = opponentTeam;
						awayTeam = team;
					} else {
						throw new IllegalArgumentException("no game status for '" + row + "'");
					}

					String gameExistsKey = date.toString() + "." + homeTeam.getId() + "." + awayTeam.getId();

					int gameNumber = gameExists.contains(gameExistsKey) ? 2 : 1;
					if (!bbcGameService.gameExists(date, homeTeam, awayTeam, gameNumber)) {
						BbcGame bbcGame = new BbcGame(date, homeTeam, awayTeam, gameNumber);
						bbcGameRepository.save(bbcGame);
					}
					gameExists.add(gameExistsKey);


					Integer espnGameId = null;

					matcher = ESPN_GAME_ID_PATTERN.matcher(element.select("[class=score]").toString());
					if (matcher.find()) {
						espnGameId = Integer.valueOf(matcher.group(1));
						if (bbcGameService.gameExists(espnGameId)) {
							continue;
						}

						try {
							handleEspnGameId(espnGameId);
							BbcGame bbcGame = bbcGameRepository.getBbcGame(date, homeTeam.getId(), awayTeam.getId(), gameNumber);
							bbcGame.setEspnGameId(espnGameId);
							bbcGameRepository.save(bbcGame);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

						newBbcGames++;
					}
					totalBbcGames++;
				}
			}


			LOG.info(team.getName() + " had " + newBbcGames + " new games, " + totalBbcGames + " total games");
		}

	}

	private void handleBoxScoreElement(Map<Integer, Integer> playerPoints, Map<String, Integer> playerNameMap, Set<String> pitchers, Map<BbcTeam, Integer> pitchingPoints, Map<BbcTeam, Integer> startingPitchers, Element boxScoreElement) {
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

			Integer espnId = null;
			String name = null;
			String lcName = null;
			Integer slotId = null;

			if (matcher.find()) {
				espnId = Integer.valueOf(matcher.group(1));
				name = matcher.group(2);
				lcName = name.toLowerCase();
				String slot = matcher.group(3);

				if (slot.equals("P")) {
					continue;
				}

				slotId = BbcPositionEnum.getBbcPositionBySlotShortName(slot).getSlotId();
				bbcPlayerService.perhapsInsertPlayer(espnId, slotId, bbcTeam, name);

				Integer runs = Integer.valueOf(matcher.group(5));
				Integer hits = Integer.valueOf(matcher.group(6));
				Integer rbis = Integer.valueOf(matcher.group(7));
				Integer walks = Integer.valueOf(matcher.group(8));
				int points = runs + hits + rbis + walks;
				playerPoints.put(espnId, points);
				playerNameMap.put(lcName, espnId);
			} else {
				//The following points will be awarded/deducted for each instance one of them take place: 1 out = +1 (complete IP = +3), ER = -3, Hit = -1, BB = -1, K = +1, Win = +5.
				matcher = PITCHER_BOX_PATTERN.matcher(playerElement.toString());
				if (matcher.find()) {
					espnId = Integer.valueOf(matcher.group(1));
					name = matcher.group(2).trim();
					pitchers.add(name);

					if (!startingPitchers.containsKey(bbcTeam)) {
						bbcPlayerService.perhapsInsertPlayer(espnId, PITCHING_SLOT_ID, bbcTeam, name);
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

	private void handleExtraBox(Map<Integer, Integer> playerPoints, Map<String, Integer> playerNameMap, Set<String> pitchers, String html) {
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

					int espnId = playerNameMap.get(lcName);
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

	public void handleEspnGameId(int espnGameId) throws Exception {
		LOG.debug("starting " + espnGameId);

		if (bbcGameService.gameExists(espnGameId)) {
			LOG.info("already complete " + espnGameId);

			return;
		}

		bbcPointsRepository.clearOutEspnGameId(espnGameId);

		String url = "http://espn.go.com/mlb/boxscore?gameId=" + espnGameId;

		String html = SimpleHttpCache.getHtmlFromUrl(url);

		Document document = Jsoup.parse(html);

		Elements gameFinalElements = document.select("[class=game-state]");
		if (gameFinalElements.size() == 0) {
			LOG.info("game with id " + espnGameId + " doesn't appear to be final");
			SimpleHttpCache.wipeCache(url);
			return;
		}

		Map<Integer, Integer> playerPoints = new HashMap<Integer, Integer>();
		Map<String, Integer> playerNameMap = new HashMap<String, Integer>();
		Set<String> pitchers = new HashSet<String>();
		Map<BbcTeam, Integer> startingPitchers = new HashMap<BbcTeam, Integer>();
		Map<BbcTeam, Integer> pitchingPoints = new HashMap<BbcTeam, Integer>();

		for (Element boxScoreElement : document.select("[class$=mlb-box]")) {
			handleBoxScoreElement(playerPoints, playerNameMap, pitchers, pitchingPoints, startingPitchers, boxScoreElement);
		}

		handleExtraBox(playerPoints, playerNameMap, pitchers, html);

		for (Map.Entry<Integer, Integer> entry : playerPoints.entrySet()) {
			BbcPoints bbcPoints = new BbcPoints(espnGameId, entry.getKey(), entry.getValue());
			bbcPointsRepository.save(bbcPoints);
		}

		for (Map.Entry<BbcTeam, Integer> entry : startingPitchers.entrySet()) {
			BbcPoints bbcPoints = new BbcPoints(espnGameId, entry.getValue(), pitchingPoints.get(entry.getKey()));
			bbcPointsRepository.save(bbcPoints);
		}
	}

	public BbcTeamRepository getBbcTeamRepository() {
		return bbcTeamRepository;
	}

	public BbcPlayerRepository getBbcPlayerRepository() {
		return bbcPlayerRepository;
	}
}
