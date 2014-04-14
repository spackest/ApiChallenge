package com.apichallenge.common.espn.bbc;

public class EspnGameId {
	private Integer espnGameId;

	public EspnGameId(Integer espnGameId) {
		this.espnGameId = espnGameId;
	}

	public Integer getId() {
		return espnGameId;
	}

	@Override
	public String toString() {
		return "EspnGameId{" +
			"espnGameId=" + espnGameId +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EspnGameId that = (EspnGameId) o;

		if (espnGameId != null ? !espnGameId.equals(that.espnGameId) : that.espnGameId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return espnGameId != null ? espnGameId.hashCode() : 0;
	}
}
