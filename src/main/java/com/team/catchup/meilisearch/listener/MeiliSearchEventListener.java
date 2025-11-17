package com.team.catchup.meilisearch.listener;

import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.listener.event.SyncedIssueMetaDataEvent;
import com.team.catchup.meilisearch.service.MeiliSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeiliSearchEventListener {
    private final MeiliSearchService meiliSearchService;

    /**
     * Handles synchronization events by converting synced issue metadata into MeiliSearch documents and persisting them after the surrounding database transaction commits.
     *
     * Converts the event's synced issue metadata into a list of MeiliSearchDocument objects and adds or updates them in MeiliSearch. Exceptions during document creation or persistence are caught and logged; they are not propagated and there is currently no retry or recovery implemented.
     *
     * @param event the event containing the synced issue metadata response used to build MeiliSearch documents
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIssueSync(SyncedIssueMetaDataEvent event){
        int issueCount = event.syncedIssueMetaDataResponse().issues().size();
        log.info("[MeiliSearchEventListener][handleIssueSync] document 생성 중: {}개", issueCount);

        try {
            // Jira API 응답 결과를 Document 형식으로 변환
            List<MeiliSearchDocument> documentsToSave = meiliSearchService.createDocuments(
                    event.syncedIssueMetaDataResponse()
            );

            // Document 생성
            meiliSearchService.addOrUpdateDocument(documentsToSave);
            log.info("[MeiliSearchEventListener][handleIssueSync] document 생성 성공: {}개", issueCount);

        } catch (Exception e) {
            // TODO: 재시도 및 복구 로직 추가. DB 저장은 완료된 상태이기 때문에 여기서 예외가 발생하면 데이터 정합성이 깨짐.
            log.error("[MeiliSearchEventListener] document 생성 실패: {}", e.getMessage());
        }

    }
}