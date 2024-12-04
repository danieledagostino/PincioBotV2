package com.pincio.telegramwebhook;

import com.pincio.telegramwebhook.service.AIResponseService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AIResponseServiceTest {

    private final AIResponseService aiResponseService = new AIResponseService();

    @Test
    public void testCosineSimilarity_SameLength() {
        // Vettori di esempio con lunghezza uguale
        double[] vectorA = {1, 2, 3};
        double[] vectorB = {4, 5, 6};

        // Calcolo della similarità
        double result = aiResponseService.cosineSimilarity(vectorA, vectorB);

        // Verifica che il risultato sia un valore valido (ad esempio tra -1 e 1)
        assertNotNull(result);
        assertTrue(result >= -1.0 && result <= 1.0, "La similarità deve essere compresa tra -1 e 1");
    }

    @Test
    public void testCosineSimilarity_SpecificResult() {
        double[] vectorA = {1, 0, 0};
        double[] vectorB = {0, 1, 0};

        // I due vettori sono ortogonali, quindi il risultato atteso è 0
        double result = aiResponseService.cosineSimilarity(vectorA, vectorB);
        assertEquals(0.0, result, 0.0001, "La similarità dovrebbe essere 0 per vettori ortogonali");
    }

}
