# Notas para o avaliador

Este documento resume decisões técnicas que podem não ficar evidentes apenas pela cor da pipeline.

## Escopo Web

O requisito principal do desafio Web é a pesquisa de artigos pela lupa. Por isso os dois cenários de `BlogSearchTest` permanecem como a cobertura obrigatória:

1. pesquisa com resultado, validação de relevância e abertura do artigo;
2. pesquisa sem resultado, validando mensagem e ausência de artigos.

Foram adicionados dois cenários independentes de calculadoras como cobertura complementar. Eles não substituem os cenários obrigatórios de pesquisa.

Na execução consolidada de 22/09/2026, os quatro cenários Web foram executados. As duas calculadoras passaram; os dois cenários de pesquisa pararam na ativação da lupa do tema Astra. A falha foi mantida de propósito: acessar diretamente uma URL de resultados faria a automação contornar a jornada solicitada no desafio. O comportamento e as evidências estão registrados em `docs/BUG-001-pesquisa.md` e `docs/validation.md`.

## Estrutura e manutenibilidade

A camada Web usa Page Object para separar interação e regra de teste. `BlogSearchPage` concentra a jornada de pesquisa e `CalculatorPage` concentra iframes, seletores e ações das calculadoras. As classes de teste mantêm cenário e assertions.

Não são usados `Thread.sleep` nem retries automáticos. Esperas são explícitas. Nas calculadoras, o clique WebDriver é a primeira opção; existe fallback JavaScript somente para interceptação do clique de um botão que já foi considerado clicável.

## API

A suíte RestAssured/JUnit cobre os três endpoints solicitados e acrescenta um cenário negativo de raça inexistente. São validados status HTTP, JSON, estrutura do contrato, conteúdo relevante e URLs de imagens. O caso de imagens por raça é parametrizado para evitar duplicação.

## Performance

O plano JMeter executa a compra completa no BlazeDemo e possui smoke, carga e pico. O critério foi tratado como medição, não como valor configurado: a execução deve apresentar vazão medida de pelo menos 250 req/s e p90 inferior a 2 s na mesma janela.

Na execução entregue o critério não foi atingido. Carga: 174,99 req/s, p90 9.141 ms e 6.220 erros. Pico: 139,09 req/s, p90 10.372 ms e 834 erros. Esses números foram preservados; não houve ajuste do critério para produzir aprovação. Sem telemetria do servidor e isolamento completo de rede/gerador, o relatório não atribui a causa exclusivamente ao BlazeDemo.

## Pipeline e evidências

API e Web rodam em jobs independentes. O job Web permanece vermelho quando um cenário Web falha, mas o workflow agora publica no GitHub Actions um resumo por cenário antes de encerrar o job como falho. Assim, o avaliador consegue distinguir imediatamente os quatro cenários executados e seus resultados.

Relatórios e evidências são publicados mesmo em caso de falha. Screenshots, HTML, URL e log do navegador são coletados para falhas da pesquisa.

## Decisão de entrega

A intenção da entrega é tornar falhas observadas auditáveis, em vez de escondê-las para obter uma pipeline verde. Os documentos `docs/validation.md`, `docs/delivery-checklist.md` e `docs/performance-report.md` registram resultados, limitações e critérios utilizados.
