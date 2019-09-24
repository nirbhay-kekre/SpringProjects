package edu.sjsu.cmpe275.api.model;

import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
//@Entity
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonProperty("id")
	private long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("email")
	private String email;

	@JsonProperty("title")
	private String title;

	//@Embedded
	@JsonProperty("address")
	private Address address;

	@JsonProperty("employer")
	private Employer employer;

	@JsonProperty("manager")
	private Employee manager;

	@JsonProperty("reports")
	private List<Employee> reports;

	@JsonProperty("collaborators")
	private List<Employee> collaborators;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Employer getEmployer() {
		return employer;
	}

	public void setEmployer(Employer employer) {
		this.employer = employer;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		manager = manager;
	}

	public List<Employee> getReports() {
		return reports;
	}

	public void setReports(List<Employee> reports) {
		this.reports = reports;
	}

	public List<Employee> getCollaborators() {
		return collaborators;
	}

	public void setCollaborators(List<Employee> collaborators) {
		this.collaborators = collaborators;
	}

}
