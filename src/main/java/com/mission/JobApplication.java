package com.mission;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class JobApplication {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String company;
    @Column(nullable=false) public String role;
    public String stage="Interested";
    public LocalDate appliedOn;
    public LocalDate deadline;
    public LocalDate interviewOn;
    public LocalDate followUpOn;
    public String salary="";
    @Column(length=1200) public String link="";
    @Column(length=2000) public String notes="";
    protected JobApplication(){}
}
