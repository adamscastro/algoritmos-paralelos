import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class GuiMain extends JFrame {

    private JTextField txtFilePath;
    private JButton btnBrowse;
    private JTextField txtWord;
    private JButton btnRun;
    private JTextArea outputArea;
    private ChartPanel chartPanelMedia;
    private ChartPanel chartPanelDetalhes;

    public GuiMain() {
        setTitle("Contagem Paralela de Palavras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        content.add(topPanel, BorderLayout.NORTH);

        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        txtFilePath = new JTextField();
        btnBrowse = new JButton("...");
        filePanel.add(new JLabel("Arquivo:"), BorderLayout.WEST);
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        JPanel wordPanel = new JPanel(new BorderLayout(5, 5));
        txtWord = new JTextField();
        wordPanel.add(new JLabel("Palavra:"), BorderLayout.WEST);
        wordPanel.add(txtWord, BorderLayout.CENTER);

        btnRun = new JButton("Executar");

        JPanel centerTop = new JPanel(new GridLayout(1, 2, 5, 5));
        centerTop.add(filePanel);
        centerTop.add(wordPanel);
        topPanel.add(centerTop, BorderLayout.CENTER);
        topPanel.add(btnRun, BorderLayout.EAST);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        content.add(mainSplit, BorderLayout.CENTER);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollOutput = new JScrollPane(outputArea);
        scrollOutput.setBorder(BorderFactory.createTitledBorder("Saída"));
        mainSplit.setTopComponent(scrollOutput);

        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartPanelMedia = new ChartPanel(null);
        chartPanelMedia.setBorder(BorderFactory.createTitledBorder("Média de Tempos"));

        chartPanelDetalhes = new ChartPanel(null);
        chartPanelDetalhes.setBorder(BorderFactory.createTitledBorder("Tempos por Amostra"));

        chartsPanel.add(chartPanelMedia);
        chartsPanel.add(chartPanelDetalhes);
        mainSplit.setBottomComponent(chartsPanel);
        mainSplit.setDividerLocation(250);

        btnBrowse.addActionListener(e -> chooseFile());
        btnRun.addActionListener(e -> runCounting());
        txtWord.addActionListener(e -> runCounting());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            txtFilePath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void runCounting() {
        String filePath = txtFilePath.getText().trim();
        String word = txtWord.getText().trim();

        if (filePath.isEmpty() || word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o arquivo e a palavra.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Files.exists(Path.of(filePath))) {
            JOptionPane.showMessageDialog(this, "Arquivo não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        outputArea.setText("");

        new Thread(() -> {
            try {
                String rawText = Files.readString(Path.of(filePath));
                String cleanedText = TextCleaner.clean(rawText);

                Map<String, Long> medias = new LinkedHashMap<>();
                Map<String, List<Long>> detalhes = new LinkedHashMap<>();
                List<String> logs = Main.runAllAndSave(filePath, cleanedText, word.toLowerCase(), false, medias, detalhes);

                for (String linha : logs) {
                    SwingUtilities.invokeLater(() -> outputArea.append(linha + "\n"));
                }

                SwingUtilities.invokeLater(() -> {
                    updateChartMedia(medias);
                    updateChartDetalhado(detalhes);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private void updateChartMedia(Map<String, Long> tempos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : tempos.entrySet()) {
            dataset.addValue(entry.getValue(), "Tempo médio (ms)", entry.getKey());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Média de Tempos por Método",
                "Método",
                "Tempo (ms)",
                dataset
        );
        chartPanelMedia.setChart(chart);
    }

    private void updateChartDetalhado(Map<String, List<Long>> detalhes) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, List<Long>> entry : detalhes.entrySet()) {
            String metodo = entry.getKey();
            List<Long> tempos = entry.getValue();
            for (int i = 0; i < tempos.size(); i++) {
                dataset.addValue(tempos.get(i), metodo, "Amostra " + (i + 1));
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Tempos por Amostra",
                "Amostras",
                "Tempo (ms)",
                dataset
        );
        chartPanelDetalhes.setChart(chart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiMain gui = new GuiMain();
            gui.setVisible(true);
        });
    }
}
