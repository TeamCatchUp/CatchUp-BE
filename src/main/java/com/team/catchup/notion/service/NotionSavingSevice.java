package com.team.catchup.notion.service;

import com.team.catchup.notion.repository.NotionPageRepository;
import com.team.catchup.notion.entity.NotionPage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotionSavingSevice {

    private final NotionPageRepository notionPageRepository;

    public int saveAllPages(List<NotionPage> pages) {
        int savedCount = 0;

        for (NotionPage page : pages) {
            if(savePageIfNotExists(page)) {
                savedCount++;
            }
        }

        log.info("[NOTION][BATCH SAVE] Page Saved - Saved: {}, Total: {}", savedCount, pages.size());
        return savedCount;
    }

    public boolean savePageIfNotExists(NotionPage page) {

        if(notionPageRepository.existsById(page.getPageId())) {
            log.debug("[NOTION][SKIP] Page Already saved: {}", page.getPageId());
            return false;
        }

        notionPageRepository.save(page);
        log.debug("[NOTION][SAVE] Page Saved: {}", page.getPageId());

        return true;
    }
}
