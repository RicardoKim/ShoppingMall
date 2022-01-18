package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration // Spring에서 설정 파일을 만들기 위한 어노테이션 or 빈을 등록하기 위한 어노테이션이다. 이때 Configuration은 싱클톤 보장을 해준다.
@EnableWebSecurity // EnableWebSecurity 어노테이션을 가지고 WebSecurityConfigurerAdpater를 상속받은 클래스는 SpringSecurityFilterChain에 자동적으로 등록된다
public class SecurityConfig extends WebSecurityConfigurerAdapter{
    @Autowired
    MemberService memberService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.formLogin()// 인증 설정하는 부분
                .loginPage("/members/login")
                .defaultSuccessUrl("/") // 성공하면 메인페이지로 돌아가는 것을 의미한다.
                .usernameParameter("email") //
                .failureUrl("/members/login/error")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) //특정한 경로를 지정한다.
                .logoutSuccessUrl("/");
        http.authorizeRequests()//권한 설정 부분
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll() //모든 사용자가 접근 가능하도록 한다.
                .mvcMatchers("/admin/**").hasRole("ADMIN") //admin으로 시작하는 모든 경로는 ADMIN role일 경우에만 접근 가능하도록 한다.
                .anyRequest().authenticated(); //위의 경로가 아닌 모든 경로는 인증을 요구하도록 설정한다.

        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()); //인증되지 않은 사용자가 접근을 할 때 수행되는 handler를 등록한다.
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        // AuthenticationManagerBuilder는 스프링 시큐리티의 인증에 대한 지원을 설정하는 몇가지 메소드를 가지고 있다.
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**"); //다음과 같이 되어있는 하위파일은 인증을 무시하도록 설정한다.
    }
}
