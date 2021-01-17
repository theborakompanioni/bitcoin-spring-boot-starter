package org.tbk.tor.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.Matchers.is;
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
        // here we just check if the response is well-formed
        // after the start the hidden service might not yet be reachable and return 503 DOWN
        // if we are lucky we get 200 UP - but this is not important in this test case
        mockMvc.perform(get("/actuator/health/hiddenService"))
                .andExpect(jsonPath("status").value(either(is("UP")).or(is("DOWN"))))
                .andExpect(jsonPath("details.name").exists())
                .andExpect(jsonPath("details.host").exists())
                .andExpect(jsonPath("details.port").exists())
                .andExpect(jsonPath("details.virtual_host").exists())
                .andExpect(jsonPath("details.virtual_port").exists())
                .andExpect(status().is(either(is(WebEndpointResponse.STATUS_OK))
                        .or(is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE))));
    }

    @Test
    public void itShouldAddHiddenServiceInformationToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(jsonPath("status").value(either(is("UP")).or(is("DOWN"))))
                .andExpect(jsonPath("components.hiddenService.status").value(either(is("UP")).or(is("DOWN"))))
                .andExpect(jsonPath("components.hiddenService.details.name").exists())
                .andExpect(jsonPath("components.hiddenService.details.host").exists())
                .andExpect(jsonPath("components.hiddenService.details.port").exists())
                .andExpect(jsonPath("components.hiddenService.details.virtual_host").exists())
                .andExpect(jsonPath("components.hiddenService.details.virtual_port").exists())
                .andExpect(status().is(either(is(WebEndpointResponse.STATUS_OK))
                        .or(is(WebEndpointResponse.STATUS_SERVICE_UNAVAILABLE))));
    }
}