package com.mission;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(properties={"spring.datasource.url=jdbc:h2:mem:missiontest;DB_CLOSE_DELAY=-1"})
class MissionApiIntegrationTest {
    @org.springframework.test.context.DynamicPropertySource
    static void password(org.springframework.test.context.DynamicPropertyRegistry registry) {
        String value=java.util.UUID.randomUUID().toString();registry.add("APP_PASSWORD",()->value);
    }
    @Autowired MockMvc mvc;
    @Autowired TaskRepository taskRepo;

    @Test void loginIsRequiredForPrivateRecords() throws Exception {
        mvc.perform(get("/api/bootstrap").param("date","2026-11-09"))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void seededTasksAreAvailable() throws Exception {
        mvc.perform(get("/api/bootstrap").param("date","2026-11-09"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.totalToday").value(7))
           .andExpect(jsonPath("$.tasks",hasSize(7)))
           .andExpect(jsonPath("$.completedOverall").value(greaterThanOrEqualTo(0)));
    }

    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void ownerCanCreateAndReadPersonalTasks() throws Exception {
        mvc.perform(post("/api/tasks").with(csrf()).contentType("application/json")
            .content("{\"day\":\"2026-11-09\",\"track\":\"DSA\",\"title\":\"Practice binary search\",\"minutes\":45}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.custom").value(true));
        mvc.perform(get("/api/bootstrap").param("date","2026-11-09"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.tasks",hasSize(8)));
    }

    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void exportAndStatisticsAreAvailableToOwner() throws Exception {
        mvc.perform(get("/api/export"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.format").value("mission90-backup-v1"));
        mvc.perform(get("/api/stats"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.tracks.DSA.total").exists());
    }

    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void invalidDateReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/bootstrap").param("date","not-a-date"))
           .andExpect(status().isBadRequest());
    }
    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void checkboxAndJournalChangesAreStoredInDatabase() throws Exception {
        Long id=taskRepo.findByDayOrderByIdAsc(LocalDate.of(2026,11,9)).get(0).id;
        mvc.perform(patch("/api/tasks/{id}",id).with(csrf()).contentType("application/json")
            .content("{\"done\":true,\"note\":\"Completed my first session\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.done").value(true));
        mvc.perform(get("/api/bootstrap").param("date","2026-11-09"))
            .andExpect(jsonPath("$.doneToday").value(greaterThanOrEqualTo(1)));
        mvc.perform(put("/api/journal").param("date","2026-11-09").with(csrf())
            .contentType("application/json")
            .content("{\"wins\":\"Built Java CLI\",\"blockers\":\"Time complexity\",\"actualMinutes\":420}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/bootstrap").param("date","2026-11-09"))
            .andExpect(jsonPath("$.journal.actualMinutes").value(420))
            .andExpect(jsonPath("$.journal.wins").value("Built Java CLI"));
    }

    @Test @WithMockUser(username="uday",roles={"OWNER"})
    void csrfTokenRequiredOnMutations() throws Exception {
        Long id=taskRepo.findByDayOrderByIdAsc(LocalDate.of(2026,11,9)).get(0).id;
        mvc.perform(patch("/api/tasks/{id}",id).contentType("application/json")
            .content("{\"done\":true}"))
            .andExpect(status().isForbidden());
    }

}
