import enums.PaddleType;
import enums.Side;

public record PaddleEvent (
        PaddleType type,
        Side side,
        int frame
) {}