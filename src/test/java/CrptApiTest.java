import org.junit.jupiter.api.Test;
import ru.agniaendie.selsup.CrptApi;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


public class CrptApiTest {
    @Test
    public void testCrptApi() throws InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 10);
        ExecutorService service = Executors.newFixedThreadPool(4);
        IntStream.range(0, 100).forEach(
                thread -> service.submit(test(api))
        );


        service.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    public Thread test(CrptApi api) {
        return new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            Random random = new Random();
                            String signature = "test" + random.nextInt();
                            api.createDocument(prepareDocument(), signature);
                        }
                    }
                }
        );
    }

    public ArrayList<CrptApi.ProductDTO> prepareProduct(Integer ctn) {
        ArrayList<CrptApi.ProductDTO> list = new ArrayList<>();

        //can be replaced with for-each loop
        for (int i = 0; i < ctn; i++) {
            CrptApi.ProductDTOBuilder productDTOBuilder = new CrptApi.DefaultProductBuilder();
            productDTOBuilder.setCertificateDocument("string");
            productDTOBuilder.setCertificateDocumentDate(LocalDate.parse("2020-01-23"));
            productDTOBuilder.setCertificateDocumentNumber("string");
            productDTOBuilder.setOwnerInn("string");
            productDTOBuilder.setProducerInn("string");
            productDTOBuilder.setProductionDate(LocalDate.parse("2020-01-23"));
            productDTOBuilder.setTnVedCode("string");
            productDTOBuilder.setUiTCode("string");
            productDTOBuilder.setUiTUCode("string");
            list.add(productDTOBuilder.build());
        }
        return list;
    }

    public CrptApi.Document prepareDocument() {
        CrptApi.DocumentBuilder builder = new CrptApi.RussianDocumentBuilder();
        builder.setDescription(new CrptApi.DocumentDescription("string"));
        builder.setDocId("string");
        builder.setDocStatus("string");
        builder.setDocType("string");
        builder.setImportRequest(true);
        builder.setOwnerInn("string");
        builder.setParticipantInn("string");
        builder.setProducerInn("string");
        builder.setProductionDate(LocalDate.parse("2020-01-23"));
        builder.setProductionType("string");
        builder.setProducts(prepareProduct(1));
        builder.setRegDate(LocalDate.parse("2020-01-23"));
        builder.setRegNumber("string");
        return builder.build();
    }

}
