package com.example.elhabashyback.configuration.seed;

import com.example.elhabashyback.user.entity.Role;
import com.example.elhabashyback.user.repoistory.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAccountSeederIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedsTheDefaultAdminAccount() {
        var admin = userRepository.findByEmailIgnoreCase("mostafa.mahmoudegy10@gmail.com").orElseThrow();

        assertThat(admin.getFirstName()).isEqualTo("Mostafa");
        assertThat(admin.getLastName()).isEqualTo("Mahmoud");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getEnabled()).isTrue();
        assertThat(passwordEncoder.matches("password", admin.getPassword())).isTrue();
    }

    @Test
    void seededAdminCanLoginAndAccessAdminApis() throws Exception {
        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "mostafa.mahmoudegy10@gmail.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.enabled").value(true))
                .andReturn();

        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
}
