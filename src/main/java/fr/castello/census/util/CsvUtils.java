package fr.castello.census.util;

/**
 * Aides à la génération de contenu CSV (dialecte « français / Excel » : séparateur point-virgule).
 *
 * <p>Classe utilitaire sans état : elle ne dépend ni du web ni de la persistance, et reste
 * donc réutilisable par n'importe quelle couche.</p>
 */
public final class CsvUtils {

    /** Séparateur de colonnes. */
    public static final String SEPARATOR = ";";

    /** Fin de ligne CSV (RFC 4180). */
    public static final String LINE_END = "\r\n";

    private CsvUtils() {
        // Classe utilitaire : pas d'instanciation.
    }

    /**
     * Construit une ligne CSV à partir de valeurs, échappées si nécessaire.
     *
     * @param values valeurs des colonnes, dans l'ordre ({@code null} devient une cellule vide)
     * @return la ligne CSV, fin de ligne comprise
     */
    public static String line(Object... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(escape(values[i]));
        }
        return line.append(LINE_END).toString();
    }

    /**
     * Échappe une valeur : elle est entourée de guillemets si elle contient un séparateur,
     * un guillemet ou un saut de ligne (les guillemets internes étant doublés).
     *
     * @param value valeur à échapper (peut être {@code null})
     * @return la valeur prête à être insérée dans une ligne CSV
     */
    private static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(SEPARATOR) || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
