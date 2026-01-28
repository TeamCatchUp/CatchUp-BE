package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.JiraIssueResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.entity.RecentlyReadIssue;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
import com.team.catchup.jira.repository.RecentlyReadIssueRepository;
import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JiraIssueService {

    private final IssueMetaDataRepository issueMetaDataRepository;
    private final RecentlyReadIssueRepository recentlyReadIssueRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveRecentlyReadIssues(Long memberId, Integer issueId) {
        Member member = getMember(memberId);
        IssueMetadata issueMetadata = getIssueMetadata(issueId);
        List<RecentlyReadIssue> history = recentlyReadIssueRepository.findTop3ByMemberOrderByLastViewedAtDesc(member);

        for (RecentlyReadIssue record : history) {
            if (record.getIssueMetadata().getIssueId().equals(issueId)) {
                record.updateLastViewedAt();
                return;
            }
        }

        if (history.size() < 3) {
            recentlyReadIssueRepository.save(RecentlyReadIssue.of(member, issueMetadata));
            return;
        }

        RecentlyReadIssue oldestRecord = history.get(history.size() - 1);

        oldestRecord.replaceIssue(issueMetadata);
    }

    @Transactional(readOnly = true)
    public List<JiraIssueResponse> getRecentlyReadIssues(Long memberId) {

        Member member = getMember(memberId);

        List<RecentlyReadIssue> history = recentlyReadIssueRepository.findTop3ByMemberOrderByLastViewedAtDesc(member);

        return history.stream()
                .map(record -> {
                    IssueMetadata issue = record.getIssueMetadata();

                    List<String> parentSummaries = new ArrayList<>();
                    Integer parentId = issue.getParentIssueId();
                    if (parentId != null) {
                        issueMetaDataRepository.findSummaryByIssueId(parentId)
                                .ifPresent(parentSummaries::add);
                    }

                    List<String> childrenSummaries = issueMetaDataRepository.findChildSummariesByParentId(issue.getIssueId());

                    return JiraIssueResponse.of(issue, parentSummaries, childrenSummaries);
                })
                .toList();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private IssueMetadata getIssueMetadata(Integer issueId) {
        return issueMetaDataRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Jira 이슈입니다."));
    }
}
