package fr.castello.census.exception;

/**
 * Levée lorsqu'une ressource demandée n'existe pas. Traduite en HTTP 404.
 *
 * <p>Sous-type de {@link FunctionalException} : les signatures {@code throws
 * FunctionalException} la couvrent, et le gestionnaire le plus spécifique
 * ({@code NotFoundException}) l'emporte pour renvoyer un 404.</p>
 */
public class NotFoundException extends FunctionalException {
    public NotFoundException(String message) {
        super(message);
    }
}
