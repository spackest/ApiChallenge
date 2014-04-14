package com.apichallenge.common.espn.bbc;

public class EspnId {
	private Integer espnId;

	public EspnId(Integer espnId) {
		this.espnId = espnId;
	}

	public Integer getId() {
		return espnId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EspnId espnId1 = (EspnId) o;

		if (espnId != null ? !espnId.equals(espnId1.espnId) : espnId1.espnId != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return espnId != null ? espnId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "EspnId{" +
			"espnId=" + espnId +
			'}';
	}
}
