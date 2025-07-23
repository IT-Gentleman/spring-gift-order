package gift.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public class PageRequest {

    @PositiveOrZero
    int page = 0;

    @Min(1)
    @Max(100)
    int size = 10;

    List<String> sort = new ArrayList<>(); // ex. &sort=name,asc&sort=price,desc&sort=createdAt

    public Pageable toPageable(Set<String> allowedSortFields, String defaultSortField,
            Sort.Direction defaultSortDirection) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null) {
            for (String sortParam : sort) {
                String[] parts = sortParam.split(",");
                String field = parts[0];
                Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

                if (!allowedSortFields.contains(field)) {
                    throw new IllegalArgumentException("Invalid sort field: " + field);
                }
                orders.add(new Sort.Order(direction, field));
            }
        }
        if (orders.isEmpty()) {
            orders.add(new Order(defaultSortDirection, defaultSortField));
        }

        return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(orders));

    }


}
