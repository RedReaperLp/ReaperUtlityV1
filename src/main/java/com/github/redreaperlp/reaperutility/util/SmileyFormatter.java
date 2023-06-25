package com.github.redreaperlp.reaperutility.util;

public class SmileyFormatter {
    public static String numbersToSmileys(int number) {
        String[] numbers = String.valueOf(number).split("");
        String smileys = "";
        for (String num : numbers) {
            switch (num) {
                case "0" -> smileys += "0️⃣";
                case "1" -> smileys += "1️⃣";
                case "2" -> smileys += "2️⃣";
                case "3" -> smileys += "3️⃣";
                case "4" -> smileys += "4️⃣";
                case "5" -> smileys += "5️⃣";
                case "6" -> smileys += "6️⃣";
                case "7" -> smileys += "7️⃣";
                case "8" -> smileys += "8️⃣";
                case "9" -> smileys += "9️⃣";
            }
        }
        return smileys;
    }
}
