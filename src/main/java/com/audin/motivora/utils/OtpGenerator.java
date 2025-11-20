package com.audin.motivora.utils;

import java.security.SecureRandom;

public class OtpGenerator {
    /**
     * Génère un code OTP numérique cryptographiquement sûr de la longueur
     * spécifiée.
     *
     * @param length Le nombre de chiffres que l'OTP doit contenir (ex: 6).
     * @return Le code OTP généré sous forme de chaîne de caractères.
     * @throws IllegalArgumentException Si la longueur spécifiée est trop grande
     *                                  (max 18).
     */
    public static String generateOtp(int length) {
        if (length <= 0 || length > 18) {
            throw new IllegalArgumentException("La longueur doit être comprise entre 1 et 18.");
        }

        // 1. Déterminer la plage de nombres
        // Ex: pour length = 6, min = 100 000 et max = 999 999
        long max = (long) Math.pow(10, length);
        long min = (long) Math.pow(10, length - 1);
        long range = max - min; // La plage de nombres à générer (ex: 900 000)

        // 2. Utiliser SecureRandom
        // SecureRandom est préférable à Random pour la cryptographie et les secrets.
        SecureRandom random = new SecureRandom();

        // Génère un nombre aléatoire dans la plage [0, range)
        long randomValue = (long) (random.nextDouble() * range);

        // 3. Ajouter la valeur minimale pour s'assurer que le nombre a le bon nombre de
        // chiffres
        long otpNumber = min + randomValue;

        // 4. Formater pour garantir la bonne longueur
        // Cette étape est surtout une sécurité, car l'étape 3 garantit normalement la
        // longueur.
        String otpString = String.valueOf(otpNumber);

        // Si par sécurité le formatage est nécessaire (ex: 000123 si length=6)
        return String.format("%0" + length + "d", otpNumber);
    }
}
