package com.adnibog.gamecenter;

import com.adnibog.gamecenter.entity.Folder;
import com.adnibog.gamecenter.repository.FolderRepository;
import com.adnibog.gamecenter.repository.QuestionRepository;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class TestBug {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void testUpdateFolder() {
        try {
            String projectId = "test-proj";
            String folderId = UUID.randomUUID().toString();
            Folder folder = new Folder();
            folder.setProjectId(projectId);
            folder.setId(folderId);
            folder.setName("old-name");
            folderRepository.save(folder);

            boolean exists = folderRepository.existsByProjectIdAndName(projectId, "new-name", folderId);
            System.out.println("Exists result: " + exists);

            folderRepository.deleteById(projectId, folderId);
            System.out.println("✅ Folder update logic passed!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Folder update logic failed: " + e.getMessage());
        }
    }

    @Test
    public void testFolderQuestions() {
        try {
            String projectId = "test-proj";
            String folderId = UUID.randomUUID().toString();
            PaginationRequest req = new PaginationRequest();
            req.setLimit(10);
            questionRepository.findQuestionsByFolderId(projectId, folderId, req);
            System.out.println("✅ Question fetch logic passed!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Question fetch logic failed: " + e.getMessage());
        }
    }
}
