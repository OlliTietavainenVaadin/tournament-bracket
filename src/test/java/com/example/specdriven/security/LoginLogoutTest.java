package com.example.specdriven.security;

import com.example.specdriven.MainLayout;
import com.example.specdriven.admin.AdminView;
import com.example.specdriven.admin.ManageTournamentsView;
import com.example.specdriven.signup.SignUpView;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LoginLogoutTest extends SpringBrowserlessTest {

    @Autowired
    private AuthenticationContext authenticationContext;

    @Test
    @WithAnonymousUser
    void anonymousUserCanReachLoginView() {
        navigate(LoginView.class);
        LoginForm form = $(LoginForm.class).first();
        assertNotNull(form, "Login form should render");
    }

    @Test
    @WithAnonymousUser
    void anonymousUserBlockedFromAdminRoutes() {
        assertThrows(Exception.class, () -> navigate(ManageTournamentsView.class),
                "Anonymous access to admin routes should be denied");
        assertThrows(Exception.class, () -> navigate(AdminView.class),
                "Anonymous access to /admin should be denied");
    }

    @Test
    @WithAnonymousUser
    void anonymousUserBlockedFromSignupRoute() {
        assertThrows(Exception.class, () -> navigate(SignUpView.class),
                "Anonymous access to /signup should be denied");
    }

    @Test
    @WithAnonymousUser
    void mainLayoutShowsLoginLinkWhenAnonymous() {
        MainLayout layout = new MainLayout(authenticationContext);
        boolean hasLoginLink = $(RouterLink.class, layout).all().stream()
                .anyMatch(l -> "Log in".equals(l.getText()));
        assertTrue(hasLoginLink, "Anonymous top bar should show a Log in link");

        boolean hasLogoutButton = $(Button.class, layout).all().stream()
                .anyMatch(b -> "Log out".equals(b.getText()));
        assertFalse(hasLogoutButton, "Anonymous top bar should not show a Log out button");
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void adminCanAccessAdminRoutes() {
        ManageTournamentsView view = navigate(ManageTournamentsView.class);
        assertNotNull(view);
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void mainLayoutShowsUsernameAndLogoutForAdmin() {
        MainLayout layout = new MainLayout(authenticationContext);
        boolean showsUsername = $(Span.class, layout).all().stream()
                .anyMatch(s -> s.getText() != null && s.getText().contains("admin"));
        assertTrue(showsUsername, "Top bar should show the signed-in username");

        boolean hasLogoutButton = $(Button.class, layout).all().stream()
                .anyMatch(b -> "Log out".equals(b.getText()));
        assertTrue(hasLogoutButton, "Top bar should show a Log out button");
    }

    @Test
    @WithMockUser(username = "player1", password = "player1", roles = "USER")
    void playerCanAccessSignupRoute() {
        SignUpView view = navigate(SignUpView.class);
        assertNotNull(view);
    }

    @Test
    @WithMockUser(username = "player1", password = "player1", roles = "USER")
    void playerCannotAccessAdminRoutes() {
        assertThrows(Exception.class, () -> navigate(ManageTournamentsView.class),
                "Players (role USER) must not access admin routes");
        assertThrows(Exception.class, () -> navigate(AdminView.class),
                "Players (role USER) must not access /admin");
    }

    @Test
    @WithMockUser(username = "player3", password = "player3", roles = "USER")
    void mainLayoutShowsUsernameAndLogoutForPlayer() {
        MainLayout layout = new MainLayout(authenticationContext);
        boolean showsUsername = $(Span.class, layout).all().stream()
                .anyMatch(s -> s.getText() != null && s.getText().contains("player3"));
        assertTrue(showsUsername, "Top bar should show the player's username");

        boolean hasLogoutButton = $(Button.class, layout).all().stream()
                .anyMatch(b -> "Log out".equals(b.getText()));
        assertTrue(hasLogoutButton, "Top bar should show a Log out button");
    }
}
