# Relatório de performance — 22/09/2026

**Conclusão:** o critério de aceitação não foi atendido nesta execução. Foram executados os testes completos de carga e pico no BlazeDemo. A meta é vazão medida ≥ 250 requisições HTTP/s e p90 < 2.000 ms na mesma janela, com verificação adicional de erros e compras confirmadas.

| Perfil / janela | Requisições | Req/s | p90 (ms) | Erros | Compras confirmadas |
| --- | ---: | ---: | ---: | ---: | ---: |
| Carga — 60 a 660 s | 104993 | 174,99 | 9141 | 6220 (5,92%) | 23474 |
| Pico — 60 a 180 s | 16691 | 139,09 | 10372 | 834 (5,00%) | 3710 |

## Execução e método

- JMeter 5.6.3, Temurin Java 17.0.20.1, macOS 12.7.6, MacBookPro12,1, 4 CPUs lógicas e 16 GiB de RAM.
- Carga: até 600 usuários, ramp-up de 60 s, duração de 660 s, meta de 250 req/s.
- Pico: até 600 usuários, ramp-up de 30 s, duração de 240 s; meta de 25 → 250 → 25 req/s, com pico entre 60 e 180 s.
- Os perfis foram executados sequencialmente, sem sobreposição. Cada compra tem quatro chamadas HTTP; 250 req/s não equivale a 250 compras/s.
- Janela selecionada pelo início de cada amostra; p90 nearest rank inclui sucessos e falhas. Vazão calculada por quantidade de requisições dividida pela duração fixa da janela.
- Início obtido do timestamp do console JMeter; os logs registram encerramento normal e as amostras cobrem o fim da janela. JMeter terminou com código 0 nos dois perfis; isso indica término da ferramenta, não aprovação do critério.

Comandos de execução, na raiz do projeto (JMeter no PATH):

```sh
jmeter -n -t performance/purchase.jmx -q performance/load.properties -l performance/results/load.jtl -j performance/results/load.log -e -o performance/results/load-html
jmeter -n -t performance/purchase.jmx -q performance/spike.properties -l performance/results/spike.jtl -j performance/results/spike.log -e -o performance/results/spike-html
```

Utilize diretórios novos a cada execução. A execução local usou o binário em `.tools/` e o diretório `performance/results/full-20260922-run1/`. A primeira tentativa com caminho absoluto falhou no lançador macOS por espaços no caminho, antes de enviar requisições; a execução válida usou caminho relativo.

Comandos de análise reproduzíveis após extrair os ZIPs de resultados:

```sh
python3 performance/analyze.py load.jtl --start-ms 1790117729546 --seconds 600
python3 performance/analyze.py spike.jtl --start-ms 1790118426028 --seconds 120
```

## Aquecimento, pico e recuperação

| Perfil / fase | Duração (s) | Req/s | p90 (ms) | Erros | Compras confirmadas |
| --- | ---: | ---: | ---: | ---: | ---: |
| load / warmup | 60 | 191,10 | 3075 | 0 | 2629 |
| load / steady | 600 | 174,99 | 9141 | 6220 | 23474 |
| spike / warmup | 60 | 25,28 | 2580 | 4 | 104 |
| spike / peak | 120 | 139,09 | 10372 | 834 | 3710 |
| spike / recovery | 60 | 25,58 | 2775 | 6 | 320 |

As médias do teste inteiro não foram usadas para aprovar o pico. As séries em intervalos de 10 s estão nos CSVs de timeline; os dashboards mostram o perfil temporal.

## Interpretação e limites

- **load:** vazão abaixo da meta (174,99 req/s); p90 acima do limite (9141 ms). O analisador retornou código 1; houve 6220 falhas na janela.
- **spike:** vazão abaixo da meta (139,09 req/s); p90 acima do limite (10372 ms). O analisador retornou código 1; houve 834 falhas na janela.

As falhas de transporte e os tempos observados estão documentados abaixo. Não há telemetria do servidor nem isolamento da conexão de internet, portanto estes resultados não identificam uma causa raiz exclusiva nem medem a capacidade intrínseca do BlazeDemo. Uma taxa configurada de 250 req/s não garante essa vazão medida. O limite de 600 usuários também pode restringir a vazão quando a latência cresce; não foi usado um gerador distribuído nem feita uma busca de capacidade com outros níveis de concorrência. A conclusão vale para esta execução e este gerador.

| Perfil | Código / exceção | Quantidade na janela |
| --- | --- | ---: |
| load | `200` | 98773 |
| load | `Non HTTP response code: org.apache.http.NoHttpResponseException` | 948 |
| load | `Non HTTP response code: java.net.SocketTimeoutException` | 2894 |
| load | `Non HTTP response code: org.apache.http.conn.ConnectTimeoutException` | 2281 |
| load | `Non HTTP response code: java.net.SocketException` | 97 |
| spike | `200` | 15857 |
| spike | `Non HTTP response code: java.net.SocketTimeoutException` | 478 |
| spike | `Non HTTP response code: org.apache.http.NoHttpResponseException` | 136 |
| spike | `Non HTTP response code: org.apache.http.conn.ConnectTimeoutException` | 205 |
| spike | `Non HTTP response code: java.net.SocketException` | 15 |

## Recursos do gerador

Amostragem aproximadamente a cada 10 s; CPU percentual do processo pode ultrapassar 100% ao usar mais de um núcleo. Métricas de memória são RSS, não apenas heap Java. A máquina é compartilhada; não se assume saturação ou ausência de gargalo apenas desses números. A coleta detalhada começou depois do início do aquecimento da carga. As métricas publicadas cobrem a execução das requisições; amostras posteriores, durante a geração dos dashboards, foram excluídas.

| Perfil | Amostras | CPU Java máxima (%) | RSS Java máximo (MiB) | Swap in/out (páginas, delta) |
| --- | ---: | ---: | ---: | --- |
| load | 65 | 85,10 | 1.018,60 | 0 / 0 |
| spike | 24 | 318,60 | 1.053,31 | 0 / 0 |

`generator-metrics.json` contém CPU, RSS, carga do sistema, swap e contadores cumulativos de rede da interface en0. Esses contadores incluem tráfego de outros processos e não devem ser interpretados como uso exclusivo do JMeter. Endereços de rede e caminhos locais foram removidos desse artefato.

## Evidências entregues

- **load:** [JTL, logs, plano e configuração](evidence/performance-20260922/load-results.zip), [dashboard HTML](evidence/performance-20260922/load-dashboard.zip), [análise da janela](evidence/performance-20260922/load-analysis.json), [série de 10 s](evidence/performance-20260922/load-timeline.csv).
- **spike:** [JTL, logs, plano e configuração](evidence/performance-20260922/spike-results.zip), [dashboard HTML](evidence/performance-20260922/spike-dashboard.zip), [análise da janela](evidence/performance-20260922/spike-analysis.json), [série de 10 s](evidence/performance-20260922/spike-timeline.csv).

Extraia o ZIP do dashboard e abra `index.html`. O resumo consolidado está em [summary.json](evidence/performance-20260922/summary.json). Os relatórios registram o resultado real, inclusive falhas; não foi ajustado o critério para obter aprovação.
