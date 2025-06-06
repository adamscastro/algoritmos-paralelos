# Análise Comparativa de Algoritmos com Uso de Paralelismo

## 👥 Autores
- **Adams Amaral de Castro Filho**
- **Carolina Cavalcante Aguiar**

## 📄 Resumo

Este projeto realiza uma análise comparativa do desempenho de três abordagens de contagem de palavras em arquivos texto: execução serial, paralelismo em CPU com múltiplos núcleos e paralelismo em GPU com OpenCL. A aplicação foi desenvolvida em Java, com interface gráfica (Swing + JFreeChart) para visualização dos resultados e geração de gráficos automáticos. Os resultados são armazenados em CSV e analisados estatisticamente com base em múltiplas amostras.

---

## 📘 Introdução

A crescente demanda por processamento eficiente em grandes volumes de dados exige a exploração de estratégias de paralelismo. Este trabalho compara três abordagens:

- **SerialCPU**: algoritmo sequencial simples em Java.
- **ParallelCPU**: uso de `ExecutorService` com múltiplos núcleos.
- **ParallelGPU**: execução paralela em GPU via OpenCL (JOCL).

Cada algoritmo realiza a contagem de uma palavra específica em arquivos `.txt`, com medição precisa do tempo de execução.

---

## ⚙️ Metodologia

**Implementação de Algoritmos**  
Foram implementadas três versões para contagem de palavras: serial, paralela em CPU e paralela em GPU.

**Framework de Teste**  
A classe `Main.java` automatiza execuções com repetição de amostras e grava os resultados no arquivo `resultados/resultados.csv`.

**Execução em Ambientes Variados**  
Foram utilizados arquivos com diferentes tamanhos (ex: capítulos de livros) para analisar o impacto da escala.

**Registro de Dados**  
Cada execução grava no CSV: nome do arquivo, método, número de threads, número de ocorrências e tempo em ms.

**Análise Estatística**  
Cada configuração é testada com 3 amostras. O sistema exibe os tempos individuais e a média no gráfico (via JFreeChart).

---

## 📊 Resultados e Discussão

Abaixo, um exemplo de resultado de execução para a palavra `el` no livro *Don Quixote*:

```
SerialCPU [amostra 1]: 8271 ocorrências em 31 ms
SerialCPU [amostra 2]: 8271 ocorrências em 31 ms
SerialCPU [amostra 3]: 8271 ocorrências em 31 ms

ParallelCPU-1T [amostra 1]: 8271 ocorrências em 31 ms
ParallelCPU-1T [amostra 2]: 8271 ocorrências em 182 ms
ParallelCPU-1T [amostra 3]: 8271 ocorrências em 28 ms

ParallelCPU-2T [amostra 1]: 8271 ocorrências em 26 ms
ParallelCPU-2T [amostra 2]: 8271 ocorrências em 26 ms
ParallelCPU-2T [amostra 3]: 8271 ocorrências em 27 ms

ParallelCPU-4T [amostra 1]: 8271 ocorrências em 26 ms
ParallelCPU-4T [amostra 2]: 8271 ocorrências em 26 ms
ParallelCPU-4T [amostra 3]: 8271 ocorrências em 26 ms

ParallelCPU-8T [amostra 1]: 8271 ocorrências em 29 ms
ParallelCPU-8T [amostra 2]: 8271 ocorrências em 151 ms
ParallelCPU-8T [amostra 3]: 8271 ocorrências em 37 ms

ParallelGPU [amostra 1]: 8271 ocorrências em 19 ms
ParallelGPU [amostra 2]: 8271 ocorrências em 13 ms
ParallelGPU [amostra 3]: 8271 ocorrências em 20 ms

```

### 📈 Gráfico gerado:
A interface mostra um gráfico de barras com as médias de tempo por método e número de threads. Isso facilita a percepção visual da eficiência de cada abordagem.

---

## ✅ Conclusão

Os resultados confirmam que abordagens paralelas oferecem desempenho superior, especialmente em entradas maiores. A versão GPU foi a mais rápida, mas sua eficiência depende do hardware e custo de setup. O paralelismo com CPU se mostrou balanceado e mais flexível. A abordagem serial tem desempenho previsível, mas limitado.

Este estudo evidencia a importância da escolha da estratégia de processamento com base no tipo de aplicação e volume de dados.

---

## 📚 Referências

- Java Documentation: https://docs.oracle.com/en/java/
- JFreeChart API: https://sourceforge.net/projects/jfreechart/
- JOCL (Java bindings for OpenCL): http://www.jocl.org

---

## 📦 Bibliotecas Utilizadas

- **JFreeChart (jfreechart-1.5.3.jar)**: para geração dos gráficos.
- **JCommon (jcommon-1.0.24.jar)**: dependência do JFreeChart.
- **JOCL (jocl-2.0.4.jar)**: binding Java para OpenCL (usado na execução GPU).

### ➕ Como configurar:

Coloque os arquivos `.jar` na pasta `libs/` do projeto e certifique-se de adicioná-los ao classpath no `settings.json` do VS Code:

```json
"java.project.referencedLibraries": [
    "libs/jfreechart-1.5.3.jar",
    "libs/jcommon-1.0.24.jar",
    "libs/jocl-2.0.4.jar"
]
```

---

## 📎 Anexos

### Estrutura do Projeto

```
Algoritmos-Paralelos/
├── libs/              # Bibliotecas JAR: jfreechart, jocl, etc.
├── amostras/          # Arquivos .txt de entrada
├── resultados/        # Arquivo CSV gerado
├── src/app/           # Códigos-fonte Java
│   ├── Main.java
│   ├── GuiMain.java
│   ├── SerialCounter.java
│   ├── ParallelCpuCounter.java
│   └── ParallelGpuCounter.java
```

### 🔗 Link do projeto no GitHub:
> https://github.com/adamscastro/algoritmos-paralelos