# Análise Comparativa de Algoritmos com Uso de Paralelismo

## 👥 Autores
- **Adams Amaral de Castro Filho**
- **Carolina Cavalcante Aguiar**

---

## 📄 Resumo

Este trabalho apresenta uma análise comparativa de desempenho entre diferentes estratégias de paralelismo na contagem de palavras em arquivos de texto. Três abordagens foram implementadas em Java: execução sequencial (SerialCPU), paralelismo em CPU com múltiplas threads (ParallelCPU) e paralelismo em GPU via OpenCL (ParallelGPU). A análise considera dados reais de livros clássicos, com medições de tempo de execução para cada abordagem, gerando gráficos e registros em CSV para facilitar a análise e comparação estatística.

---

## 📘 Introdução

Com o crescimento do volume de dados processados diariamente, algoritmos paralelos se tornam cada vez mais relevantes. Este projeto propõe a implementação de três métodos:

- **SerialCPU**: execução sequencial pura.
- **ParallelCPU**: divisão do trabalho em múltiplas threads (com ExecutorService).
- **ParallelGPU**: contagem de palavras usando processamento paralelo em GPU com OpenCL (JOCL).

O objetivo é comparar desempenho e escalabilidade dessas abordagens frente a diferentes cargas de dados.

---

## ⚙️ Metodologia

**Implementação de Algoritmos:**  
Criação de algoritmos de busca sequenciais e paralelos em Java para contagem de palavras.

**Framework de Teste:**  
O `Main.java` automatiza a execução das abordagens com medições de tempo e exporta os resultados em `CSV`.

**Execução em Ambientes Variados:**  
Os testes foram realizados em arquivos com diferentes tamanhos e estruturas (como *Moby Dick* e *Don Quixote*).

**Registro de Dados:**  
Cada execução armazena: método, número de threads, número de ocorrências e tempo total.

**Análise Estatística:**  
Para cada abordagem, foram realizadas três execuções. As médias e as amostras individuais são plotadas em dois gráficos (com JFreeChart).

---

## 📊 Resultados e Discussão

### Execução para palavra `the` em *Moby Dick*

```
SerialCPU [amostra 1]: 14727 ocorrências em 44 ms
SerialCPU [amostra 2]: 14727 ocorrências em 33 ms
SerialCPU [amostra 3]: 14727 ocorrências em 32 ms

ParallelCPU-1T [amostra 1]: 14727 ocorrências em 32 ms
ParallelCPU-1T [amostra 2]: 14727 ocorrências em 29 ms
ParallelCPU-1T [amostra 3]: 14727 ocorrências em 29 ms

ParallelCPU-2T [amostra 1]: 14727 ocorrências em 32 ms
ParallelCPU-2T [amostra 2]: 14727 ocorrências em 27 ms
ParallelCPU-2T [amostra 3]: 14727 ocorrências em 29 ms

ParallelCPU-4T [amostra 1]: 14727 ocorrências em 34 ms
ParallelCPU-4T [amostra 2]: 14727 ocorrências em 29 ms
ParallelCPU-4T [amostra 3]: 14727 ocorrências em 36 ms

ParallelCPU-8T [amostra 1]: 14727 ocorrências em 31 ms
ParallelCPU-8T [amostra 2]: 14727 ocorrências em 34 ms
ParallelCPU-8T [amostra 3]: 14727 ocorrências em 31 ms

ParallelCPU-12T [amostra 1]: 14727 ocorrências em 30 ms
ParallelCPU-12T [amostra 2]: 14727 ocorrências em 39 ms
ParallelCPU-12T [amostra 3]: 14727 ocorrências em 37 ms

ParallelCPU-16T [amostra 1]: 14727 ocorrências em 34 ms
ParallelCPU-16T [amostra 2]: 14727 ocorrências em 31 ms
ParallelCPU-16T [amostra 3]: 14727 ocorrências em 31 ms

ParallelCPU-24T [amostra 1]: 14727 ocorrências em 43 ms
ParallelCPU-24T [amostra 2]: 14727 ocorrências em 39 ms
ParallelCPU-24T [amostra 3]: 14727 ocorrências em 33 ms

ParallelGPU [amostra 1]: 14727 ocorrências em 4 ms
ParallelGPU [amostra 2]: 14727 ocorrências em 4 ms
ParallelGPU [amostra 3]: 14727 ocorrências em 4 ms
```

### 📈 Visualização Gráfica

O sistema apresenta automaticamente dois gráficos:

- **Gráfico de barras (médias)**: compara o tempo médio de cada método.
- **Gráfico de amostras individuais**: exibe a variação entre execuções para o mesmo método.

Os gráficos permitem visualizar com clareza a superioridade do processamento paralelo em GPU, além de oscilações no paralelismo em CPU com muitos threads.

---

## ✅ Conclusão

Os testes demonstram que:

- A **GPU é extremamente eficiente** para tarefas de busca simples e paralelizáveis.
- O **paralelismo com CPU** melhora o desempenho até certo ponto, mas pode gerar sobrecarga com muitos threads.
- A **versão serial** é útil como base comparativa, mas limitada para grandes volumes.

O trabalho reforça a importância de escolher a estratégia de paralelismo com base no tipo de aplicação, arquitetura da máquina e complexidade do problema.

---

## 📚 Referências

- Java Documentation: https://docs.oracle.com/en/java/
- JFreeChart: https://sourceforge.net/projects/jfreechart/
- JOCL: http://www.jocl.org

---

## 📦 Bibliotecas Utilizadas

- `jfreechart-1.5.3.jar`
- `jcommon-1.0.24.jar`
- `jocl-2.0.4.jar`

📁 Local: `/libs`  
📄 Configuração (VS Code):

```json
"java.project.referencedLibraries": [
    "libs/jfreechart-1.5.3.jar",
    "libs/jcommon-1.0.24.jar",
    "libs/jocl-2.0.4.jar"
]
```

---

## ▶️ Instruções para Execução

1. Abra o projeto em um IDE (recomenda-se VS Code).
2. Verifique se os arquivos `.jar` estão referenciados no `settings.json`.
3. Compile e execute a `Main.java`.
4. O programa rodará automaticamente os testes (modo automático) e abrirá a interface gráfica com os gráficos.
5. Também é possível executar manualmente pela GUI, escolhendo um arquivo `.txt` e palavra-chave.

---

## 📎 Anexos

**Códigos-fonte implementados:**
- `Main.java`
- `GuiMain.java`
- `SerialCounter.java`
- `ParallelCpuCounter.java`
- `ParallelGpuCounter.java`
- `TextCleaner.java`

**Link do Projeto no GitHub:**
> https://github.com/adamscastro/algoritmos-paralelos