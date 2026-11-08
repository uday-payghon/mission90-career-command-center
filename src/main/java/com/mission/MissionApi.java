package com.mission;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** All endpoints require the single owner session, except /api/csrf. */
@RestController
@RequestMapping("/api")
public class MissionApi {
    private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");
    private static final Set<String> TRACKS = Set.of("Java / Spring", "DSA", "Cybersecurity", "Core CS / Design", "Frontend", "Agentic AI", "Placement");
    private final TaskRepository tasks;
    private final JournalRepository journals;
    private final DsaRepository problems;
    private final ApplicationRepository applications;

    MissionApi(TaskRepository tasks, JournalRepository journals, DsaRepository problems, ApplicationRepository applications) {
        this.tasks = tasks;
        this.journals = journals;
        this.problems = problems;
        this.applications = applications;
    }

    @GetMapping("/csrf")
    Map<String,String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @GetMapping("/whoami")
    Map<String,String> whoami(Authentication principal) {
        return Map.of("username", principal.getName(), "mode", "database");
    }

    @GetMapping("/bootstrap")
    Map<String,Object> bootstrap(@RequestParam(required=false) String date) {
        LocalDate d = parseDate(date);
        List<TaskItem> today = tasks.findByDayOrderByIdAsc(d);
        int completeToday = (int) today.stream().filter(t -> t.done).count();
        int plannedMinutes = today.stream().mapToInt(t -> t.minutes).sum();
        int doneMinutes = today.stream().filter(t -> t.done).mapToInt(t -> t.minutes).sum();
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("date", d);
        m.put("start", PlanSeeder.START);
        m.put("end", PlanSeeder.START.plusDays(PlanSeeder.DAYS-1));
        m.put("dayIndex", Math.max(1,Math.min(PlanSeeder.DAYS, ChronoUnit.DAYS.between(PlanSeeder.START,d)+1)));
        m.put("tasks", today);
        m.put("doneToday", completeToday);
        m.put("totalToday", today.size());
        m.put("plannedMinutes", plannedMinutes);
        m.put("doneMinutes", doneMinutes);
        m.put("completedOverall", tasks.countByDoneTrue());
        m.put("totalOverall", tasks.count());
        m.put("totalDsa", problems.count());
        m.put("totalApplications", applications.count());
        m.put("streak", streak(d));
        m.put("journal", journals.findByDay(d).orElse(new DailyJournal(d)));
        return m;
    }

    private int streak(LocalDate day) {
        int n=0;
        // Count days with >= 70% of completed tasks, not a single checkbox.
        for (LocalDate d=day; n < PlanSeeder.DAYS && !d.isBefore(PlanSeeder.START); d=d.minusDays(1)) {
            List<TaskItem> ts=tasks.findByDayOrderByIdAsc(d);
            if(ts.isEmpty() || ts.stream().filter(t->t.done).count() < Math.ceil(ts.size()*0.7)) break;
            n++;
        }
        return n;
    }

    @GetMapping("/roadmap")
    List<Map<String,Object>> roadmap() {
        List<Map<String,Object>> result=new ArrayList<>();
        for(int i=0;i<PlanSeeder.WEEKS.length;i++) {
            String[] a=PlanSeeder.WEEKS[i];
            LocalDate start=PlanSeeder.START.plusDays(i*7);
            LocalDate end=PlanSeeder.START.plusDays(Math.min(PlanSeeder.DAYS-1,i*7+6));
            var weekTasks=tasks.findByDayBetween(start,end);
            Map<String,Object> r=new LinkedHashMap<>();
            r.put("week",i+1);r.put("title",a[0]);r.put("start",start);r.put("end",end);
            r.put("topics",Arrays.asList(Arrays.copyOfRange(a,1,a.length)));
            r.put("done",weekTasks.stream().filter(t->t.done).count());r.put("total",weekTasks.size());
            result.add(r);
        }
        return result;
    }

