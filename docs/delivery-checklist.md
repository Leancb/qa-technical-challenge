# Conferência da entrega contra o PDF

Fonte: `Teste técnico - QA.pdf`, fornecido pelo candidato. Conferência em 22/09/2026.

| Requisito | Entrega | Estado |
| --- | --- | --- |
| Web: ao menos dois cenários relevantes pela lupa | Pesquisa com abertura de artigo e pesquisa sem resultados | Implementados e executados; 2 erros documentados |
| API: três endpoints exigidos | `DogApiTest`, cinco casos | 5 aprovados no reteste |
| API: formato, status e cenários de erro | JSON, HTTP, raças, URLs e contrato de erro | Aprovado |
| Relatório de resultados | XML JUnit e HTML Surefire | Gerado com sucessos e erros |
| Configuração e execução em Linux/Windows/macOS | README, Java 17 e Maven | Documentado; execução local macOS e CI Linux |
| Pipeline | Jobs independentes Web/API e artefatos | Execução remota verificada: API aprovada, Web com falha |
| JMeter: compra de passagem | `purchase.jmx` e smoke | Compra confirmada |
| Carga e pico | Perfis completos executados sequencialmente | Concluído |
| 250 req/s com p90 inferior a 2 s | Janelas analisadas sem arredondamento para aprovação | Resultado detalhado em `performance-report.md` |
| Relatórios de carga e pico e conclusão | Dashboards HTML, JTL/logs, JSON, séries temporais e métricas | Gerado |
| Repositório público | https://github.com/Leancb/qa-technical-challenge | Existente; relatórios desta execução incluídos |

## Limitações do resultado

Os testes e relatórios foram entregues com seus resultados reais. A execução completa não significa aprovação: a pesquisa Web permanece bloqueada e o critério de performance deve ser avaliado pelos números do relatório. Não há mais pendência de autorização para as execuções realizadas, confirmada pelo candidato nesta sessão.

O envio do link ao recrutador continua a cargo do candidato; nenhuma mensagem foi enviada. A mensagem sugerida sobre o defeito Web está em `BUG-001-pesquisa.md`.
