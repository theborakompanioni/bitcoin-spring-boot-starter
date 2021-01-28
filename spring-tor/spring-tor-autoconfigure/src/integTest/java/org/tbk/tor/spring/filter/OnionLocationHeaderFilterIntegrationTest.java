package org.tbk.tor.spring.filter;

import com.google.common.collect.ImmutableMap;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "server.port=13337",
        "org.tbk.tor.onion-location-header.allow-on-localhost-http=false"
})
public class OnionLocationHeaderFilterIntegrationTest {

    @SpringBootApplication
    public static class HiddenServiceTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(HiddenServiceTestApplication.class)
                    .web(WebApplicationType.SERVLET)
                    .run(args);
        }

        @RestController
        public static class HelloTestCtrl {
            @GetMapping(path = "/hello")
            public ResponseEntity<? extends Map<String, Object>> hello() {
                Map<String, Object> result = ImmutableMap.<String, Object>builder()
                        .put("message", "hello world.")
                        .build();

                return ResponseEntity.ok(result);
            }
        }
    }

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private HiddenServiceDefinition hiddenService;

    @Test
    public void itShouldNotAddHeaderIfRequestIsNotSecure() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(header().doesNotExist("Onion-Location"))
                .andExpect(jsonPath("message").value("hello world."))
                .andExpect(status().isOk());
    }

    @Test
    public void itShouldAddHeaderIfRequestIsSecure() throws Exception {
        String expectedHeaderValue = String.format("http://%s/hello", hiddenService.getVirtualHostOrThrow());

        mockMvc.perform(get("/hello").secure(true))
                .andExpect(header().string("Onion-Location", expectedHeaderValue))
                .andExpect(jsonPath("message").value("hello world."))
                .andExpect(status().isOk());
    }
}