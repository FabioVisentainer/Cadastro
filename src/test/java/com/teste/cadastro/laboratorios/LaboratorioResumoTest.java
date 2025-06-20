package com.teste.cadastro.laboratorios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.cadastro.pessoas.Pessoa;
import com.teste.cadastro.pessoas.PessoaRepository;
import com.teste.cadastro.propriedades.Propriedade;
import com.teste.cadastro.propriedades.PropriedadeRepository;
import org.springframework.test.web.servlet.MvcResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LaboratorioResumoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        pessoaRepository.deleteAll();
        laboratorioRepository.deleteAll();
        propriedadeRepository.deleteAll();

        // Criando e salvando 6 laboratórios
        List<Laboratorio> laboratorios = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Laboratorio lab = new Laboratorio("Laboratório Modelo " + i);
            laboratorioRepository.save(lab);
            laboratorios.add(lab); // Adiciona o laboratório à lista
        }

        // Criando e salvando 6 propriedades
        List<Propriedade> propriedades = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Propriedade prop = new Propriedade("Fazenda Modelo " + i);
            propriedadeRepository.save(prop);
            propriedades.add(prop); // Adiciona a propriedade à lista
        }

        // Criando e salvando 30 pessoas distribuídas entre laboratórios e propriedades
        List<Pessoa> pessoas = new ArrayList<>();
        int pessoaIndex = 1;
        for (int i = 1; i <= 30; i++) {
            Pessoa pessoa = new Pessoa();
            pessoa.setNome("Pessoa " + pessoaIndex);

            // Definindo datas de início e fim com variação controlada
            ZonedDateTime dataInicial = ZonedDateTime.now().minusDays(10 + pessoaIndex);  // Aumento progressivo de dias
            ZonedDateTime dataFinal = dataInicial.plusDays(10);  // Aumento progressivo de dias

            pessoa.setDataInicial(dataInicial);
            pessoa.setDataFinal(dataFinal);

            // Distribuindo pessoas entre os laboratórios de forma controlada
            Laboratorio laboratorio;
            if (i <= 6) {
                laboratorio = laboratorios.get(0); // Laboratório Modelo 1 (6 pessoas)
            } else if (i <= 11) {
                laboratorio = laboratorios.get(1); // Laboratório Modelo 2 (5 pessoas)
            } else if (i <= 18) {
                laboratorio = laboratorios.get(2); // Laboratório Modelo 3 (7 pessoas)
            } else if (i <= 22) {
                laboratorio = laboratorios.get(3); // Laboratório Modelo 4 (4 pessoas)
            } else if (i <= 27) {
                laboratorio = laboratorios.get(4); // Laboratório Modelo 5 (5 pessoas)
            } else {
                laboratorio = laboratorios.get(5); // Laboratório Modelo 6 (3 pessoas)
            }



            // Distribuindo as propriedades de forma circular entre as 6 propriedades
            Propriedade propriedade = propriedades.get((i - 1) % 6);  // Ciclo entre as 6 propriedades

            pessoa.setLaboratorio(laboratorio);
            pessoa.setInfosPropriedade(propriedade);

            // Definindo as observações
            if (i <= 10) {
                // Observações específicas para as primeiras 5 pessoas
                pessoa.setObservacoes("Observação específica da pessoa" + i);
            } else {
                // Para as demais, a observação será nula
                pessoa.setObservacoes(null);
            }

            // Salvando a pessoa
            pessoaRepository.save(pessoa);
            pessoas.add(pessoa); // Adiciona a pessoa à lista

            pessoaIndex++;  // Incrementa o índice para a próxima pessoa
        }

    }

    @Nested
    @DisplayName("GET /laboratorio/resumo - Testes de Filtros")
    class ResumoTests {

        @Test
        @DisplayName("Deve retornar todos os laboratórios sem filtros aplicados")
        void testSemFiltros() throws Exception {
            // Realiza a requisição sem filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("quantidadeMinima", "1") // Definindo o valor mínimo de pessoas
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(6)) // Espera 6 laboratórios (de acordo com os dados de setup)
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 1"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 5"))
                    .andExpect(jsonPath("$[3].nome").value("Laboratório Modelo 2"))
                    .andExpect(jsonPath("$[4].nome").value("Laboratório Modelo 4"))
                    .andExpect(jsonPath("$[5].nome").value("Laboratório Modelo 6"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar todos os laboratórios com pelo menos 6 Pessoas")
        void testSemFiltrosMinimo() throws Exception {

            System.out.println("quantidadeMinima = 6");

            // Realiza a requisição sem filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("quantidadeMinima", "6") // Definindo o valor mínimo de pessoas
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2)) // Espera 6 laboratórios (de acordo com os dados de setup)
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 1"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataInicialInicio e dataInicialFim")
        void testFiltroDataInicial() throws Exception {
            // Definindo intervalos de datas específicas para o filtro
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(30).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            System.out.println("dataInicialInicio = -30 dias");
            System.out.println("dataInicialFim = -20 dias");

            // Realiza a requisição com filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3)) // Espera 3 laboratórios atendendo a esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 4"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);;
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataInicialInicio")
        void testFiltroDataInicialInicial() throws Exception {
            // Definindo intervalos de datas específicas para o filtro
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            // Exibindo os parâmetros da requisição
            System.out.println("dataInicialInicio = -20 dias");

            // Realiza a requisição com filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2)) // Espera 3 laboratórios atendendo a esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 1"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);;
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataInicialFim")
        void testFiltroDataInicialFinal() throws Exception {
            // Definindo intervalos de datas específicas para o filtro
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            // Exibindo os parâmetros da requisição
            System.out.println("dataInicialFim = -20 dias");

            // Realiza a requisição com filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(5)) // Espera 3 laboratórios atendendo a esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 5"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 4"))
                    .andExpect(jsonPath("$[3].nome").value("Laboratório Modelo 6"))
                    .andExpect(jsonPath("$[4].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);;
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataFinalInicio e dataFinalFim")
        void testFiltroDataFinal() throws Exception {
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            System.out.println("dataFinalInicio = -20 dias");
            System.out.println("dataFinalFim = -10 dias");

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3)) // Espera 4 laboratórios atendendo esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 4"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);;
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataFinalInicio")
        void testFiltroDataFinalInicial() throws Exception {
            // Definindo intervalos de datas específicas para o filtro
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            System.out.println("dataFinalInicio = -20 dias");

            // Realiza a requisição com filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4)) // Espera 3 laboratórios atendendo a esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 3"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 1"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 2"))
                    .andExpect(jsonPath("$[3].nome").value("Laboratório Modelo 4"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);;
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por dataFinalFim")
        void testFiltroDataFinalFim() throws Exception {
            // Definindo intervalos de datas específicas para o filtro
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());

            // Exibindo os parâmetros da requisição
            System.out.println("dataFinalFim = -20 dias");

            // Realiza a requisição com filtros
            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3)) // Espera 3 laboratórios atendendo a esse filtro
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 5"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 6"))
                    .andExpect(jsonPath("$[2].nome").value("Laboratório Modelo 4"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar laboratórios filtrados por observações")
        void testFiltroObservacoes() throws Exception {
            String observacoes = "Específica";

            System.out.println("observacoes = Específica");

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("observacoes", observacoes)
                            .param("quantidadeMinima", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2)) // Espera 2 laboratórios com observações específicas
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 1"))
                    .andExpect(jsonPath("$[1].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar laboratórios com múltiplos filtros aplicados")
        void testFiltrosCombinados() throws Exception {
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(15).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(5).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            String observacoes = "Observação Específica";
            long quantidadeMinima = 2;

            System.out.println("dataInicialInicio = -20 dias");
            System.out.println("dataInicialFim = -10 dias");
            System.out.println("dataFinalInicio = -15 dias");
            System.out.println("dataFinalFim = -5 dias");
            System.out.println("observacoes = Observação Específica");
            System.out.println("quantidadeMinima = 1");
            System.out.println("Deve retornar da pessoa 7 até a 10 ambas estão no modelo 2");

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("observacoes", observacoes)
                            .param("quantidadeMinima", String.valueOf(quantidadeMinima))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1)) // Espera 2 laboratórios que atendem aos filtros
                    .andExpect(jsonPath("$[0].nome").value("Laboratório Modelo 2"))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar erro quando quantidade mínima não for informada")
        void testQuantidadeMinimaNaoInformada() throws Exception {
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(15).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(5).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            String observacoes = "Observação Específica";

            // Não incluir o parâmetro quantidadeMinima

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("observacoes", observacoes)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest()) // Espera um erro 400
                    .andExpect(jsonPath("$.message").value("Parâmetro 'quantidadeMinima' está ausente."))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar erro quando a data inicial de começo for posterior a data inicial de termino")
        void testDataInicialInicioPosteriorDataInicialFim() throws Exception {
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(5).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(8).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(2).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            String observacoes = "Observação Específica";
            long quantidadeMinima = 1;

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("observacoes", observacoes)
                            .param("quantidadeMinima", String.valueOf(quantidadeMinima))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest()) // Espera um erro 400
                    .andExpect(jsonPath("$.message").value("A Data Inicial de Começo não pode ser posterior à Data Final de Começo."))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar erro quando a data final de começo for anterior a data inicial de começo")
        void testDataFinalInicioAnteriorDataInicialInicio() throws Exception {
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(5).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(15).toLocalDate().atStartOfDay(ZoneId.systemDefault()); // Data inválida
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(3).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            String observacoes = "Observação Específica";
            long quantidadeMinima = 1;

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("observacoes", observacoes)
                            .param("quantidadeMinima", String.valueOf(quantidadeMinima))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest()) // Espera um erro 400
                    .andExpect(jsonPath("$.message").value("A Data Inicial de Começo não pode ser posterior à data Data Inicial de Término."))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

        @Test
        @DisplayName("Deve retornar erro quando a data final de termino for anterior a qualquer data inicial ou final")
        void testDataFinalFimAnteriorQualquerData() throws Exception {
            ZonedDateTime dataInicialInicio = ZonedDateTime.now().minusDays(10).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataInicialFim = ZonedDateTime.now().minusDays(5).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalInicio = ZonedDateTime.now().minusDays(8).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime dataFinalFim = ZonedDateTime.now().minusDays(20).toLocalDate().atStartOfDay(ZoneId.systemDefault()); // Data inválida
            String observacoes = "Observação Específica";
            long quantidadeMinima = 1;

            MvcResult mvcResult = mockMvc.perform(get("/laboratorio/resumo")
                            .param("dataInicialInicio", dataInicialInicio.toString())
                            .param("dataInicialFim", dataInicialFim.toString())
                            .param("dataFinalInicio", dataFinalInicio.toString())
                            .param("dataFinalFim", dataFinalFim.toString())
                            .param("observacoes", observacoes)
                            .param("quantidadeMinima", String.valueOf(quantidadeMinima))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest()) // Espera um erro 400
                    .andExpect(jsonPath("$.message").value("A Data Inicial de Começo não pode ser posterior à Data Final de Término."))
                    .andReturn();

            String jsonResponse = mvcResult.getResponse().getContentAsString();
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("Response JSON:\n" + prettyJson);
        }

    }
}