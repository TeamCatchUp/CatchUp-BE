package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachment, Integer> {
}
