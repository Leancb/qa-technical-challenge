# BUG-001 — Lupa não abre o campo de pesquisa

**Estado:** aberto, reproduzido. **Severidade proposta:** alta, pois impede iniciar a pesquisa pela interface. **Prioridade:** a confirmar com Produto; não definida apenas pela automação.

## Ambiente e evidências

- URL informada: https://blogdoagi.com.br/ (redireciona para https://blog.agibank.com.br/).
- Reprodução local: macOS 12.7.6, Chrome 150, com e sem interface visível.
- Reprodução em CI: Ubuntu 24.04, Chrome 152, em 22/09/2026 UTC.
- [Execução inicial no GitHub](https://github.com/Leancb/qa-technical-challenge/actions/runs/35679593189): 5 testes API aprovados e 2 testes Web com erro ao aguardar o campo de pesquisa.
- [Screenshot local](evidence/web-search-screenshot.png), [console local](evidence/web-search-browser.log) e artefato `test-results` da execução do GitHub.

## Passos para reproduzir

1. Abrir o endereço informado em uma nova sessão do Chrome desktop.
2. Aguardar o carregamento e o redirecionamento.
3. Clicar na lupa no canto superior direito.
4. Observar se o campo de pesquisa é exibido.

**Esperado:** abrir um campo visível e utilizável para pesquisar artigos.

**Observado:** o campo não é exibido após o clique. Os testes encerram com timeout antes de enviar o termo.

## Impacto e cobertura

Os cenários de pesquisa com resultados e sem resultados estão implementados, mas bloqueados funcionalmente nessa etapa. A execução automatizada deve falhar; os cenários não devem ser marcados como aprovados, ignorados ou substituídos por navegação direta para `?s=...`.

A consulta direta por URL pode auxiliar o diagnóstico do backend de pesquisa, mas não valida a interação com a lupa exigida no desafio. Nenhum bypass desse tipo foi aplicado aos testes.

## Investigação e limitações

Foram observados carregamento adiado de JavaScript, recarregamento de primeira visita pelo LiteSpeed, erro `$scope.imagesLoaded is not a function` e imagens com HTTP 404. Não há evidência suficiente para afirmar qual deles causa o defeito. O teste agora aguarda o registro do evento de clique do Astra antes de interagir, para distinguir elemento visível de componente pronto.

## Reteste

Após a correção, executar `mvn clean test "-Dgroups=web"`. Confirmar abertura do campo, envio dos termos, resultados relevantes com abertura de artigo e mensagem de ausência de resultados. Fechar o defeito somente com evidência dessa execução.

### Reteste local de 22/09/2026

Os dois cenários continuam com erro. Nesta rodada, a automação não chegou ao clique: expirou a espera por `onclick` no botão Astra (`BlogSearchPage.java:36`). Esse resultado não confirma novamente o sintoma pós-clique nem sua causa raiz. Evidências por cenário em [web-evidence.zip](evidence/retest-20260922/web-evidence.zip) e detalhes no [relatório de validação](validation.md). O defeito permanece aberto com base na reprodução anterior.
