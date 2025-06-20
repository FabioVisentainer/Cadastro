package com.teste.cadastro.propriedades;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PropriedadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropriedadeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private int idP1;
    private int idP2;

    @BeforeEach
    void setup() {
        Propriedade p1 = new Propriedade();
        p1.setNome("Fazenda Modelo 1");
        p1 = repository.save(p1);
        idP1 = p1.getId();

        Propriedade p2 = new Propriedade();
        p2.setNome("Fazenda Modelo 2");
        p2 = repository.save(p2);
        idP2 = p2.getId();
    }


    @Nested
    @DisplayName("GET Tests")
    class GetTests {

        @Test
        @DisplayName("GET /propriedade - Deve retornar lista real do banco em memória")
        void findAll() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Fazenda Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /propriedade):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /propriedade/{id} - Deve retornar propriedade real")
        void findById() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Fazenda Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /propriedade/{id}):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /propriedade/{id} - Deve retornar 404 para id inexistente")
        void findById_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++; // aumenta até achar um id que não exista
            }

            MvcResult mvcResult = mockMvc.perform(get("/propriedade/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (GET /pessoa/{id}):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("CREATE Tests")
    class CreateTests {

        @Test
        @DisplayName("POST /propriedade - Deve criar nova propriedade com nome único")
        void create() throws Exception {
            PropriedadeDTO dtoToCreate = new PropriedadeDTO(null, "Fazenda Nova");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome").value("Fazenda Nova"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /propriedade - Deve falhar ao criar propriedade com nome já existente")
        void create_DuplicateName() throws Exception {
            PropriedadeDTO dtoToCreate = new PropriedadeDTO(null, "Fazenda Modelo 1");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (POST /propriedade):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /propriedade - Deve falhar ao criar propriedade com nome vazio")
        void create_NameEmpty() throws Exception {
            PropriedadeDTO dtoToCreate = new PropriedadeDTO(null, "");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /propriedade - Deve falhar ao criar propriedade com nome com 2 caracteres")
        void create_NameTooShort() throws Exception {
            PropriedadeDTO dtoToCreate = new PropriedadeDTO(null, "AB");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com menos de 3 caracteres
            MvcResult mvcResult = mockMvc.perform(post("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /propriedade):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /propriedade - Deve falhar ao criar propriedade com nome maior que 100 caracteres")
        void create_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            PropriedadeDTO dtoToCreate = new PropriedadeDTO(null, longName);

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/propriedade")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /propriedade):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("UPDATE Tests")
    class UpdateTests {

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve atualizar propriedade existente")
        void update() throws Exception {
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "Fazenda Atualizada");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Fazenda Atualizada"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve retornar 404 para update em propriedade inexistente")
        void update_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "Algum Nome");

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (PUT /propriedade/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve falhar ao atualizar para nome que já existe em outra propriedade")
        void update_DuplicateName() throws Exception {
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "Fazenda Modelo 2");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (PUT /propriedade/{id}):\n" + responseBody);

        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve atualizar para mesmo nome atual sem erro")
        void update_SameName() throws Exception {
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "Fazenda Modelo 1");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Fazenda Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve falhar ao atualizar com nome vazio")
        void update_NameEmpty() throws Exception {
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve falhar ao atualizar com nome com 2 caracteres")
        void update_NameTooShort() throws Exception {
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, "AB");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com menos de 3 caracteres
            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /propriedade/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /propriedade/{id} - Deve falhar ao atualizar com nome maior que 100 caracteres")
        void update_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            PropriedadeDTO dtoToUpdate = new PropriedadeDTO(null, longName);

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com mais de 100 caracteres
            MvcResult mvcResult = mockMvc.perform(put("/propriedade/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /pessoa/{id}):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("DELETE Tests")
    class DeleteTests {

        @Test
        @DisplayName("DELETE /propriedade/{id} - Deve deletar propriedade existente")
        void deletePropriedade() throws Exception {
            MvcResult mvcResult = mockMvc.perform(delete("/propriedade/" + idP1))
                    .andExpect(status().isNoContent())
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /propriedade/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("DELETE /propriedade/{id} - Deve retornar 404 ao deletar propriedade inexistente")
        void delete_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            MvcResult mvcResult = mockMvc.perform(delete("/propriedade/" + nonExistentId))
                    .andExpect(status().isNotFound()) // Espera que a resposta seja 404 (não encontrado)
                    .andReturn();

            // Exibe a resposta completa para verificar o corpo da resposta
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /propriedade/{id}):\n" + responseBody);
        }
    }
}