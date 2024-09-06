package guru.springframework.springairag.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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
    SimpleVectorStore simpleVectorStore(EmbeddingClient embeddingClient, VectorStoreProperties vectorStoreProperties) {
        var store = new SimpleVectorStore(embeddingClient);
        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());
        // TODO: add data

        if(vectorStoreFile.exists()) {
            store.load(vectorStoreFile);
        } else {
            log.debug("Loading documents into vector store...");
            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                log.debug("Loading document: " + document.getFilename());
                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> documents = documentReader.get();
                TokenTextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocs = textSplitter.apply(documents);
                store.add(splitDocs);
            });
            store.save(vectorStoreFile);
        }

        return store;
    }
}
