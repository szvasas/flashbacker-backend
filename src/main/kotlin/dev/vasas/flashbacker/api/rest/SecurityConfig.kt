package dev.vasas.flashbacker.api.rest

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

/**
 * This bean overrides the auto configuration in
 * [org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerJwtConfiguration.OAuth2WebSecurityConfigurerAdapter].
 * and adds the following settings to the defaults:
 *
 * - Permits unauthenticated CORS requests
 * - Permits unauthenticated requests to Spring Boot Actuator endpoints (this is not an issue since
 * the Actuator endpoints are available on a different port which is not exposed in Kubernetes)
 * - Makes sure that we do not manage sessions for the clients
 *
 * This bean must not be created when running automated tests since they use mock security configuration.
 */
@Configuration
@Profile("!test")
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests().antMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").permitAll()
                .anyRequest().authenticated()
            .and()
                .oauth2ResourceServer().jwt()
    }

}
