package com.example.specdriven;

import com.example.specdriven.bracket.TournamentListView;
import com.example.specdriven.security.LoginView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        addClassName("main-layout");

        RouterLink titleLink = new RouterLink("Tournament Bracket", TournamentListView.class);
        titleLink.addClassName("main-layout-title");

        HorizontalLayout sessionArea = new HorizontalLayout();
        sessionArea.addClassName("main-layout-session");
        sessionArea.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        authContext.getAuthenticatedUser(UserDetails.class).ifPresentOrElse(
                user -> {
                    Span signedInAs = new Span("Signed in as " + user.getUsername());
                    signedInAs.addClassName("main-layout-username");

                    Button logoutButton = new Button("Log out", e -> authContext.logout());
                    logoutButton.addThemeVariants(ButtonVariant.TERTIARY);
                    logoutButton.addClassName("main-layout-logout");

                    sessionArea.add(signedInAs, logoutButton);
                },
                () -> {
                    RouterLink loginLink = new RouterLink("Log in", LoginView.class);
                    loginLink.addClassName("main-layout-login");
                    sessionArea.add(loginLink);
                });

        HorizontalLayout navbar = new HorizontalLayout(titleLink, sessionArea);
        navbar.addClassName("main-layout-navbar");
        navbar.setWidthFull();
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.expand(titleLink);

        addToNavbar(navbar);
    }
}
