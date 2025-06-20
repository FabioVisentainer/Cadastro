package com.teste.cadastro.pessoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.cadastro.laboratorios.Laboratorio;
import com.teste.cadastro.laboratorios.LaboratorioDTO;
import com.teste.cadastro.propriedades.PropriedadeDTO;
import org.springframework.test.web.servlet.MvcResult;
import com.teste.cadastro.laboratorios.LaboratorioRepository;
import com.teste.cadastro.propriedades.Propriedade;
import com.teste.cadastro.propriedades.PropriedadeRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository repository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Laboratorio laboratorioTeste;
    private Propriedade propriedadeTeste;

    private Laboratorio laboratorioTeste2;
    private Propriedade propriedadeTeste2;

    private Long idP1;
    private Long idP2;

    @BeforeAll
    void criarLaboratorioEPropriedade() {
        repository.deleteAll();
        laboratorioRepository.deleteAll();
        propriedadeRepository.deleteAll();

        laboratorioTeste = new Laboratorio();
        laboratorioTeste.setNome("Laboratório de Teste");
        laboratorioTeste = laboratorioRepository.save(laboratorioTeste);

        laboratorioTeste2 = new Laboratorio();
        laboratorioTeste2.setNome("Laboratório de Teste 2");
        laboratorioTeste2 = laboratorioRepository.save(laboratorioTeste);

        propriedadeTeste = new Propriedade();
        propriedadeTeste.setNome("Propriedade de Teste");
        propriedadeTeste = propriedadeRepository.save(propriedadeTeste);

        propriedadeTeste2 = new Propriedade();
        propriedadeTeste2.setNome("Propriedade de Teste 2");
        propriedadeTeste2 = propriedadeRepository.save(propriedadeTeste);
    }

    @AfterAll
    void limparBancoDeDados() {
        // Limpar o banco após a execução de todos os testes
        repository.deleteAll();
        laboratorioRepository.deleteAll();
        propriedadeRepository.deleteAll();
    }

    @BeforeEach
    void limparEPovoarPessoas() {
        repository.deleteAll();

        Pessoa pessoa1 = new Pessoa();
        pessoa1.setNome("Pessoa Teste 1");
        pessoa1.setDataInicial(ZonedDateTime.now().minusDays(10));
        pessoa1.setDataFinal(ZonedDateTime.now().minusDays(5));
        pessoa1.setLaboratorio(laboratorioTeste);
        pessoa1.setInfosPropriedade(propriedadeTeste);
        pessoa1.setObservacoes("Observação pessoa 1");
        pessoa1 = repository.save(pessoa1);
        idP1 = pessoa1.getId();

        Pessoa pessoa2 = new Pessoa();
        pessoa2.setNome("Pessoa Teste 2");
        pessoa2.setDataInicial(ZonedDateTime.now().minusDays(8));
        pessoa2.setDataFinal(ZonedDateTime.now().minusDays(3));
        pessoa2.setLaboratorio(laboratorioTeste);
        pessoa2.setInfosPropriedade(propriedadeTeste);
        pessoa2.setObservacoes("Observação pessoa 2");
        pessoa2 = repository.save(pessoa2);
        idP2 = pessoa2.getId();
    }

    @Nested
    @DisplayName("Persistência e deleção de entidades relacionadas")
    class EntidadesRelacionadasTests {

        @Test
        @DisplayName("Deve persistir pessoas com laboratório e propriedade vinculados")
        void devePersistirPessoasComLaboratorioEPropriedade() {
            // Buscar todas as pessoas cadastradas (devem ter sido criadas no setup)
            List<Pessoa> pessoas = repository.findAll();

            assertFalse(pessoas.isEmpty(), "Deve existir pessoas cadastradas");

            // Verificar se todas as pessoas têm laboratório e propriedade vinculados
            for (Pessoa pessoa : pessoas) {
                assertNotNull(pessoa.getLaboratorio(), "Pessoa deve ter laboratório vinculado");
                assertNotNull(pessoa.getInfosPropriedade(), "Pessoa deve ter propriedade vinculada");
            }
        }

        @Test
        @DisplayName("Não deve ser possível deletar laboratório/propriedade que estejam vinculados a pessoas")
        void naoDeveDeletarLaboratorioOuPropriedadeComVinculo() {
            // Tenta deletar o laboratório que está vinculado a pessoas
            Exception labException = assertThrows(DataIntegrityViolationException.class, () -> {
                laboratorioRepository.delete(laboratorioTeste);
                laboratorioRepository.flush();
            });
            System.out.println("Exceção esperada ao deletar laboratório vinculado: " + labException.getMessage());

            // Tenta deletar a propriedade que está vinculada a pessoas
            Exception propException = assertThrows(DataIntegrityViolationException.class, () -> {
                propriedadeRepository.delete(propriedadeTeste);
                propriedadeRepository.flush();
            });
            System.out.println("Exceção esperada ao deletar propriedade vinculada: " + propException.getMessage());
        }
    }

    @Nested
    @DisplayName("GET Tests")
    class GetTests {

        @Test
        @DisplayName("GET /pessoa - Deve retornar lista real do banco em memória")
        void findAll() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].nome").value("Pessoa Teste 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /pessoa):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /pessoa/{id} - Deve retornar pessoa real")
        void findById() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Pessoa Teste 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON (GET /pessoa/{id}):\n" + prettyJson);
        }

        @Test
        @DisplayName("GET /pessoa/{id} - Deve retornar 404 para id inexistente")
        void findById_NotFound() throws Exception {
            Long nonExistentId = 999L;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++; // aumenta até achar um id que não exista
            }

            MvcResult mvcResult = mockMvc.perform(get("/pessoa/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Pessoa com ID " + nonExistentId + " não encontrada."))
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (GET /pessoa/{id}):\n" + responseBody);
        }
    }

    @Nested
    @DisplayName("CREATE Tests")
    class CreateTests {

        @Test
        @DisplayName("POST /pessoa - Deve criar nova pessoa com todos os campos válidos")
        void create() throws Exception {
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "Pessoa Nova",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nome").value("Pessoa Nova"))
                    .andExpect(jsonPath("$.dataInicial").exists())
                    .andExpect(jsonPath("$.dataFinal").exists())
                    .andExpect(jsonPath("$.observacoes").value("Observações de teste"))
                    .andExpect(jsonPath("$.laboratorio.id").value(laboratorioTeste.getId()))
                    .andExpect(jsonPath("$.laboratorio.nome").value(laboratorioTeste.getNome()))
                    .andExpect(jsonPath("$.infosPropriedade.id").value(propriedadeTeste.getId()))
                    .andExpect(jsonPath("$.infosPropriedade.nome").value(propriedadeTeste.getNome()))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar ao criar nova pessoa com o nome já existente")
        void create_DuplicateName() throws Exception {
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "Pessoa Teste 1",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Nome da pessoa já existe."))
                    .andReturn();


            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (POST /pessoa):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar ao criar pessoa com nome com 2 caracteres")
        void create_NameTooShort() throws Exception {
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "A",  // Nome com apenas 1 caractere
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com menos de 3 caracteres
            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())  // Espera erro 400
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 200 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /pessoa):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar ao criar pessoa com nome maior que 200 caracteres")
        void create_NameTooLong() throws Exception {
            // Criando um nome com mais de 200 caracteres
            String longName = "A".repeat(201);  // Nome com 201 caracteres

            PessoaDTO dtoToCreate = new PessoaDTO(
                    longName,  // Nome com mais de 200 caracteres
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com mais de 200 caracteres
            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())  // Espera erro 400
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 200 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (POST /pessoa):\n" + responseBody);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar ao criar pessoa com vários campos vazios")
        void create_NameEmpty() throws Exception {
            PessoaDTO dtoToCreate = new PessoaDTO(
                    null, // nome vazio
                    null, // dataInicial vazia
                    null, // dataFinal vazia
                    null, // laboratorioId vazio
                    null, // propriedadeId vazio
                    "" // observações vazias
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItems(
                            "Nome não pode estar vazio!",
                            "Data inicial é obrigatória.",
                            "O laboratório é obrigatório.",
                            "A propriedade é obrigatória.",
                            "Data final é obrigatória."
                    )))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /pessoa -  Deve falhar se dataInicial for maior ou igual a dataFinal")
        void create_DateValidationTest() throws Exception {
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "Pessoa Teste",
                    ZonedDateTime.now().plusDays(2),
                    ZonedDateTime.now().minusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
//                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Data Final deve ser posterior a Data Inicial.")))
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar se Laboratório for inexistente")
        void create_LaboratorioNotFound() throws Exception {
            // Criando DTO com IDs de Laboratório e Propriedade inexistentes
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "Pessoa Teste Inexistente",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(999, "Laboratório Inexistente"),  // ID inválido
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNotFound()) // Espera que retorne erro 404, pois os recursos não foram encontrados
                    .andExpect(content().string("Laboratório não encontrado."))  // Espera mensagem de erro no corpo da resposta
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            System.out.println("Response JSON:\n" + jsonResponse);
        }

        @Test
        @DisplayName("POST /pessoa - Deve falhar se Propriedade fore inexistente")
        void create_PropriedadeNotFound() throws Exception {
            // Criando DTO com IDs de Laboratório e Propriedade inexistentes
            PessoaDTO dtoToCreate = new PessoaDTO(
                    "Pessoa Teste Inexistente",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(999, "Propriedade Inexistente"),  // ID inválido
                    new LaboratorioDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToCreate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(post("/pessoa")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNotFound()) // Espera que retorne erro 404, pois os recursos não foram encontrados
                    .andExpect(content().string("Propriedade não encontrada."))  // Espera mensagem de erro no corpo da resposta
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            System.out.println("Response JSON:\n" + jsonResponse);
        }
    }

    @Nested
    @DisplayName("UPDATE Tests")
    class UpdateTests {

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve atualizar propriedade existente")
        void update() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Atualizada",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste2.getId(), propriedadeTeste2.getNome()),
                    new LaboratorioDTO(laboratorioTeste2.getId(), laboratorioTeste2.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Pessoa Atualizada"))
                    .andExpect(jsonPath("$.dataInicial").exists())
                    .andExpect(jsonPath("$.dataFinal").exists())
                    .andExpect(jsonPath("$.observacoes").value("Observações de teste"))
                    .andExpect(jsonPath("$.laboratorio.id").value(laboratorioTeste2.getId()))
                    .andExpect(jsonPath("$.laboratorio.nome").value(laboratorioTeste2.getNome()))
                    .andExpect(jsonPath("$.infosPropriedade.id").value(propriedadeTeste2.getId()))
                    .andExpect(jsonPath("$.infosPropriedade.nome").value(propriedadeTeste2.getNome()))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve atualizar para mesmo nome atual sem erro")
        void update_SameName() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Teste 1",
                    ZonedDateTime.now().minusDays(3),
                    ZonedDateTime.now().plusDays(3),
                    new PropriedadeDTO(propriedadeTeste2.getId(), propriedadeTeste2.getNome()),
                    new LaboratorioDTO(laboratorioTeste2.getId(), laboratorioTeste2.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idP1))
                    .andExpect(jsonPath("$.nome").value("Pessoa Teste 1"))
                    .andExpect(jsonPath("$.dataInicial").exists())
                    .andExpect(jsonPath("$.dataFinal").exists())
                    .andExpect(jsonPath("$.observacoes").value("Observações de teste"))
                    .andExpect(jsonPath("$.laboratorio.id").value(laboratorioTeste2.getId()))
                    .andExpect(jsonPath("$.laboratorio.nome").value(laboratorioTeste2.getNome()))
                    .andExpect(jsonPath("$.infosPropriedade.id").value(propriedadeTeste2.getId()))
                    .andExpect(jsonPath("$.infosPropriedade.nome").value(propriedadeTeste2.getNome()))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve retornar 404 para update em pessoa inexistente")
        void update_NotFound() throws Exception {
            Long nonExistentId = 999L;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Atualizada",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Pessoa com ID " + nonExistentId + " não encontrada."))
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (PUT /pessoa/{id}):\n" + responseBody);

        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar ao atualizar para nome que já existe em outra pessoa")
        void update_DuplicateName() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Teste 2",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Nome da pessoa já existe."))
                    .andReturn();

            // Obtém a resposta em texto (corpo da resposta)
            String responseBody = mvcResult.getResponse().getContentAsString();

            // Exibe o conteúdo da resposta no console
            System.out.println("Response Body (PUT /pessoa/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar ao atualizar para nome com 2 caracteres")
        void update_NameTooShort() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "A",  // Nome com apenas 1 caractere
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com menos de 3 caracteres
            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())  // Espera erro 400
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 200 caracteres."))
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /pessoa/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar ao atualizar para nome maior que 200 caracteres")
        void update_NameTooLong() throws Exception {
            // Criando um nome com mais de 200 caracteres
            String longName = "A".repeat(201);  // Nome com 201 caracteres

            PessoaDTO dtoToUpdate = new PessoaDTO(
                    longName,  // Nome com mais de 200 caracteres
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            // Espera erro 400, devido à validação do nome com mais de 200 caracteres
            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())  // Espera erro 400
                    .andExpect(jsonPath("$").value("Nome deve ter entre 3 e 200 caracteres."))
                    .andReturn();

            // Exibe a resposta da requisição no console
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (PUT /pessoa/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar ao atualizar pessoa com vários campos vazios")
        void update_NameEmpty() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    null, // nome vazio
                    null, // dataInicial vazia
                    null, // dataFinal vazia
                    null, // laboratorioId vazio
                    null, // propriedadeId vazio
                    "" // observações vazias
            );

            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItems(
                            "Nome não pode estar vazio!",
                            "Data inicial é obrigatória.",
                            "O laboratório é obrigatório.",
                            "A propriedade é obrigatória.",
                            "Data final é obrigatória."
                    )))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} -  Deve falhar se dataInicial for maior ou igual a dataFinal")
        void update_DateValidationTest() throws Exception {
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Teste",
                    ZonedDateTime.now().plusDays(2),
                    ZonedDateTime.now().minusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(laboratorioTeste.getId(), laboratorioTeste.getNome()),
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
//                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(Matchers.hasItem("Data Final deve ser posterior a Data Inicial.")))
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar se Laboratório for inexistente")
        void create_LaboratorioNotFound() throws Exception {
            // Criando DTO com IDs de Laboratório e Propriedade inexistentes
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Teste Inexistente",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    new LaboratorioDTO(999, "Laboratório Inexistente"),  // ID inválido
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNotFound()) // Espera que retorne erro 404, pois os recursos não foram encontrados
                    .andExpect(content().string("Laboratório não encontrado."))  // Espera mensagem de erro no corpo da resposta
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            System.out.println("Response JSON:\n" + jsonResponse);
        }

        @Test
        @DisplayName("PUT /pessoa/{id} - Deve falhar se Propriedade fore inexistente")
        void update_PropriedadeNotFound() throws Exception {
            // Criando DTO com IDs de Laboratório e Propriedade inexistentes
            PessoaDTO dtoToUpdate = new PessoaDTO(
                    "Pessoa Teste Inexistente",
                    ZonedDateTime.now().minusDays(2),
                    ZonedDateTime.now().plusDays(2),
                    new PropriedadeDTO(999, "Propriedade Inexistente"),  // ID inválido
                    new LaboratorioDTO(propriedadeTeste.getId(), propriedadeTeste.getNome()),
                    "Observações de teste"
            );

            // JSON formatado do request
            String jsonRequest = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToUpdate);
            System.out.println("Request JSON:\n" + jsonRequest);

            MvcResult mvcResult = mockMvc.perform(put("/pessoa/" + idP1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isNotFound()) // Espera que retorne erro 404, pois os recursos não foram encontrados
                    .andExpect(content().string("Propriedade não encontrada."))  // Espera mensagem de erro no corpo da resposta
                    .andReturn();

            // JSON formatado do response
            String jsonResponse = mvcResult.getResponse().getContentAsString();
            System.out.println("Response JSON:\n" + jsonResponse);
        }
    }

    @Nested
    @DisplayName("DELETE Tests")
    class DeleteTests {

        @Test
        @DisplayName("DELETE /pessoa/{id} - Deve deletar pessoa existente")
        void deletePropriedade() throws Exception {
            MvcResult mvcResult = mockMvc.perform(delete("/pessoa/" + idP1))
                    .andExpect(status().isNoContent())  // Espera que a resposta seja 204 (sem conteúdo)
                    .andReturn();

            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /pessoa/{id}):\n" + responseBody);
        }

        @Test
        @DisplayName("DELETE /pessoa/{id} - Deve retornar 404 ao deletar pessoa inexistente")
        void delete_NotFound() throws Exception {
            Long nonExistentId = 999L;
            while (repository.existsById(nonExistentId)) {
                nonExistentId++;
            }

            MvcResult mvcResult = mockMvc.perform(delete("/pessoa/" + nonExistentId))
                    .andExpect(status().isNotFound())  // Espera que a resposta seja 404 (não encontrado)
                    .andReturn();

            // Exibe a resposta completa para verificar o corpo da resposta
            String responseBody = mvcResult.getResponse().getContentAsString();
            System.out.println("Response Body (DELETE /pessoa/{id}):\n" + responseBody);
        }
    }
}