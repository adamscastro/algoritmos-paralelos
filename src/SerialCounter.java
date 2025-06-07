public class SerialCounter {
    public static int countWord(String text, String word) {
        String[] words = text.split("\\W+");
        int count = 0;
        for (String w : words) {
            if (w.equalsIgnoreCase(word)) {
                count++;
            }
        }
        return count;
    }
}
