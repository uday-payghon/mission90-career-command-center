package com.mission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface TaskRepository extends JpaRepository<TaskItem,Long> { long countByDoneTrue(); boolean existsBySeedKey(String key); List<TaskItem> findByDayOrderByIdAsc(LocalDate day); List<TaskItem> findByDayBetween(LocalDate start,LocalDate end); }
interface JournalRepository extends JpaRepository<DailyJournal,Long> { Optional<DailyJournal> findByDay(LocalDate day); }
interface DsaRepository extends JpaRepository<DsaProblem,Long> { List<DsaProblem> findAllByOrderBySolvedOnDescIdDesc(); }
interface ApplicationRepository extends JpaRepository<JobApplication,Long> { List<JobApplication> findAllByOrderByIdDesc(); }