    @GetMapping("/stats")
    Map<String,Object> stats() {
        List<TaskItem> all=tasks.findAll();
        Map<String,Map<String,Object>> byTrack=new LinkedHashMap<>();
        for(String track:List.of("Java / Spring","DSA","Cybersecurity","Core CS / Design","Frontend","Agentic AI","Placement")) {
            long total=all.stream().filter(t->t.track.equals(track)).count();
            long done=all.stream().filter(t->t.track.equals(track)&&t.done).count();
            byTrack.put(track,Map.of("total",total,"done",done));
        }
        List<Map<String,Object>> days=new ArrayList<>();
        LocalDate now=LocalDate.now(INDIA);
        for(int i=13;i>=0;i--) {
            LocalDate d=now.minusDays(i);
            List<TaskItem> dayTasks=all.stream().filter(t->t.day.equals(d)).toList();
            days.add(Map.of("day",d,"done",dayTasks.stream().filter(t->t.done).count(),"total",dayTasks.size()));
        }
        int studyMinutes=journals.findAll().stream().mapToInt(j->j.actualMinutes).sum();
        LocalDate reference=now.isBefore(PlanSeeder.START)?PlanSeeder.START:now;
        LocalDate weekStart=reference.with(java.time.DayOfWeek.MONDAY), monthStart=reference.withDayOfMonth(1);
        var week=all.stream().filter(t->!t.day.isBefore(weekStart)&&t.day.isBefore(weekStart.plusDays(7))).toList();
        var month=all.stream().filter(t->!t.day.isBefore(monthStart)&&t.day.isBefore(monthStart.plusMonths(1))).toList();
        var upcoming=applications.findAllByOrderByIdDesc().stream().filter(a->
            (a.deadline!=null&&!a.deadline.isBefore(now))||(a.interviewOn!=null&&!a.interviewOn.isBefore(now))||(a.followUpOn!=null&&!a.followUpOn.isBefore(now))).toList();
        return Map.of("tracks",byTrack,"last14",days,"recordedHours",Math.round(studyMinutes/60.0*10.0)/10.0,
            "week",Map.of("total",week.size(),"done",week.stream().filter(t->t.done).count()),
            "month",Map.of("total",month.size(),"done",month.stream().filter(t->t.done).count()),"upcoming",upcoming);
    }

