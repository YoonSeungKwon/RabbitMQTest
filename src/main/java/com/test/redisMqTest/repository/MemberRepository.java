package com.test.redisMqTest.repository;

import com.test.redisMqTest.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Members, Long> {

    Members findMembersByMemberId(long id);

}
