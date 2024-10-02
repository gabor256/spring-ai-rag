package guru.springframework.springairag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel, VectorStoreProperties vectorStoreProperties) {
        var store = new SimpleVectorStore(embeddingModel);
        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());
        if(vectorStoreFile.exists()) {
            store.load(vectorStoreFile);
        } else {
            log.debug("Loading documents into vector store...");
            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                log.debug("Loading document: " + document.getFilename());
                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> documents = documentReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocs = textSplitter.apply(documents);
                try {
                    store.add(splitDocs);
                } catch (Exception e) {
                    log.error("Error adding documents to the store: {}", e.getMessage());
                }
            });
            store.save(vectorStoreFile);
        }

        return store;
    }
}
