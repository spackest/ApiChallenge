package com.apichallenge.common.espn;

import com.apichallenge.common.*;
import com.apichallenge.common.espn.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

import java.util.*;
import java.util.regex.*;

public final class EspnUtil {
	private static final String LOGIN_URL = "https://r.espn.go.com/members/util/loginUser";
	private static Pattern entryIdPattern = Pattern.compile("entryID=(\\d+)");

	public static MyConnection login(String username, String password, String parentLocation, String registrationFormId) throws Exception {
		assert (username != null);
		assert (password != null);
		assert (parentLocation != null);
		assert (registrationFormId != null);

		MyConnection myConnection = new MyConnection();

		Map<String, String> form = new HashMap<String, String>();
		form.put("affiliateName", "espn_fantgames");
		form.put("appRedirect", parentLocation);
		form.put("language", "en");
		form.put("parentLocation", parentLocation);
		form.put("username", username);
		form.put("password", password);
		form.put("registrationFormId", registrationFormId);

		myConnection.post(LOGIN_URL, form);

		Connection.Response response = myConnection.get(parentLocation, null);

		//<a class="entryLink" href="entry?entryID=30247">champaroo!</a>
		Document document = response.parse();

		List<EspnEntry> espnEntries = new ArrayList<EspnEntry>();
		for (Element element : document.select("a.entryLink")) {
			Integer id = null;
			String name = element.text();
			String url = element.attr("abs:href");

			Matcher matcher = entryIdPattern.matcher(element.toString());
			if (matcher.find()) {
				id = Integer.valueOf(matcher.group(1));
			}
			assert (id != null);

			espnEntries.add(new EspnEntry(id, name, url));
		}

		assert (espnEntries.size() > 0);

		myConnection.setEspnEntries(espnEntries);

		return myConnection;
	}

	public static EspnEntry getEspnEntry(MyConnection myConnection, String entryName) {
		List<EspnEntry> espnEntries = myConnection.getEspnEntries();

		if (espnEntries == null || espnEntries.size() == 0) {
			throw new IllegalArgumentException("no entries found :(");
		}

		EspnEntry espnEntry = null;

		for (EspnEntry thisEspnEntry : espnEntries) {
			if (thisEspnEntry.getName().equals(entryName)) {
				espnEntry = thisEspnEntry;
				break;
			}
		}

		if (espnEntry == null) {
			throw new IllegalArgumentException("no espnEntry for " + entryName);
		}

		return espnEntry;
	}

	public static void getBoxScores(int year) {


	}
}