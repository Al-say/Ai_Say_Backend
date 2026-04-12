package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.service.business.TopicGeneratorTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:daily-it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "deepseek.api.api-key=dummy-key"
})
class HomeControllerDailyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicGeneratorTask topicGeneratorTask;

    @Test
    void dailyEndpointShouldReturn200ForConsecutiveCalls() throws Exception {
        when(topicGeneratorTask.generateFor(any(LocalDate.class), any(UserPersona.class)))
                .thenThrow(new RuntimeException("mock deepseek unavailable"));

        mockMvc.perform(get("/api/home/daily"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/home/daily"))
                .andExpect(status().isOk());
    }
}
