package gift.dto;

public record NewProductOptionCommand(
        String name,
        Integer quantity,
        Long productId
) {

    public static NewProductOptionCommand from(AddProductOptionRequest request) {
        return new NewProductOptionCommand(
                request.name(),
                request.quantity(),
                null
        );
    }

}
