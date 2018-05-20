package uniruse.mse.examregistration.subject;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uniruse.mse.examregistration.user.model.ApplicationUser;

@Entity
@Table(name = "subject")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Subject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	private String description;

	@ManyToMany()
	@JoinTable(name = "subject_professor",
		joinColumns = @JoinColumn(name = "subject_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
	private List<ApplicationUser> professors = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
    @JsonIgnore
	public List<ApplicationUser> getProfessors() {
		return professors;
	}

	public void setProfessors(List<ApplicationUser> professors) {
		this.professors = professors;
	}

}
