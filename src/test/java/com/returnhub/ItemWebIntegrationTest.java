package com.returnhub;

import com.returnhub.model.Item;
import com.returnhub.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ItemWebIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ItemRepository itemRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Homepage should be publicly accessible and render stats and branding")
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Return")))
                .andExpect(content().string(containsString("Hub")))
                .andExpect(content().string(containsString("Recently Reported Lost Items")))
                .andExpect(content().string(containsString("Recently Reported Found Items")));
    }

    @Test
    @DisplayName("Lost directory should be publicly accessible")
    void testLostItemsPage() throws Exception {
        mockMvc.perform(get("/lost-items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/list"))
                .andExpect(content().string(containsString("Lost Directory")));
    }

    @Test
    @DisplayName("Found directory should be publicly accessible")
    void testFoundItemsPage() throws Exception {
        mockMvc.perform(get("/found-items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/list"))
                .andExpect(content().string(containsString("Found Directory")));
    }

    @Test
    @DisplayName("Public Item Details must protect private phone and email from unauthorized public viewers")
    void testItemDetails_ShieldsPrivateContactInfo() throws Exception {
        Item item = itemRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(get("/items/" + item.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("items/details"))
                .andExpect(content().string(containsString(item.getTitle())))
                .andExpect(content().string(containsString("Private Contact Information Protected")))
                // Ensure private phone is NOT exposed in the HTML output
                .andExpect(content().string(not(containsString("tel:" + item.getContactPhone()))));
    }

    @Test
    @DisplayName("Reporting an item requires authentication and redirects anonymous users to login")
    void testReportPage_RequiresAuthentication() throws Exception {
        mockMvc.perform(get("/items/report/lost"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("User registration flow should succeed with valid parameters")
    void testUserRegistrationFlow() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fullName", "Test Registration User")
                        .param("email", "newuser@example.com")
                        .param("phone", "+1555987654")
                        .param("password", "securePassword123")
                        .param("confirmPassword", "securePassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("User registration should fail validation when passwords do not match")
    void testUserRegistration_ValidationFailure() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fullName", "Test User")
                        .param("email", "mismatch@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("Passwords do not match")));
    }
}
