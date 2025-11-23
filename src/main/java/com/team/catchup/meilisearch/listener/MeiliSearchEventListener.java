package com.team.catchup.meilisearch.listener;

import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.listener.event.SyncedIssueMetaDataEvent;
import com.team.catchup.meilisearch.service.MeiliSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
     * DB 트랜잭션 완료 이후 시점(AFTER_COMMIT)에 MeiliSearch Document을 비동기적으로 생성하는 이벤트 핸들러.
     */
    @Async("meiliSearchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIssueSync(SyncedIssueMetaDataEvent event){
        int issueCount = event.syncedIssueMetaDataResponse().issues().size();
        log.info("MeiliSearch 동기화 시작. 대상: {} 건", issueCount);

        try {
            // Jira API 응답 결과를 Document 형식으로 변환
            List<MeiliSearchDocument> documentsToSave = meiliSearchService.createDocuments(
                    event.syncedIssueMetaDataResponse()
            );

            // Document 생성. @Retryable로 실패 시 최대 3번까지 재시도.
            meiliSearchService.addOrUpdateDocument(documentsToSave);

            log.info("MeiliSearch 동기화 완료. 대상: {} 건", issueCount);

        } catch (MeilisearchException e){
            log.error("[handleIssueSync] MeiliSearch 연동 실패 (서버/통신)", e);
        } catch (RuntimeException e) {
            log.error("[handleIssueSync] MeiliSearch 문서 변환/처리 중 논리 오류 발생", e);
        } catch (Exception e) {
            log.error("[handleIssueSync] MeiliSearch 동기화 중 알 수 없는 오류 발생", e);
        }

    }
}
