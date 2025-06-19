package com.teste.cadastro.laboratorios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.cadastro.propriedades.PropertyDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LaboratoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LaboratoryRepository repository; // Repositório real, banco em memória

    @Autowired
    private ObjectMapper objectMapper;

    private int idP1;
    private int idP2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        LaboratoryModel p1 = new LaboratoryModel();
        p1.setNome("Laboratório Modelo 1");
        p1 = repository.save(p1);
        idP1 = p1.getId();

        LaboratoryModel p2 = new LaboratoryModel();
        p2.setNome("Laboratório Modelo 2");
        p2 = repository.save(p2);
        idP2 = p2.getId();
    }

    @Nested
    @DisplayName("GET Tests")
    class GetTests {

        @Test
        @DisplayName("GET /laboratory - Deve retornar lista real do banco em memória")
        void findAll() throws Exception {
            mockMvc.perform(get("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 1"));
        }

        @Test
        @DisplayName("GET /laboratory/{id} - Deve retornar laboratório real")
        void findById() throws Exception {
            mockMvc.perform(get("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Laboratório Modelo 1"));
        }

        @Test
        @DisplayName("GET /laboratory/{id} - Deve retornar 404 para id inexistente")
        void findById_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++; // aumenta até achar um id que não exista
            }

            mockMvc.perform(get("/laboratory/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CREATE Tests")
    class CreateTests {

        @Test
        @DisplayName("POST /laboratory - Deve criar novo laboratório com nome único")
        void create() throws Exception {
            LaboratoryDTO dtoToCreate = new LaboratoryDTO(null, "Laboratório Novo");

            mockMvc.perform(post("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome").value("Laboratório Novo"));
        }

        @Test
        @DisplayName("POST /laboratory - Deve falhar ao criar laboratório com nome já existente")
        void create_DuplicateName() throws Exception {
            LaboratoryDTO dtoToCreate = new LaboratoryDTO(null, "Laboratório Modelo 1");

            mockMvc.perform(post("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /laboratory - Deve falhar ao criar laboratório com nome vazio")
        void create_NameEmpty() throws Exception {
            LaboratoryDTO dtoToCreate = new LaboratoryDTO(null, "");

            mockMvc.perform(post("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")));
        }

        @Test
        @DisplayName("POST /laboratory - Deve falhar ao criar laboratório com nome com 2 caracteres")
        void create_NameTooShort() throws Exception {
            LaboratoryDTO dtoToCreate = new LaboratoryDTO(null, "AB");

            mockMvc.perform(post("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }

        @Test
        @DisplayName("POST /laboratory - Deve falhar ao criar laboratório com nome maior que 100 caracteres")
        void create_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            LaboratoryDTO dtoToCreate = new LaboratoryDTO(null, longName);

            mockMvc.perform(post("/laboratory")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }
    }

    @Nested
    @DisplayName("UPDATE Tests")
    class UpdateTests {

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve atualizar laboratório existente")
        void update() throws Exception {
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "Laboratório Atualizado");

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Laboratório Atualizado"));
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve retornar 404 para update em laboratório inexistente")
        void update_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "Algum Nome");

            mockMvc.perform(put("/laboratory/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve falhar ao atualizar para nome que já existe em outro laboratório")
        void update_DuplicateName() throws Exception {
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "Laboratório Modelo 2");

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve atualizar para mesmo nome atual sem erro")
        void update_SameName() throws Exception {
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "Laboratório Modelo 1");

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Laboratório Modelo 1"));
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve falhar ao atualizar com nome vazio")
        void update_NameEmpty() throws Exception {
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "");

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")));
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve falhar ao atualizar com nome com 2 caracteres")
        void update_NameTooShort() throws Exception {
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, "AB");

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }

        @Test
        @DisplayName("PUT /laboratory/{id} - Deve falhar ao atualizar com nome maior que 100 caracteres")
        void update_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            LaboratoryDTO dtoToUpdate = new LaboratoryDTO(null, longName);

            mockMvc.perform(put("/laboratory/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }
    }

    @Nested
    @DisplayName("DELETE Tests")
    class DeleteTests {

        @Test
        @DisplayName("DELETE /laboratory/{id} - Deve deletar laboratório existente")
        void deleteLaboratory() throws Exception {
            mockMvc.perform(delete("/laboratory/" + idP1))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /laboratory/{id} - Deve retornar 404 ao deletar laboratório inexistente")
        void delete_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            mockMvc.perform(delete("/laboratory/" + nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }

}