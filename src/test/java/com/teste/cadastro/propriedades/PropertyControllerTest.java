package com.teste.cadastro.propriedades;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository repository; // Repositório real, banco em memória

    @Autowired
    private ObjectMapper objectMapper;

    private int idP1;
    private int idP2;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        PropertyModel p1 = new PropertyModel();
        p1.setNome("Fazenda Modelo 1");
        p1 = repository.save(p1);
        idP1 = p1.getId();

        PropertyModel p2 = new PropertyModel();
        p2.setNome("Fazenda Modelo 2");
        p2 = repository.save(p2);
        idP2 = p2.getId();
    }

    @Nested
    @DisplayName("GET Tests")
    class GetTests {

        @Test
        @DisplayName("GET /property - Deve retornar lista real do banco em memória")
        void findAll() throws Exception {
            mockMvc.perform(get("/property")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Fazenda Modelo 1"));
        }

        @Test
        @DisplayName("GET /property/{id} - Deve retornar propriedade real")
        void findById() throws Exception {
            mockMvc.perform(get("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Fazenda Modelo 1"));
        }

        @Test
        @DisplayName("GET /property/{id} - Deve retornar 404 para id inexistente")
        void findById_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++; // aumenta até achar um id que não exista
            }

            mockMvc.perform(get("/property/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CREATE Tests")
    class CreateTests {

        @Test
        @DisplayName("POST /property - Deve criar nova propriedade com nome único")
        void create() throws Exception {
            PropertyDTO dtoToCreate = new PropertyDTO(null, "Fazenda Nova");

            mockMvc.perform(post("/property")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome").value("Fazenda Nova"));
        }

        @Test
        @DisplayName("POST /property - Deve falhar ao criar propriedade com nome já existente")
        void create_DuplicateName() throws Exception {
            PropertyDTO dtoToCreate = new PropertyDTO(null, "Fazenda Modelo 1");

            mockMvc.perform(post("/property")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /property - Deve falhar ao criar propriedade com nome vazio")
        void create_NameEmpty() throws Exception {
            PropertyDTO dtoToCreate = new PropertyDTO(null, "");

            mockMvc.perform(post("/property")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")));
        }

        @Test
        @DisplayName("POST /property - Deve falhar ao criar propriedade com nome com 2 caracteres")
        void create_NameTooShort() throws Exception {
            PropertyDTO dtoToCreate = new PropertyDTO(null, "AB");

            mockMvc.perform(post("/property")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToCreate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }

        @Test
        @DisplayName("POST /property - Deve falhar ao criar propriedade com nome maior que 100 caracteres")
        void create_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            PropertyDTO dtoToCreate = new PropertyDTO(null, longName);

            mockMvc.perform(post("/property")
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
        @DisplayName("PUT /property/{id} - Deve atualizar propriedade existente")
        void update() throws Exception {
            PropertyDTO dtoToUpdate = new PropertyDTO(null, "Fazenda Atualizada");

            mockMvc.perform(put("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Fazenda Atualizada"));
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve retornar 404 para update em propriedade inexistente")
        void update_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            PropertyDTO dtoToUpdate = new PropertyDTO(null, "Algum Nome");

            mockMvc.perform(put("/property/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve falhar ao atualizar para nome que já existe em outra propriedade")
        void update_DuplicateName() throws Exception {
            PropertyDTO dtoToUpdate = new PropertyDTO(null, "Fazenda Modelo 2");

            mockMvc.perform(put("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve atualizar para mesmo nome atual sem erro")
        void update_SameName() throws Exception {
            PropertyDTO dtoToUpdate = new PropertyDTO(null, "Fazenda Modelo 1");

            mockMvc.perform(put("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Fazenda Modelo 1"));
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve falhar ao atualizar com nome vazio")
        void update_NameEmpty() throws Exception {
            PropertyDTO dtoToUpdate = new PropertyDTO(null, "");

            mockMvc.perform(put("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Nome não pode estar vazio!")));
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve falhar ao atualizar com nome com 2 caracteres")
        void update_NameTooShort() throws Exception {
            PropertyDTO dtoToUpdate = new PropertyDTO(null, "AB");

            mockMvc.perform(put("/property/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dtoToUpdate)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0]").value("Nome deve ter entre 3 e 100 caracteres."));
        }

        @Test
        @DisplayName("PUT /property/{id} - Deve falhar ao atualizar com nome maior que 100 caracteres")
        void update_NameTooLong() throws Exception {
            String longName = "A".repeat(101);
            PropertyDTO dtoToUpdate = new PropertyDTO(null, longName);

            mockMvc.perform(put("/property/" + idP1)
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
        @DisplayName("DELETE /property/{id} - Deve deletar propriedade existente")
        void deleteProperty() throws Exception {
            mockMvc.perform(delete("/property/" + idP1))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /property/{id} - Deve retornar 404 ao deletar propriedade inexistente")
        void delete_NotFound() throws Exception {
            int nonExistentId = 999;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            mockMvc.perform(delete("/property/" + nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }
}