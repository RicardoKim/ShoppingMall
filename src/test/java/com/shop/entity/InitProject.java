package com.shop.entity;

import com.shop.dto.MemberFormDto;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class InitProject {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void enrollAdmin(){
        MemberFormDto memberFormDto = MemberFormDto.builder()
                .address("경기도")
                .password("123456789")
                .name("admin")
                .email("admin@gmail.com").build();
        Member admin = Member.createMember(memberFormDto, passwordEncoder);
        memberRepository.save(admin);
    }
}
