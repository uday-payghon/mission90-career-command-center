package com.mission;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
public class DsaProblem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public LocalDate solvedOn=LocalDate.now(ZoneId.of("Asia/Kolkata"));
    @Column(nullable=false) public String name;
    public String topic="Arrays";
    public String difficulty="Easy";
    public String status="Solved";
    public Integer minutes=0;
    public LocalDate revisionOn;
    @Column(length=1200) public String link="";
    @Column(length=2000) public String insight="";
    protected DsaProblem(){}
}
