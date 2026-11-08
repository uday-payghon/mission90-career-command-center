package com.mission;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class PlanSeeder {
    public static final LocalDate START=LocalDate.of(2026,11,9);
    public static final int DAYS=90;
    public static final String[][] WEEKS={
        {"Java launch", "JDK, JVM, variables, loops, methods, basic I/O", "Arrays and strings, dry runs", "Internet, IP, DNS, basic HTTP", "Computer basics, Git, CLI", "HTML and semantic forms", "Agentic AI foundations"},
        {"Java foundations", "Classes, methods, collections, exceptions", "Two pointers and string frequency", "Linux, TCP/UDP and packet basics", "OOP pillars, memory, stack/heap", "CSS layouts, flex/grid", "LLM, tokens, prompts"},
        {"OOP + DSA fluency", "Interfaces, generics, streams, unit testing", "HashMap, sliding window", "Web security fundamentals, headers", "SQL SELECT, joins, indexes", "JavaScript fundamentals", "Tools versus agents"},
        {"Spring Boot starts", "Spring Initializr, DI, MVC, REST APIs", "Sorting, binary search", "HTTP auth, OWASP basics", "Java concurrency basics and ACID", "React components and state", "RAG overview and embeddings"},
        {"Backend with data", "PostgreSQL, Spring Data JPA, DTOs, validation", "Linked lists and stacks", "SQL injection prevention, error design", "SQL schema and query design", "React forms and API calls", "Vector stores and retrieval"},
        {"Secure backend", "Spring Security, sessions, authorization, tests", "Queues, recursion, trees", "IDOR, broken access control and CSRF", "Networking: TLS, HTTP/2, reverse proxies", "React routing, authentication UI", "RAG chunking and retrieval"},
        {"Full-stack integration", "Integrate React with secure Java APIs", "Trees, BST and heap", "VAPT on local lab and secure code review", "LLD: SOLID, UML and patterns", "React dashboards", "AI agent workflows"},
        {"Systems and deployment", "Dockerfile, GitHub Actions, deploy, logs", "Graphs, BFS and DFS", "Dependency scanning and secrets", "HLD: load balancers, cache, queues", "Responsive UI and accessibility", "RAG prototype and citations"},
        {"Security engineering", "Rate limits, audit logging, auth tests", "Intro DP, backtracking", "OWASP API Top 10, threat models", "SOC: logs, alerts, SIEM fundamentals", "Security dashboard polish", "Prompt injection and RAG authorization"},
        {"Interview sprint I", "Java interview coding and Spring troubleshooting", "Timed mixed DSA practice", "VAPT report and API security explanations", "OS, DBMS, networking rapid-fire", "Frontend interview tasks", "Agent systems and evaluations"},
        {"Interview sprint II", "Spring Boot architecture and API testing", "Trees, graphs, medium problems", "SOC event triage and defensive workflows", "LLD problems and system design basics", "Portfolio UX polish", "Explain RAG architecture"},
        {"Interview sprint III", "Performance, queries and security review", "Timed mock assessments", "Security mock interview and remediation", "HLD tradeoffs, caching, availability", "Project deployment review", "Agentic AI design interview"},
        {"Offer conversion", "Debugging and project walkthroughs", "Weak area retry and mock OAs", "Secure coding mock", "Core CS revision and whiteboard design", "UI polishing and resume proof", "RAG demo and limitations"}
    };

    // Seven concrete daily outputs for each of the thirteen curriculum weeks.
    // Start with beginner-level Java in week 1; shift to interviews in January.
    private static final String[][] JAVA_DAILY={
        {"Install JDK21, compile HelloWorld, explain JVM/JRE/JDK","Write a calculator using primitives and Scanner","Practice if/else, loops; build multiplication-table CLI","Write reusable methods; test edge inputs","Build an array statistics CLI and push GitHub commit","Refactor Java basics and explain code aloud","Solve Java quiz and rebuild calculator without notes"},
        {"Create Student class with constructor and access modifiers","Practice encapsulation with BankAccount class","Model inheritance using Employee and Developer","Implement interfaces and polymorphic payments","Use exceptions and custom validation on user input","Build a mini contact manager in core Java","Review OOP: explain 4 pillars in 8 minutes"},
        {"Collections: ArrayList versus LinkedList benchmark","Build Map frequency counter for strings","Use generics and Comparator to sort objects","Streams filter/map/groupBy on transaction data","Write JUnit 5 tests for Java utility methods","Practice Java concurrency with Runnable and Executor","Refactor mini app and explain performance tradeoffs"},
        {"Generate Spring Boot project; GET /api/health returns JSON","Build Controller, Service and Repository layers","Add POST /api/incidents; understand DTO versus Entity","Implement validation and clear 400/404 error responses","Create Java REST test suite with MockMvc","Review dependency injection and bean lifecycle","Demo API live and draw MVC request flow"},
        {"Design Incident and User tables in PostgreSQL","Add Spring Data JPA entities and repositories","Build create/read/update endpoints for incidents","Implement pagination, sorting and query parameters","Use DTO mapping and avoid overexposing entities","Write SQL indexes and EXPLAIN query observations","Integration test persistence and revise N+1 problem"},
        {"Configure Spring Security and private endpoints","Implement session login, bcrypt password verification","Add USER/ADMIN role authorization and 403 tests","Test ownership checks to prevent IDOR","Implement CSRF protection and test failures","Review JWT-versus-session design with threat model","Run authorization test suite; document attack surfaces"},
        {"Create React Vite client and fetch incident list","Build incident submission form with validation","Connect secure login UI and session flow","Render error/loading states accessibly","Add search/filter and responsive mobile layout","Review cross-origin versus same-origin integration","Record full-stack demo; capture README screenshots"},
        {"Write production multi-stage Dockerfile","Build CI workflow with Maven tests and lint","Deploy Java backend to free cloud with Postgres","Configure HTTPS, secure cookies and health endpoint","Add structured logs and safe exception responses","Design database backup/recovery and README runbook","Test restart persistence and document architecture"},
        {"Implement audit log on sensitive changes","Add rate-limit strategy and review request throttling","Review secrets and dependency vulnerability scanning","Fix top three security findings from local tests","Write service-level secure design decision record","Threat-model RAG and authorization boundaries","Present a 10-minute secure API project walkthrough"},
        {"Timed 45-min Java coding mock + explanation","Debug failing Spring Boot service and write fix","Explain Dependency Injection, JPA and transactions","Design REST pagination and idempotent PUT handlers","Simulate code review and answer security questions","Record technical mock interview and note gaps","Retake failed Java and backend interview questions"},
        {"Build a 45-minute Spring CRUD task from scratch","SQL join/index interview and query optimization","Explain Hibernate lazy loading and transaction scopes","Implement validated input and authorization quickly","Refactor Controller-Service-Repository live coding","Review RAG integration design and privacy boundaries","Mock panel: Java + Spring + project security"},
        {"Complete timed online assessment with Java","Practice concurrency, locks and thread safety","Explain caching, rate limiting and retry design","Audit secure Spring endpoints with threat checklist","Optimize slow SQL query using indexes and profiling","Present full solution and tradeoffs without slides","Rewrite weak answers and fix incorrect coding patterns"},
        {"Review top 25 Java interview questions from notes","Finish final Java and Spring project walkthrough","Complete a timed Java + SQL mock round","Fix all remaining project bugs and tests","Practice offer and HR questions with real examples","Review GitHub portfolio and follow up with recruiters","Final 90-day reflection, application review and next plan"}
    };

    private static final String[][] DSA_DAILY={
        {"Arrays: find maximum and second maximum","Arrays: reverse in place and remove duplicates","Arrays: rotate array and move zeroes","Strings: reverse words and check palindrome","Strings: count vowels and first nonrepeating char","Retry two missed array/string problems","Timed 30-min arrays/strings assessment"},
        {"Two pointers: pair sum on sorted array","Two pointers: valid palindrome and merge sorted arrays","Sliding window: max sum subarray length k","Sliding window: longest unique substring introduction","Strings: anagram and frequency-map practice","Retry two difficult two-pointer questions","Timed easy/medium string mini assessment"},
        {"HashMap: Two Sum and frequency count","HashSet: find duplicates and intersection","Prefix sum: range sum and equilibrium index","Sliding window: longest substring without repeats","Sort objects then solve interval merge basics","Reattempt wrong hash map and prefix-sum questions","Timed 40-min arrays/hash map OA"},
        {"Sorting: bubble versus insertion; Big-O","Sorting: merge sort hand trace","Binary search: standard template and boundaries","Binary search: first/last occurrence","Binary search on answer: minimum feasible rate","Reattempt binary-search edge-case questions","Timed mixed sorting/search mock"},
        {"Linked list: traversal and reversal","Linked list: find middle using fast/slow pointers","Linked list: detect cycle","Stack: valid parentheses","Stack: next greater element","Implement custom stack and linked list in Java","Timed linked-list/stack mini mock"},
        {"Queues: circular queue and deque","Recursion: factorial, power and stack frames","Binary trees: preorder/inorder/postorder","Binary trees: max depth and node count","Binary search tree insert/search","Revisit recursive stack mistakes","Timed tree and queue assessment"},
        {"Tree traversal iterative with stack","BST: lowest common ancestor introduction","Heap: Kth largest element","PriorityQueue: top K frequent numbers","Two pointers: 3Sum conceptual approach","Review tree, heap and stack weak areas","Mixed DSA 60-min timed assessment"},
        {"Graph adjacency lists in Java","BFS shortest path in unweighted graph","DFS and connected components","Detect cycle in undirected graph","Topological sort and indegree basics","Rework BFS/DFS code without watching tutorial","Timed graph and tree mock"},
        {"DP: Fibonacci memoization vs tabulation","DP: climbing stairs and house robber","Backtracking: generate subsets","Backtracking: permutations","DP: coin change introduction","Review graph/DP state and transition","Timed mixed OA and mistake log"},
        {"Timed array/hash map OA: 2 questions","Timed binary-search OA: 2 questions","Timed linked-list/stack OA: 2 questions","Timed tree/BST OA: 2 questions","Timed graph OA: 2 questions","Retry three failed interview problems","Mock coding interview with verbal explanation"},
        {"Two pointers medium + explain invariants","Sliding window medium + complexity","Tree traversal medium + dry run","Graph BFS medium + complexity","DP easy/medium + state explanation","Retry two mistakes without solutions","Mock coding interview focused on clarity"},
        {"Company-style timed OA 60 minutes","Review high-frequency array/string patterns","Review binary search + sorting edge cases","Review stack/queue + linked list patterns","Review tree/graph patterns","Review DP/backtracking and tradeoffs","Timed 90-minute mixed OA and debrief"},
        {"Retry 3 most frequently failed questions","Timed pair of easy/medium Java questions","Interview-style tree/graph practice","Fast revision of hashmap and two pointers","Final timed algorithm assessment","Explain five solutions out loud without IDE","Make DSA revision schedule for February"}
    };

    @Bean CommandLineRunner seed(TaskRepository tasks) {
        return args->{
            List<TaskItem> all=new ArrayList<>();
            var existing=tasks.findAll().stream().map(t->t.seedKey).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            List<TaskItem> missing=new ArrayList<>();
            for(int i=0;i<DAYS;i++){
                LocalDate date=START.plusDays(i);int w=Math.min(WEEKS.length-1,i/7);
                String[] x=WEEKS[w];int weekday=(int)(i%7);String[] routine={"Study foundations; create and explain a minimal example", "Implement from memory; commit a focused exercise", "Apply the concept to a practical feature", "Debug three mistakes and add tests", "Build a small deliverable and explain tradeoffs", "Follow weekend course and review weak areas", "Timed mini-assessment, recap and plan next week"};boolean weekend=date.getDayOfWeek()==DayOfWeek.SATURDAY||date.getDayOfWeek()==DayOfWeek.SUNDAY;
                String phase=i<21?"Learn and implement one small exercise. Commit the code.":i<56?"Extend the secure incident-reporting portfolio. Add a test.":"Practice with interview-quality explanations and measurable deliverables.";
                all.add(new TaskItem(date,"Java / Spring",JAVA_DAILY[w][weekday],phase,weekend?120:180));
                all.add(new TaskItem(date,"DSA",DSA_DAILY[w][weekday],"Solve or implement in Java. Write complexity, edge cases and failed approaches; retry mistakes next day.",weekend?90:120));
                all.add(new TaskItem(date,"Cybersecurity",x[3]+": "+routine[weekday],"Owned systems/authorized labs only. Day "+(i+1)+": record the finding, impact, mitigation and one defensive test.",weekend?60:75));
                all.add(new TaskItem(date,"Core CS / Design",x[4]+": "+routine[weekday],"Draw one diagram, write a query/example and explain five questions without notes.",weekend?60:75));
                all.add(new TaskItem(date,"Frontend",x[5]+": "+routine[weekday],"Build one visible UI deliverable; verify keyboard access, mobile layout and error states.",weekend?30:75));
                all.add(new TaskItem(date,"Agentic AI",weekend?"Purchased weekend course + implementation: "+x[6]:x[6],weekend?"Take course notes and implement one measurable RAG/agent experiment.":"Read, test, or implement one concept; list its limits.",weekend?180:45));
                all.add(new TaskItem(date,"Placement",i<14?"Set up resume, GitHub and job watch; review one legitimate role":"Apply or follow up on relevant Java/security roles and do one interview drill","Log actions in Applications; verify deadlines with recruiter or placement cell.",weekend?60:60));
            }
            for(TaskItem task:all) {
                if(!task.day.isBefore(LocalDate.of(2027,1,1))) {
                    boolean weekend=task.day.getDayOfWeek()==DayOfWeek.SATURDAY||task.day.getDayOfWeek()==DayOfWeek.SUNDAY;
                    task.minutes=switch(task.track) {
                        case "Java / Spring" -> weekend?105:150;
                        case "DSA" -> weekend?90:120;
                        case "Cybersecurity" -> weekend?30:45;
                        case "Core CS / Design" -> weekend?30:45;
                        case "Frontend" -> weekend?15:30;
                        case "Agentic AI" -> weekend?180:45;
                        default -> weekend?255:240;
                    };
                    if(task.track.equals("Placement")) {task.title="Timed OA, mock interview, verified applications and portfolio explanation";task.description="90-minute OA + 45-minute mock + 45-minute applications/referrals + 60-minute project explanation. Log results and revise weak answers.";}
                }
                task.seedKey=task.day+":"+task.track;
                if(!existing.contains(task.seedKey)) missing.add(task);
            }
            // One transaction avoids partial visible curricula and per-record transaction overhead.
            tasks.saveAll(missing);
        };
    }
}
