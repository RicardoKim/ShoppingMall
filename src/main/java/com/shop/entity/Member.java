package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.*;

@Entity
@Table(name = "member")
@Getter @Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity{
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Enumerated(EnumType.STRING) //enum을 통해 숫자가 들어가는게 아니라 문자열 자체가 들어가게 된다.
    private Role role;

    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder){
        Member newMember = Member.builder().name(memberFormDto.getName())
                .email(memberFormDto.getEmail())
                .address(memberFormDto.getAddress())
                .password(passwordEncoder.encode(memberFormDto.getPassword()))
                .role(Role.ADMIN)
                .build();
        return newMember;
    }
}
