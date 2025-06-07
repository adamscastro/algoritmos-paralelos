import java.text.Normalizer;

public class TextCleaner {

    public static String clean(String text) {
        // 1. Minúsculas
        text = text.toLowerCase();

        // 2. Remover acentuação
        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                         .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 3. Remover pontuação e símbolos (preserva apenas letras e espaços)
        text = text.replaceAll("[^\\p{L}\\s]", " ");

        // 4. Substituir múltiplos espaços por um espaço só
        text = text.replaceAll("\\s+", " ");

        // 5. Remover espaços extras
        return text.trim();
    }
}
