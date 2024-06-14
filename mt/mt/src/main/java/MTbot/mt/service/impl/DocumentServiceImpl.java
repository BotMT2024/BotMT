package MTbot.mt.service.impl;

import MTbot.mt.entity.Document;
import MTbot.mt.repository.DocumentRepository;
import MTbot.mt.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

    @Override
    public List<Document> findAllDocuments() {
        return documentRepository.findAll();
    }
}
