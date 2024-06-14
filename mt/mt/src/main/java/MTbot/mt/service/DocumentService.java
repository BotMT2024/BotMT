package MTbot.mt.service;

import MTbot.mt.entity.Document;

import java.util.List;

public interface DocumentService {
    List<Document> findAllDocuments();
}
