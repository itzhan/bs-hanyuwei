package com.xiangyongshe.swim_admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiangyongshe.swim_admin.baby.BabyController;
import com.xiangyongshe.swim_admin.baby.BabyProfile;
import com.xiangyongshe.swim_admin.baby.BabyProfileService;
import com.xiangyongshe.swim_admin.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BabyController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class BabyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BabyProfileService babyProfileService;

    private UsernamePasswordAuthenticationToken auth() {
        AuthUser user = new AuthUser(1L, "u", "p",
                List.of(new SimpleGrantedAuthority("ROLE_PARENT")), true);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void listQueryShouldReturnOk() throws Exception {
        Page<BabyProfile> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(babyProfileService.page(any(Page.class), any())).thenReturn(page);

        mockMvc.perform(get("/api/babies")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createValidationShouldFail() throws Exception {
        mockMvc.perform(post("/api/babies")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth()))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
