package com.test.redisMqTest.service;

import com.test.redisMqTest.entity.Members;
import com.test.redisMqTest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;




}
