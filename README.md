# Desafio QA — Web, API e Performance

Automação em Java 17, Selenium WebDriver, RestAssured e JUnit 5; performance em JMeter 5.6.3. A estrutura separa testes, interação com páginas e planos de performance. JUnit mantém a solução pequena: o desafio não exige uma camada adicional de BDD.

**Estado da validação (22/09/2026):** 5 testes API aprovados; 2 testes Web com erro na ativação da pesquisa. Carga e pico executados: carga 174,99 req/s, p90 9141 ms; pico 139,09 req/s, p90 10372 ms. Consulte o [relatório de performance](docs/performance-report.md), os [resultados funcionais](docs/validation.md) e a [conferência do PDF](docs/delivery-checklist.md).

## Pré-requisitos

- JDK 17 ou superior e Maven 3.9, disponíveis no `PATH`; `JAVA_HOME` aponta para o JDK.
- Google Chrome instalado. Selenium Manager resolve o driver compatível automaticamente.
- Internet para dependências, driver e sistemas públicos testados.
- JMeter 5.6.3 para performance; Python 3 somente para a análise complementar do JTL ou regeneração do plano.

Confira `java -version` e `mvn -version`. Os comandos Maven e JMeter abaixo funcionam em Linux, Windows e macOS; no Windows o lançador JMeter pode ser `jmeter.bat`. Em macOS/Linux use locale UTF-8, especialmente se o caminho contiver acentos (`export LC_ALL=en_US.UTF-8`).

## Executar Web e API

Na raiz do repositório:

```sh
mvn clean test
mvn clean test "-Dgroups=web"
mvn clean test "-Dgroups=api | web"
```

O padrão executa apenas API. Para acompanhar o navegador: `mvn test "-Dgroups=web" -Dheadless=false`.

Endereços configuráveis por `-Dweb.baseUrl=https://blogdoagi.com.br/` e `-Dapi.baseUrl=https://dog.ceo/api`.

### Cenários Web prioritários

| Cenário | Resultado esperado | Relevância |
| --- | --- | --- |
| Pesquisar `emprestimo` pela lupa | Termo no cabeçalho, artigos relevantes e abertura do artigo selecionado com título correspondente | Jornada principal de descoberta de conteúdo |
| Pesquisar termo inexistente | Mensagem de ausência de resultados e nenhuma entrada de artigo | Comunicação correta quando a busca não encontra conteúdo |

Os testes começam no endereço solicitado, atualmente redirecionado para `blog.agibank.com.br`. Cada teste usa seu próprio navegador, resolução desktop e esperas explícitas. Não são usados sleeps, retries automáticos ou contagem fixa de artigos. Conteúdo e HTML do site público podem mudar.

### Cenários API

| Endpoint | Validações |
| --- | --- |
| `GET /breeds/list/all` | HTTP 200, JSON, sucesso, mapa não vazio de raças e listas de sub-raças |
| `GET /breed/{breed}/images` | HTTP 200, JSON, sucesso, lista não vazia, URLs e correspondência com a raça; exemplos hound e pug |
| `GET /breeds/image/random` | HTTP 200, JSON, sucesso e URL de imagem |
| Raça inexistente | HTTP 404, JSON e contrato de erro |

