package gift.repository;

import gift.entity.ProductOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    boolean existsByNameAndProductId(String name, Long productId);

    Page<ProductOption> findAllByProductId(Long productId, Pageable pageable);
}