# Evidências e estado da validação

Estado consolidado em 22/09/2026, após conferência do PDF fornecido e execução completa de performance.

| Área | Resultado |
| --- | --- |
| API | 5 testes aprovados, 0 erros |
| Web | 2 cenários de calculadoras aprovados; 2 cenários de pesquisa com erro na espera de ativação do botão Astra |
| Smoke | 4 HTTP 200, 1 compra confirmada, 0 erros; p90 2.454 ms |
| Carga, janela de 600 s | 174,99 req/s, p90 9141 ms, 6220 erros |
| Pico, janela de 120 s | 139,09 req/s, p90 10372 ms, 834 erros |
| Relatórios | Surefire HTML, JUnit XML, JMeter HTML, JTL, logs, análises e métricas do gerador |

## Web e API

Reteste local com macOS 12.7.6, Chrome 150, Temurin 17.0.20.1 e Maven 3.9.9. Comando `mvn -B -ntp test "-Dgroups=api | web"`, com ferramentas locais e cache Maven em `.tools/m2`: saída **1**, 7 testes, 5 aprovados e 2 erros. `mvn -B -ntp surefire-report:report-only`: saída **0**.

As validações da API cobrem os três endpoints exigidos, imagens de duas raças e raça inexistente. As cinco passaram no reteste. Não há retries automáticos na suíte.

Os dois novos cenários foram executados isoladamente com `mvn -B -ntp test "-Dgroups=web" "-Dtest=CalculatorTest"`: saída **0**, 2 testes, 2 aprovados. Eles acessam diretamente os iframes das calculadoras, sem depender da lupa. A calculadora de dias úteis usa 06/04/2026 a 10/04/2026 e espera 5 dias úteis. A calculadora de juros compostos usa o caso inicial da página: R$ 1.000,00, aporte mensal de R$ 500,00, taxa de 0,8% ao mês e 12 meses; espera R$ 7.371,51 de valor final, R$ 7.000,00 investidos e R$ 371,51 de rendimento.

Os dois cenários Web pararam em `BlogSearchPage.search`, linha 36: o predicado de `onclick` registrado no botão Astra não se tornou verdadeiro em 20 s. Nesta rodada a automação não chegou ao clique; esse resultado, isoladamente, não demonstra a falha pós-clique nem identifica a causa raiz do site. O [BUG-001](BUG-001-pesquisa.md) permanece aberto, com reprodução anterior e evidências atuais. Os cenários não foram ignorados ou substituídos por navegação direta para a pesquisa.

Evidências: [relatório HTML](evidence/retest-20260922/test-report.zip), [capturas, HTML e console por cenário](evidence/retest-20260922/web-evidence.zip), XMLs JUnit e `maven.log` em `docs/evidence/retest-20260922/`.

A tentativa inicial dentro do sandbox foi bloqueada por DNS e abertura de porta do Selenium. Os resultados acima correspondem à execução posterior fora do sandbox.

## Performance

Carga e pico foram executados integralmente após confirmação de autorização pelo candidato. O [relatório de performance](performance-report.md) contém conclusão por critério, janelas, fases, falhas, uso do gerador e links dos dashboards e dados brutos. Não há mais pendência de execução desses perfis nesta entrega; o resultado medido deve ser distinguido da aprovação do sistema.

O gerador do plano também foi conferido: produz XML idêntico ao `purchase.jmx` entregue. O smoke confirmou uma compra; seu p90 de 2.454 ms não foi usado como evidência de capacidade a 250 req/s.

## CI e repositório

Repositório público confirmado: https://github.com/Leancb/qa-technical-challenge, branch `main`. O workflow separa Web e API e preserva relatórios mesmo quando há falhas.

Na [execução anterior à atualização dos relatórios](https://github.com/Leancb/qa-technical-challenge/actions/runs/35679988076), o job API passou e o job Web falhou; ambos geraram e publicaram os relatórios. Uma pipeline com falha Web é coerente com o bloqueio registrado, e não deve ser descrita como aprovada.

## Histórico preservado

As evidências de 21/09/2026 continuam em `docs/evidence/`. Naquela execução local, houve dois timeouts API, dois erros Web e smoke sem erros. O reteste de 22/09 aprovou os cinco casos API; isso não apaga a instabilidade anterior. Os diretórios datados distinguem as execuções.
