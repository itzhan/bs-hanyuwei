package com.xiangyongshe.swim_admin;

import com.xiangyongshe.swim_admin.security.AuthUser;
import com.xiangyongshe.swim_admin.user.AdminUserController;
import com.xiangyongshe.swim_admin.user.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminUserController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysUserService sysUserService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private UsernamePasswordAuthenticationToken parentAuth() {
        AuthUser user = new AuthUser(1L, "u", "p",
                List.of(new SimpleGrantedAuthority("ROLE_PARENT")), true);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void adminEndpointShouldBeForbiddenForParent() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(parentAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
