package com.apichallenge.common.espn.bbc.entity;

import javax.persistence.*;

@MappedSuperclass
public class ParentEntity<T> {
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "G1")
	@SequenceGenerator(name = "G1", sequenceName = "ID_GENERATOR")
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@Version
	@Column(name = "VERSION")
	private int version;

	public ParentEntity() {
		// no-arg constructor required by JPA
	}

	public ParentEntity(Long id) {
		setId(id);
	}

	public Long getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	public T setId(Long id) {
		this.id = id;
		return (T) (this);
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ParentEntity that = (ParentEntity) o;

		if (version != that.version) return false;
		if (id != null && !id.equals(that.id)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + version;
		return result;
	}

	@Override
	public String toString() {
		return "ParentEntity{" +
			"id=" + id +
			", version=" + version +
			'}';
	}
}
