package ru.agniaendie.selsup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final int requestLimit;
    private final TimeUnit timeUnit;
    private final AtomicInteger requestCount;
    private final Object lock = new Object();

    private final CountDownLatch responseLatch = new CountDownLatch(1);


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit < 0) {
            throw new RuntimeException("Non-positive request limit reached : " + requestLimit);
        } else {
            this.requestLimit = requestLimit;
        }
        this.timeUnit = timeUnit;
        this.requestCount = new AtomicInteger(0);
    }

    public void createDocument(Document document, String signature) {
        synchronized (lock) {
            while (requestCount.get() >= requestLimit) {
                try {
                    lock.wait(timeUnit.toMillis(30));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            requestCount.incrementAndGet();
        }

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            String bodyDoc = mapper.writeValueAsString(document);

            RequestBody body = RequestBody.create(mediaType, bodyDoc);
            Request request = new Request.Builder()
                    .url("https://ismp.crpt.ru/api/v3/lk/documents/create")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println(e.getMessage());
                    responseLatch.countDown();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.isSuccessful()) {
                        System.out.println(response);
                    } else {
                        System.out.println("Unsuccessful response" + response);
                    }
                    responseLatch.countDown();
                }
            });
            try {
                responseLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        synchronized (lock) {
            requestCount.decrementAndGet();
            lock.notify();
        }
    }


    //@TODO replace all classes and interfaces under this line to other single files
    public static class Document {
        public Document(DocumentDescription description, String docId, String docStatus, String docType, boolean importRequest, String ownerInn, String participantInn, String producerInn, LocalDate productionDate, String productionType, ArrayList<ProductDTO> products, LocalDate regDate, String regNumber) {
            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;

        }

        @JsonProperty("description")
        DocumentDescription description;
        @JsonProperty("doc_id")
        String docId;
        @JsonProperty("doc_status")
        String docStatus;
        @JsonProperty("doc_type")
        String docType;
        @JsonProperty("importRequest")
        boolean importRequest;
        @JsonProperty("owner_inn")
        String ownerInn;
        @JsonProperty("participant_inn")
        String participantInn;
        @JsonProperty("producer_inn")
        String producerInn;
        @JsonProperty("production_date")
        LocalDate productionDate;
        @JsonProperty("production_type")
        String productionType;
        @JsonProperty("products")
        ArrayList<ProductDTO> products;
        @JsonProperty("reg_date")
        LocalDate regDate;
        @JsonProperty("reg_number")
        String regNumber;
    }

    public static class RussianDocumentBuilder implements DocumentBuilder {

        private CrptApi.DocumentDescription description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private LocalDate productionDate;
        private String productionType;
        private ArrayList<CrptApi.ProductDTO> products;
        private LocalDate regDate;
        private String regNumber;


        public DocumentBuilder setDescription(DocumentDescription description) {
            this.description = description;
            return this;
        }


        public DocumentBuilder setDocId(String docId) {
            this.docId = docId;
            return this;
        }

        @Override
        public DocumentBuilder setDocStatus(String docStatus) {
            this.docStatus = docStatus;
            return this;
        }

        @Override
        public DocumentBuilder setDocType(String docType) {
            this.docType = docType;
            return this;
        }

        @Override
        public DocumentBuilder setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
            return this;
        }

        @Override
        public DocumentBuilder setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
            return this;
        }

        @Override
        public DocumentBuilder setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
            return this;
        }

        @Override
        public DocumentBuilder setProducerInn(String producerInn) {
            this.producerInn = producerInn;
            return this;
        }

        @Override
        public DocumentBuilder setProductionDate(LocalDate productionDate) {
            this.productionDate = productionDate;
            return this;
        }

        @Override
        public DocumentBuilder setProductionType(String productionType) {
            this.productionType = productionType;
            return this;
        }

        @Override
        public DocumentBuilder setProducts(ArrayList<CrptApi.ProductDTO> products) {
            this.products = products;
            return this;
        }

        @Override
        public DocumentBuilder setRegDate(LocalDate regDate) {
            this.regDate = regDate;
            return this;
        }

        @Override
        public DocumentBuilder setRegNumber(String regNumber) {
            this.regNumber = regNumber;
            return this;
        }

        @Override
        public Document build() {
            return new Document(description, docId, docStatus, docType, importRequest, ownerInn, participantInn, producerInn, productionDate, productionType, products, regDate, regNumber);
        }
    }

    public static class DocumentDescription {
        @JsonProperty("participantInn")
        String participantInn;

        public DocumentDescription(String participantInn) {
            this.participantInn = participantInn;
        }
    }


    public interface DocumentBuilder {
        DocumentBuilder setDescription(DocumentDescription desc);

        DocumentBuilder setDocId(String id);

        //Replace with new Enum -> StatusEnum.Status{Typed}
        @Deprecated(since = "1.0")
        DocumentBuilder setDocStatus(String status);

        DocumentBuilder setDocType(String type);

        DocumentBuilder setImportRequest(boolean request);

        DocumentBuilder setOwnerInn(String inn);

        DocumentBuilder setParticipantInn(String inn);

        DocumentBuilder setProducerInn(String inn);

        DocumentBuilder setProductionDate(LocalDate productionDate);

        @Deprecated(since = "1.0")
            //@TODO Replace with new Enum -> StatusEnum.Status{Typed}
        DocumentBuilder setProductionType(String productionType);

        DocumentBuilder setProducts(ArrayList<ProductDTO> products);

        DocumentBuilder setRegDate(LocalDate regDate);

        DocumentBuilder setRegNumber(String regNumber);

        Document build();
    }

    //Needs to add Lombok to cp
    //@AllArgsConstructor
    public static class ProductDTO {
        public ProductDTO(String certificateDocument, LocalDate certificateDocumentDate, String certificateDocumentNumber, String ownerInn, String producerInn, LocalDate productionDate, String tnVedCode, String uiTCode, String uiTUCode) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnVedCode = tnVedCode;
            this.uiTCode = uiTCode;
            this.uiTUCode = uiTUCode;
        }

        @JsonProperty("certificate_document")
        String certificateDocument;
        @JsonProperty("certificate_document_date")
        LocalDate certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        String ownerInn;
        @JsonProperty("producer_inn")
        String producerInn;
        @JsonProperty("production_date")
        LocalDate productionDate;
        @JsonProperty("tnved_code")
        String tnVedCode;
        @JsonProperty("uit_code")
        String uiTCode;
        @JsonProperty("uitu_code")
        String uiTUCode;

    }

    public static class DefaultProductBuilder implements ProductDTOBuilder {

        private String certificateDocument;
        private LocalDate certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private LocalDate productionDate;
        private String tnVedCode;
        private String uiTCode;
        private String uiTUCode;

        public ProductDTOBuilder setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
            return this;
        }

        public ProductDTOBuilder setCertificateDocumentDate(LocalDate certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
            return this;
        }

        public ProductDTOBuilder setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
            return this;
        }

        public ProductDTOBuilder setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
            return this;
        }

        public ProductDTOBuilder setProducerInn(String producerInn) {
            this.producerInn = producerInn;
            return this;
        }

        public ProductDTOBuilder setProductionDate(LocalDate productionDate) {
            this.productionDate = productionDate;
            return this;
        }

        public ProductDTOBuilder setTnVedCode(String tnVedCode) {
            this.tnVedCode = tnVedCode;
            return this;
        }

        public ProductDTOBuilder setUiTCode(String uiTCode) {
            this.uiTCode = uiTCode;
            return this;
        }

        public ProductDTOBuilder setUiTUCode(String uiTUCode) {
            this.uiTUCode = uiTUCode;
            return this;
        }

        public ProductDTO build() {
            return new CrptApi.ProductDTO(certificateDocument, certificateDocumentDate, certificateDocumentNumber, ownerInn, producerInn, productionDate, tnVedCode, uiTCode, uiTUCode);
        }
    }

    public interface ProductDTOBuilder {
        ProductDTOBuilder setCertificateDocument(String certificateDocument);

        ProductDTOBuilder setCertificateDocumentDate(LocalDate certificateDocumentDate);

        ProductDTOBuilder setCertificateDocumentNumber(String certificateDocumentNumber);

        ProductDTOBuilder setOwnerInn(String ownerInn);

        ProductDTOBuilder setProducerInn(String producerInn);

        ProductDTOBuilder setProductionDate(LocalDate productionDate);

        ProductDTOBuilder setTnVedCode(String tnVedCode);

        ProductDTOBuilder setUiTCode(String uiTCode);

        ProductDTOBuilder setUiTUCode(String uiTUCode);

        ProductDTO build();
    }


}


