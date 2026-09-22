# Evidências e estado da validação

Execuções locais em 21/09/2026, macOS 12.7.6 Intel, Temurin JDK 17.0.20.1, Maven 3.9.9, Chrome 150 e JMeter 5.6.3. Ferramentas baixadas em `.tools/`, fora do versionamento.

## API

**Evidência posterior no GitHub:** na [execução inicial de CI](https://github.com/Leancb/qa-technical-challenge/actions/runs/35679593189), os cinco testes API passaram, sem falhas ou erros. Os dois testes Web reproduziram a falha da lupa. O resultado em CI não apaga os timeouts locais registrados abaixo.

Na execução inicial, cinco casos passaram: lista de raças, imagens de hound, imagens de pug, imagem aleatória e raça inexistente. Na execução final integrada, três passaram e dois terminaram com `SocketTimeoutException: Read timed out` (imagens de pug e raça inexistente, timeout de 15 s durante a conexão TLS). A causa externa específica não foi determinada: pode envolver serviço ou rede. Não foram aplicados retries para ocultar essa instabilidade.

Resultado final integrado: **7 testes, 3 aprovados, 4 com erro** — dois Web e dois API. O comando Maven retornou código 1 corretamente. O relatório HTML Surefire foi gerado com sucesso, incluindo detalhes individuais; consulte o arquivo compactado em `docs/evidence/test-report.zip`.

## Web — falha reproduzida

Defeito registrado em [BUG-001 — Lupa não abre o campo de pesquisa](BUG-001-pesquisa.md), incluindo confirmação do candidato, impacto, passos e mensagem sugerida ao recrutador. Não há evidência de que o defeito seja intencional. As suítes Web e API foram separadas em jobs de CI; nenhuma falha foi ignorada.

Os dois cenários foram implementados, mas não aprovados no ambiente local. O campo de pesquisa não se tornou visível após o clique na lupa, causando timeout de 20 segundos. A pesquisa sem resultados também falhou com Chrome visível, portanto a reprodução não ficou restrita ao modo headless.

Passos: acessar `https://blogdoagi.com.br/`, aguardar o redirecionamento, clicar na lupa do cabeçalho desktop e aguardar o campo `input[name='s']` no painel de busca. Esperado: campo visível para digitação. Observado: painel não aberto; captura mostra o cabeçalho e grande área vazia.

O console registrou carregamento adiado LiteSpeed, `Uncaught TypeError: $scope.imagesLoaded is not a function` e recursos de imagem com HTTP 404. Esses registros são indícios de problemas na página, **não prova de causa raiz da falha da lupa**. É necessário comparar manualmente em outro ambiente e, se persistir, encaminhar evidências ao recrutador. Não foi aplicado bypass por URL nem JavaScript para abrir artificialmente o painel.

O HTML consultado por HTTP contém o campo e, na URL de consulta sem resultados, a mensagem esperada. Essa inspeção não comprova o fluxo interativo. Os testes permanecem com falha para não ocultar o resultado. As evidências são gravadas em `target/evidence/` e as selecionadas para entrega em `docs/evidence/`.

## Performance — smoke aprovado; aceitação não verificada

Uma compra completa foi executada com um usuário, uma iteração e limite configurado de 1 req/s. As quatro requisições retornaram HTTP 200 e passaram nas validações de conteúdo, incluindo a confirmação da compra.

| Requisição | Tempo |
| --- | ---: |
| Página inicial | 1.164 ms |
| Busca de voos | 386 ms |
| Seleção de voo | 696 ms |
| Confirmação da compra | 462 ms |

Total: 4 requisições, 1 compra, 0 erros, média de 677 ms e p90 de 1.164 ms pelo método nearest rank. Os dados brutos estão em [smoke.jtl](evidence/smoke.jtl); estatísticas do dashboard em [smoke-statistics.json](evidence/smoke-statistics.json).

**Não é possível concluir que o requisito de 250 req/s com p90 < 2 s foi satisfeito.** O smoke comprova o funcionamento do script em baixa carga. Carga e pico não foram executados a 250 req/s, aguardando autorização do responsável pelo ambiente público. Um p90 baixo com apenas quatro amostras não é evidência de capacidade.

Os perfis de carga e pico estão configurados, mas seu comportamento nas taxas e durações completas também depende de execução. Para concluir a entrega de performance: executar ambos em ambiente autorizado, preservar JTL/log/dashboard, analisar as janelas definidas no README e registrar capacidade do gerador, erros e conclusão. Não foram inventados relatórios de carga.

## CI e publicação

Repositório público: https://github.com/Leancb/qa-technical-challenge. Workflow preparado para Web/API e upload de relatórios. O resultado da execução no GitHub deve ser consultado na aba Actions; não foi validado neste relatório local. Como o Web falhou localmente, não se promete pipeline verde.
