package com.teste.cadastro.laboratorios;

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
class LaboratorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LaboratorioRepository repository; // Repositório real, banco em memória

    @Autowired
    private ObjectMapper objectMapper;

    private int idP1;
    private int idP2;

    @BeforeEach
    void setup() {
        Laboratorio p1 = new Laboratorio();
        p1.setNome("Laboratório Modelo 1");
        p1 = repository.save(p1);
        idP1 = p1.getId();

        Laboratorio p2 = new Laboratorio();
        p2.setNome("Laboratório Modelo 2");
        p2 = repository.save(p2);
        idP2 = p2.getId();
    }

    @Nested
    @DisplayName("GET Tests")
    class GetTests {

        @Test
        @DisplayName("GET /laboratorio - Deve retornar lista real do banco em memória")
        void findAll() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /laboratorio):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /laboratorio/{id} - Deve retornar laboratório real")
        void findById() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Laboratório Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /laboratorio/{id}):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /laboratorio/{id} - Deve retornar 404 para id inexistente")
        void findById_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++; // aumenta até achar um id que não exista
            }

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/" + nonExistentId)
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
        @DisplayName("POST /laboratorio - Deve criar novo laboratório com nome único")
        void create() throws Exception {
            LaboratorioDTO dtoToCreate = new LaboratorioDTO(null, "Laboratório Novo");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/laboratorio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome").value("Laboratório Novo"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /laboratorio - Deve falhar ao criar laboratório com nome já existente")
        void create_DuplicateName() throws Exception {
            LaboratorioDTO dtoToCreate = new LaboratorioDTO(null, "Laboratório Modelo 1");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/laboratorio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (POST /laboratorio):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /laboratorio - Deve falhar ao criar laboratório com nome vazio")
        void create_NameEmpty() throws Exception {
            LaboratorioDTO dtoToCreate = new LaboratorioDTO(null, "");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/laboratorio")
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
        @DisplayName("POST /laboratorio - Deve falhar ao criar laboratório com nome com 2 caracteres")
        void create_NameTooShort() throws Exception {
            LaboratorioDTO dtoToCreate = new LaboratorioDTO(null, "AB");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/laboratorio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /laboratorio):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /laboratorio - Deve falhar ao criar laboratório com nome maior que 100 caracteres")
        void create_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            LaboratorioDTO dtoToCreate = new LaboratorioDTO(null, longName);

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/laboratorio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /laboratorio):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("UPDATE Tests")
    class UpdateTests {

        @Test
        @DisplayName("PUT /laboratorio/{id} - Deve atualizar laboratório existente")
        void update() throws Exception {
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "Laboratório Atualizado");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Laboratório Atualizado"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /laboratorio/{id} - Deve retornar 404 para update em laboratório inexistente")
        void update_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "Algum Nome");

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + nonExistentId)
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
        @DisplayName("PUT /laboratorio/{id} - Deve falhar ao atualizar para nome que já existe em outro laboratório")
        void update_DuplicateName() throws Exception {
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "Laboratório Modelo 2");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (PUT /laboratorio/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /laboratorio/{id} - Deve atualizar para mesmo nome atual sem erro")
        void update_SameName() throws Exception {
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "Laboratório Modelo 1");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Laboratório Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /laboratorio/{id} - Deve falhar ao atualizar com nome vazio")
        void update_NameEmpty() throws Exception {
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
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
        @DisplayName("PUT /laboratorio/{id} - Deve falhar ao atualizar com nome com 2 caracteres")
        void update_NameTooShort() throws Exception {
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, "AB");

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /laboratorio/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /laboratorio/{id} - Deve falhar ao atualizar com nome maior que 100 caracteres")
        void update_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            LaboratorioDTO dtoToUpdate = new LaboratorioDTO(null, longName);

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/laboratorio/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /laboratorio/{id}):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("DELETE Tests")
    class DeleteTests {

        @Test
        @DisplayName("DELETE /laboratorio/{id} - Deve deletar laboratório existente")
        void deleteLaboratorio() throws Exception {
            MvcResult mvcResult = mockMvc.perform(delete("/laboratorio/" + idP1))
                    .andExpect(status().isNoContent())
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /laboratorio/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("DELETE /laboratorio/{id} - Deve retornar 404 ao deletar laboratório inexistente")
        void delete_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            MvcResult mvcResult = mockMvc.perform(delete("/laboratorio/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Exibe a resposta completa para verificar o corpo da resposta
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /laboratorio/{id}):\n" + responseBody);
        }
    }
}