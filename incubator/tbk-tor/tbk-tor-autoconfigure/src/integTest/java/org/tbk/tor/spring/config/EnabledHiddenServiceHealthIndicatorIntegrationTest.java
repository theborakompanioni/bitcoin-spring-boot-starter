package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=21211",
        "management.port=21211",
        "management.server.port=21211",
        "management.endpoint.health.show-details=always",
        "management.health.hiddenService.enabled=true"
})
public class EnabledHiddenServiceHealthIndicatorIntegrationTest {

    @SpringBootApplication
    public static class HiddenServiceTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(HiddenServiceTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }
    }


    @Autowired
    private MockMvc mockMvc;

    @Test
    public void itShouldCheckHiddenServiceHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health/hiddenService"))
                .andExpect(jsonPath("status").value("UP"))
                .andExpect(jsonPath("details.name").exists())
                .andExpect(jsonPath("details.address").exists())
                .andExpect(jsonPath("details.local_address").exists())
                .andExpect(status().isOk());
    }

    @Test
    public void itShouldAddHiddenServiceInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(jsonPath("status").value("UP"))
                .andExpect(jsonPath("components.hiddenService.status").value("UP"))
                .andExpect(jsonPath("components.hiddenService.details.name").exists())
                .andExpect(jsonPath("components.hiddenService.details.address").exists())
                .andExpect(jsonPath("components.hiddenService.details.local_address").exists())
                .andExpect(status().isOk());
    }

    @Test
    public void itShouldAddHiddenServiceInformationToInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(jsonPath("defaultHiddenService.name").exists())
                .andExpect(jsonPath("defaultHiddenService.address").exists())
                .andExpect(jsonPath("defaultHiddenService.local_address").exists())
                .andExpect(status().isOk());
    }
}