Não se exige que chamadas aleatórias retornem imagens diferentes. A suíte valida as URLs retornadas, sem baixar todas as imagens. Os exemplos e o contrato foram consultados na [documentação Dog API](https://dog.ceo/dog-api/documentation).

### Relatórios e CI

```sh
mvn surefire-report:report-only
```

Resultados individuais e stack traces ficam em `target/surefire-reports/`; o relatório HTML em `target/reports/` (ou `target/site/`, conforme configuração Maven). Em falhas durante testes Web, screenshot, HTML e URL ficam em `target/evidence/`.

O workflow `.github/workflows/tests.yml` executa Web e API em jobs independentes em pushes, pull requests ou manualmente. Os relatórios são publicados como `test-results-api` e `test-results-web`, inclusive quando os testes falham. A falha de uma suíte não cancela a outra, mas mantém a pipeline com falha. A execução local requer os mesmos pré-requisitos; o runner Ubuntu do GitHub já oferece Chrome.

## Performance

O plano `performance/purchase.jmx` pode ser aberto diretamente no JMeter. Não requer plugins. O gerador Python é opcional: `python3 performance/generate_plan.py` reproduz o XML versionado.

Fluxo: página inicial → busca Paris/Buenos Aires → seleção do primeiro voo → compra com dados fictícios → confirmação. Os campos de voo e token são extraídos das respostas. Há validações HTTP e de conteúdo, cookies por compra e interrupção da iteração em caso de falha. Recursos estáticos não são baixados.

| Perfil | Usuários máximos | Aquecimento | Vazão solicitada | Duração |
| --- | ---: | ---: | --- | ---: |
| smoke | 1 | 1 s | 1 req/s, uma compra | até 30 s |
| load | 600 | 60 s | 250 req/s | 660 s |
| spike | 600 | 30 s | 25 → 250 → 25 req/s | 240 s |

No pico, a subida ocorre em 60 s e a descida em 180 s, contados a partir do início do teste. O Constant Throughput Timer controla amostras HTTP compartilhadas entre threads; sua unidade interna é requisições/minuto (250 × 60 = 15.000). Configurar essa meta não garante alcançá-la: servidor, rede e gerador podem limitar a vazão. Consulte o [manual do JMeter](https://jmeter.apache.org/usermanual/component_reference.html#Constant_Throughput_Timer).

**250 req/s não equivale a 250 compras/s.** Cada compra completa possui quatro requisições: em regime estável, aproximadamente 62,5 compras/s, sem falhas. Os 600 usuários são um limite inicial a ajustar conforme a capacidade observada do gerador.

Execute primeiro o smoke:

```sh
jmeter -n -t performance/purchase.jmx -q performance/smoke.properties -l performance/results/smoke.jtl -j performance/results/smoke.log -e -o performance/results/smoke-html
```

Carga e pico foram executados nesta entrega, após confirmação de autorização pelo candidato. Para repetir em ambiente autorizado:

```sh
jmeter -n -t performance/purchase.jmx -q performance/load.properties -l performance/results/load.jtl -j performance/results/load.log -e -o performance/results/load-html
jmeter -n -t performance/purchase.jmx -q performance/spike.properties -l performance/results/spike.jtl -j performance/results/spike.log -e -o performance/results/spike-html
```

Use caminhos novos para cada execução; JMeter não deve anexar amostras antigas ao JTL nem reutilizar diretório HTML preenchido. O host pode ser alterado com `-Jhost=ambiente-autorizado`; protocolo e porta com `-Jprotocol=http -Jport=8080`. Execute carga em CLI, monitorando CPU, memória, rede e erros do gerador. A pipeline não dispara carga no site público.

### Critério de aceitação

A vazão medida deve ser ≥ 250 requisições HTTP/s e o p90 < 2.000 ms na mesma janela. Reportar também erros e compras confirmadas: uma resposta rápida com falha não demonstra compra bem-sucedida.

Avaliar os 600 s posteriores ao aquecimento no teste de carga, e o intervalo de 60–180 s no pico. Separar aquecimento, pico e recuperação; não diluir o pico na média do teste inteiro. Confirmar também o perfil temporal no dashboard.

O analisador calcula p90 pelo método nearest rank, incluindo falhas, e vazão pela duração fixa da janela. Forneça `--start-ms` como o timestamp de início do teste registrado no log acrescido de 60.000 ms:

```sh
python3 performance/analyze.py performance/results/load.jtl --start-ms TIMESTAMP_DA_JANELA --seconds 600
python3 performance/analyze.py performance/results/spike.jtl --start-ms TIMESTAMP_DA_JANELA --seconds 120
```

O código de saída é 1 se o critério não for atingido ou houver falhas funcionais. Verifique também no log se o teste cobriu toda a janela. O dashboard inclui resultados por requisição, percentis, vazão e erros. Os resultados, dashboards e conclusão desta entrega estão em [docs/performance-report.md](docs/performance-report.md).

## Publicação

Repositório público: [Leancb/qa-technical-challenge](https://github.com/Leancb/qa-technical-challenge). O projeto inclui fontes, documentação e evidências, excluindo `.tools/`, `target/` e resultados locais volumosos pelo `.gitignore`. Preserve os relatórios selecionados em `docs/evidence/` e os artefatos da pipeline. Não inclua currículo, telefone, e-mail pessoal, credenciais ou dados reais de pagamento.
