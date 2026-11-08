package com.mission;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="daily_tasks", indexes={@Index(name="idx_daily_tasks_date",columnList="task_day")})
public class TaskItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="task_day",nullable=false) public LocalDate day;
    @Column(nullable=false) public String track;
    @Column(nullable=false,length=500) public String title;
    @Column(length=2500) public String description;
    public int minutes;
    public boolean done;
    public boolean custom=false;
    @Column(unique=true,length=80) public String seedKey;
    public int actualMinutes=0;
    @Column(length=10) public String priority="Normal";
    @Column(length=1200) public String deliverableLink="";
    @Column(length=1200) public String note;
    public LocalDateTime completedAt;

    protected TaskItem() {}
    public TaskItem(LocalDate day,String track,String title,String description,int minutes){
        this.day=day;this.track=track;this.title=title;this.description=description;this.minutes=minutes;
    }
}
