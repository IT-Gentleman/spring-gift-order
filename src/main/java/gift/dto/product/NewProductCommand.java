package gift.dto.product;

import java.util.List;

public record NewProductCommand(
        String name,
        Integer price,
        String imageUrl,
        List<NewProductOptionCommand> options
) {

}