    record TaskPatch(Boolean done,String note,Integer actualMinutes,String priority,String deliverableLink) {}
    @PatchMapping("/tasks/{id}")
    ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskPatch patch) {
        return tasks.findById(id).map(t->{
            if(patch.done()!=null) { t.done=patch.done(); t.completedAt=t.done?LocalDateTime.now():null; }
            if(patch.note()!=null) t.note=clip(patch.note(),1200);
            if(patch.actualMinutes()!=null) t.actualMinutes=bounded(patch.actualMinutes(),0,840);
            if(patch.priority()!=null) t.priority=allowed(patch.priority(),Set.of("Low","Normal","High"));
            if(patch.deliverableLink()!=null) t.deliverableLink=safeUrl(patch.deliverableLink());
            return ResponseEntity.ok(tasks.save(t));
        }).orElseGet(()->ResponseEntity.notFound().build());
    }

    record TaskInput(LocalDate day,String track,String title,String description,Integer minutes) {}
    @PostMapping("/tasks")
    ResponseEntity<?> addCustomTask(@RequestBody TaskInput p) {
        if(p.day()==null || p.day().isBefore(PlanSeeder.START) || p.day().isAfter(PlanSeeder.START.plusDays(PlanSeeder.DAYS-1)))
            return ResponseEntity.badRequest().body(Map.of("error","Choose a date in the 90-day sprint."));
        if(p.track()==null || !TRACKS.contains(p.track()) || p.title()==null || p.title().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","Track and task title are required."));
        TaskItem task=new TaskItem(p.day(),p.track(),clip(p.title().trim(),500),clip(Objects.toString(p.description(),""),2500),
                p.minutes()==null?30:bounded(p.minutes(),5,840));
        task.custom=true;
        return ResponseEntity.ok(tasks.save(task));
    }

    @DeleteMapping("/tasks/{id}")
    ResponseEntity<?> deleteCustomTask(@PathVariable Long id) {
        Optional<TaskItem> task=tasks.findById(id);
        if(task.isEmpty()) return ResponseEntity.notFound().build();
        if(!task.get().custom) return ResponseEntity.badRequest().body(Map.of("error","Only personally added tasks can be deleted."));
        tasks.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    record JournalPatch(Integer energy,String wins,String blockers,String tomorrow,Integer actualMinutes) {}
    @PutMapping("/journal")
    DailyJournal updateJournal(@RequestParam String date,@RequestBody JournalPatch patch) {
        LocalDate d=parseDate(date);
        DailyJournal j=journals.findByDay(d).orElse(new DailyJournal(d));
        if(patch.energy()!=null) j.energy=bounded(patch.energy(),1,5);
        if(patch.wins()!=null) j.wins=clip(patch.wins(),5000);
        if(patch.blockers()!=null) j.blockers=clip(patch.blockers(),5000);
        if(patch.tomorrow()!=null) j.tomorrow=clip(patch.tomorrow(),5000);
        if(patch.actualMinutes()!=null) j.actualMinutes=bounded(patch.actualMinutes(),0,1440);
        return journals.save(j);
    }

    @GetMapping("/dsa") List<DsaProblem> dsa(){return problems.findAllByOrderBySolvedOnDescIdDesc();}
    record DsaInput(String name,String topic,String difficulty,String status,Integer minutes,String link,String insight,String revisionOn) {}
    @PostMapping("/dsa")
    ResponseEntity<?> addDsa(@RequestBody DsaInput p) {
        if(p.name()==null||p.name().isBlank()) return ResponseEntity.badRequest().body(Map.of("error","Problem name required"));
        DsaProblem x=new DsaProblem(); x.name=clip(p.name(),200); x.topic=clip(Objects.toString(p.topic(),"Arrays"),100);
        x.difficulty=allowed(Objects.toString(p.difficulty(),"Easy"),Set.of("Easy","Medium","Hard"));
        x.status=allowed(Objects.toString(p.status(),"Solved"),Set.of("Solved","Solved independently","Solved with hint","Needs retry"));
        x.minutes=p.minutes()==null?0:bounded(p.minutes(),0,1000);
        x.link=safeUrl(p.link()); x.insight=clip(Objects.toString(p.insight(),""),2000);x.revisionOn=parseOptional(p.revisionOn());
        return ResponseEntity.ok(problems.save(x));
    }
    @PutMapping("/dsa/{id}")
    ResponseEntity<?> editDsa(@PathVariable Long id,@RequestBody DsaInput p) {
        if(p.name()==null || p.name().isBlank()) throw new IllegalArgumentException();
        return problems.findById(id).map(x->{
            x.name=clip(p.name(),200);x.topic=clip(Objects.toString(p.topic(),"Arrays"),100);
            x.difficulty=allowed(p.difficulty(),Set.of("Easy","Medium","Hard"));
            x.status=allowed(p.status(),Set.of("Solved","Solved independently","Solved with hint","Needs retry"));
            x.minutes=bounded(p.minutes()==null?0:p.minutes(),0,1000);x.link=safeUrl(p.link());
            x.insight=clip(p.insight(),2000);x.revisionOn=parseOptional(p.revisionOn());
            return ResponseEntity.ok(problems.save(x));
        }).orElseGet(()->ResponseEntity.notFound().build());
    }
    @DeleteMapping("/dsa/{id}")
    ResponseEntity<Void> deleteDsa(@PathVariable Long id) {
        if(!problems.existsById(id)) return ResponseEntity.notFound().build();
        problems.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/applications") List<JobApplication> applications(){return applications.findAllByOrderByIdDesc();}
    record ApplicationInput(String company,String role,String stage,String appliedOn,String deadline,String salary,String link,String notes,String interviewOn,String followUpOn) {}
    @PostMapping("/applications")
    ResponseEntity<?> addApplication(@RequestBody ApplicationInput p) {
        if(p.company()==null||p.company().isBlank()||p.role()==null||p.role().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","Company and role required"));
        JobApplication x=new JobApplication(); x.company=clip(p.company(),200); x.role=clip(p.role(),200);
        x.stage=allowed(Objects.toString(p.stage(),"Interested"),Set.of("Interested","Applied","Online assessment","Interview scheduled","Offer","Rejected")); x.appliedOn=parseOptional(p.appliedOn()); x.deadline=parseOptional(p.deadline());
        x.salary=clip(Objects.toString(p.salary(),""),100); x.link=safeUrl(p.link()); x.notes=clip(Objects.toString(p.notes(),""),2000);
        x.interviewOn=parseOptional(p.interviewOn());x.followUpOn=parseOptional(p.followUpOn());
        return ResponseEntity.ok(applications.save(x));
    }
    @PatchMapping("/applications/{id}")
    ResponseEntity<?> updateApplication(@PathVariable Long id,@RequestBody Map<String,String> fields) {
        return applications.findById(id).map(x->{
            if(fields.containsKey("stage")) x.stage=allowed(fields.get("stage"),Set.of("Interested","Applied","Online assessment","Interview scheduled","Offer","Rejected"));
            if(fields.containsKey("notes")) x.notes=clip(fields.get("notes"),2000);
            if(fields.containsKey("deadline")) x.deadline=parseOptional(fields.get("deadline"));
            if(fields.containsKey("interviewOn")) x.interviewOn=parseOptional(fields.get("interviewOn"));
            if(fields.containsKey("followUpOn")) x.followUpOn=parseOptional(fields.get("followUpOn"));
            if(fields.containsKey("company")) {x.company=clip(fields.get("company"),200);if(x.company.isBlank())throw new IllegalArgumentException();}
            if(fields.containsKey("role")) {x.role=clip(fields.get("role"),200);if(x.role.isBlank())throw new IllegalArgumentException();}
            if(fields.containsKey("link")) x.link=safeUrl(fields.get("link"));
            if(fields.containsKey("salary")) x.salary=clip(fields.get("salary"),100);
            if(fields.containsKey("appliedOn")) x.appliedOn=parseOptional(fields.get("appliedOn"));
            return ResponseEntity.ok(applications.save(x));
        }).orElseGet(()->ResponseEntity.notFound().build());
    }
    @DeleteMapping("/applications/{id}")
    ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        if(!applications.existsById(id)) return ResponseEntity.notFound().build();
        applications.deleteById(id);return ResponseEntity.noContent().build();
    }

    /** Portable owner backup, excluding credentials and session data. */
    @GetMapping(value="/export",produces=MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> exportData() {
        Map<String,Object> backup=new LinkedHashMap<>();
        backup.put("format","mission90-backup-v1");
        backup.put("exportedAt",LocalDateTime.now(INDIA));
        backup.put("tasks",tasks.findAll());
        backup.put("journals",journals.findAll());
        backup.put("dsa",problems.findAll());
        backup.put("applications",applications.findAll());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=mission90-backup.json")
                .header(HttpHeaders.CACHE_CONTROL,"no-store").body(backup);
    }

    private static LocalDate parseDate(String s) {
        if(s==null||s.isBlank()) return LocalDate.now(INDIA);
        return LocalDate.parse(s);
    }
    private static LocalDate parseOptional(String s) {
        return s==null||s.isBlank()?null:LocalDate.parse(s);
    }
    private static String clip(String s,int max) {
        if(s!=null && s.length()>max) throw new IllegalArgumentException("Field exceeds its size limit.");
        return s==null?"":s;
    }
    private static int bounded(int n,int min,int max) {if(n<min||n>max)throw new IllegalArgumentException();return n;}
    private static String allowed(String value,Set<String> values) {if(value==null||!values.contains(value))throw new IllegalArgumentException();return value;}
    static String safeUrl(String value) {
        if(value==null||value.isBlank())return "";
        clip(value,1200);
        try {var uri=java.net.URI.create(value);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null)throw new IllegalArgumentException();return uri.toASCIIString();}
        catch(RuntimeException e){throw new IllegalArgumentException("Use an HTTPS URL without embedded credentials.");}
    }
}
