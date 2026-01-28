package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.RecentlyReadIssue;
import com.team.catchup.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentlyReadIssueRepository extends JpaRepository<RecentlyReadIssue, Long> {

    List<RecentlyReadIssue> findTop3ByMemberOrderByLastViewedAtDesc(Member member);
}
