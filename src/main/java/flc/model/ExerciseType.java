package flc.model;

/**
 * Enum representing the types of group exercise lessons offered by FLC.
 * Each type has a fixed price regardless of time slot or weekend.
 */
public enum ExerciseType {
    YOGA(10.00),
    ZUMBA(8.00),
    AQUACISE(9.00),
    BOX_FIT(12.00),
    BODY_BLITZ(11.00);

    private final double price;

    ExerciseType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    /** Returns a nicely formatted name, e.g. BOX_FIT -> "Box Fit" */
    public String getDisplayName() {
        String raw = name().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split(" ")) {
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1).toLowerCase())
              .append(" ");
        }
        return sb.toString().trim();
    }
}
