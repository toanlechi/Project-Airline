package vn.airline.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.airline.authentication.MyDBAuthenticationService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private MyDBAuthenticationService myDBAuthenticationService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	
	@Bean
	public DaoAuthenticationProvider authenticationProvider(){
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(myDBAuthenticationService);
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		return authenticationProvider;
	}
	

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(myDBAuthenticationService);
		auth.authenticationProvider(authenticationProvider());

	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		// Trang không yêu cầu login
		http.authorizeRequests().antMatchers("/", "/home", "/login", "/register", "/logout").permitAll();

		// Trang user-info, chuyển về login nếu chưa đăng nhập
		http.authorizeRequests().antMatchers("/user-info").access("hasAnyRole('ROLE_2', 'ROLE_0')");

		// Trang dành cho admin
		http.authorizeRequests().antMatchers("/admin", "/admin/flight", "/admin/journey", "/admin/airline" ).access("hasAnyRole('ROLE_0')");

		// Ngoại lệ khi truy cập sai permision
		http.authorizeRequests().and().exceptionHandling().accessDeniedPage("/403");

		http.authorizeRequests().and().formLogin().loginProcessingUrl("/j_spring_security_check").loginPage("/login")
				.defaultSuccessUrl("/user-info").failureUrl("/login?error=true").usernameParameter("email")
				.passwordParameter("password").and().logout().logoutUrl("/logout").logoutSuccessUrl("/home");
	}
	

	
}
