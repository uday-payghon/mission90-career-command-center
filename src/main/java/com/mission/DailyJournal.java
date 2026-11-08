package com.mission;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="daily_journals",uniqueConstraints=@UniqueConstraint(columnNames="journal_day"))
public class DailyJournal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="journal_day",nullable=false) public LocalDate day;
    public int energy=3;
    @Column(length=5000) public String wins="";
    @Column(length=5000) public String blockers="";
    @Column(length=5000) public String tomorrow="";
    public int actualMinutes=0;
    protected DailyJournal(){}
    public DailyJournal(LocalDate d){day=d;}
}
