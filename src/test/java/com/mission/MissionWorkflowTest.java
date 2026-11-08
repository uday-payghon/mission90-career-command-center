package com.mission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
@TestPropertySource(properties="spring.datasource.url=jdbc:h2:mem:workflows;DB_CLOSE_DELAY=-1")
class MissionWorkflowTest {
    static final String PASSWORD=UUID.randomUUID().toString();
    @DynamicPropertySource static void configuration(DynamicPropertyRegistry r){r.add("APP_PASSWORD",()->PASSWORD);}
    @Autowired MockMvc mvc;
    @Autowired TaskRepository tasks;
    @Autowired ObjectMapper json;

    @Test void passwordLoginAndLogoutWork() throws Exception {
        var result=mvc.perform(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin("/login")
            .user("uday").password(PASSWORD)).andExpect(status().is3xxRedirection()).andReturn();
        var session=(MockHttpSession)result.getRequest().getSession(false);
        mvc.perform(get("/api/whoami").session(session)).andExpect(jsonPath("$.username").value("uday"));
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        assertTrue(session.isInvalid());
    }

    @Test @WithMockUser(roles="USER") void nonOwnerCannotAccessPrivateRecords() throws Exception {
        mvc.perform(get("/api/export")).andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks").with(csrf()).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles="OWNER")
    @org.springframework.test.annotation.DirtiesContext(methodMode=org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD)
    void realCsrfCookieAndHeaderWork() throws Exception {
        var response=mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        String token=json.readTree(response.getContentAsString()).get("token").asText();
        Long id=tasks.findByDayOrderByIdAsc(PlanSeeder.START).get(0).id;
        mvc.perform(patch("/api/tasks/"+id).cookie(response.getCookie("XSRF-TOKEN")).header("X-XSRF-TOKEN",token)
            .contentType("application/json").content("{\"done\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.done").value(true));
    }

    @Test @WithMockUser(roles="OWNER") void taskDetailsAndUncheckPersist() throws Exception {
        Long id=tasks.findByDayOrderByIdAsc(PlanSeeder.START).get(0).id;
        mvc.perform(patch("/api/tasks/"+id).with(csrf()).contentType("application/json")
            .content("{\"done\":true,\"actualMinutes\":45,\"priority\":\"High\",\"deliverableLink\":\"https://github.com/example/project\",\"note\":\"Tested edge cases\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.actualMinutes").value(45));
        mvc.perform(patch("/api/tasks/"+id).with(csrf()).contentType("application/json").content("{\"done\":false}"))
            .andExpect(jsonPath("$.done").value(false)).andExpect(jsonPath("$.completedAt").isEmpty())
            .andExpect(jsonPath("$.priority").value("High")).andExpect(jsonPath("$.note").value("Tested edge cases"));
        mvc.perform(delete("/api/tasks/"+id).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles="OWNER") void dsaCreateEditDelete() throws Exception {
        String body="{\"name\":\"Two Sum\",\"difficulty\":\"Easy\",\"status\":\"Needs retry\",\"minutes\":20,\"revisionOn\":\"2026-11-12\"}";
        var result=mvc.perform(post("/api/dsa").with(csrf()).contentType("application/json").content(body))
            .andExpect(status().isOk()).andReturn();
        long id=json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/dsa/"+id).with(csrf()).contentType("application/json")
            .content(body.replace("Two Sum","Two Sum revised").replace("Needs retry","Solved independently")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Two Sum revised"))
            .andExpect(jsonPath("$.revisionOn").value("2026-11-12"));
        mvc.perform(delete("/api/dsa/"+id).with(csrf())).andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles="OWNER") void applicationCreateEditDatesDelete() throws Exception {
        var result=mvc.perform(post("/api/applications").with(csrf()).contentType("application/json")
            .content("{\"company\":\"Test organization\",\"role\":\"Engineer\",\"interviewOn\":\"2027-01-10\",\"followUpOn\":\"2027-01-11\"}"))
            .andExpect(status().isOk()).andReturn();
        long id=json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/applications/"+id).with(csrf()).contentType("application/json")
            .content("{\"stage\":\"Interview scheduled\",\"notes\":\"Prepare SQL\",\"role\":\"Java Engineer\",\"interviewOn\":\"2027-01-12\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("Java Engineer"))
            .andExpect(jsonPath("$.followUpOn").value("2027-01-11")).andExpect(jsonPath("$.interviewOn").value("2027-01-12"));
        mvc.perform(delete("/api/applications/"+id).with(csrf())).andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles="OWNER") void unsafeInputsFailWithoutPartialSave() throws Exception {
        for(String body:List.of("{}","{\"track\":null,\"title\":\"x\",\"day\":\"2026-11-09\"}",
            "{\"track\":\"DSA\",\"title\":\"x\",\"day\":\"2026-11-09\",\"minutes\":-1}")) {
            mvc.perform(post("/api/tasks").with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        for(String url:List.of("javascript:alert(1)","http://example.com","https://user:invalid@example.com")) {
            mvc.perform(post("/api/dsa").with(csrf()).contentType("application/json")
                .content(json.writeValueAsString(Map.of("name","Test","link",url)))).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/dsa").with(csrf()).contentType("application/json")
            .content(json.writeValueAsString(Map.of("name","x".repeat(201))))).andExpect(status().isBadRequest());
    }

    @Test void curriculumHasNinetyDaysAndCorrectWorkload() {
        var all=tasks.findAll();assertEquals(630,all.size());assertEquals(90,all.stream().map(t->t.day).distinct().count());
        assertEquals(LocalDate.of(2027,2,6),PlanSeeder.START.plusDays(89));
        for(int day=0;day<90;day++) {
            var date=PlanSeeder.START.plusDays(day);var items=all.stream().filter(t->t.day.equals(date)).toList();
            int minutes=items.stream().mapToInt(t->t.minutes).sum();assertTrue(minutes>=600&&minutes<=720,date.toString());
            if(date.getDayOfWeek().getValue()>=6)assertEquals(180,items.stream().filter(t->t.track.equals("Agentic AI")).findFirst().orElseThrow().minutes);
            if(!date.isBefore(LocalDate.of(2027,1,1)))assertTrue(items.stream().filter(t->t.track.equals("Placement")).findFirst().orElseThrow().minutes>=minutes*.35);
        }
    }

    @Test void reseedingPreservesExistingProgress() throws Exception {
        var task=tasks.findByDayOrderByIdAsc(PlanSeeder.START).get(0);task.done=true;task.note="Keep progress";tasks.saveAndFlush(task);
        new PlanSeeder().seed(tasks).run();assertEquals(630,tasks.count());
        assertTrue(tasks.findById(task.id).orElseThrow().done);assertEquals("Keep progress",tasks.findById(task.id).orElseThrow().note);
    }

    @Test void productionRejectsUnsafeConfiguration() {
        var config=new SecurityConfig();var encoder=config.passwordEncoder();
        assertThrows(IllegalStateException.class,()->config.userDetailsService("uday",PASSWORD,encoder,"prod","jdbc:h2:mem:test","","",false,false));
        assertThrows(IllegalStateException.class,()->config.userDetailsService("uday","",encoder,"dev","","","",false,false));
    }
}
