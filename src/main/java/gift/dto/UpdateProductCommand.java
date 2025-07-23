package gift.dto;

public record UpdateProductCommand(
        Long id,
        String name,
        Integer price,
        String imageUrl
) {

}
