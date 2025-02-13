package com.ddib.product.product.service;

import com.ddib.product.product.domain.FavoriteProduct;
import com.ddib.product.product.domain.Product;
import com.ddib.product.product.domain.ProductDetail;
import com.ddib.product.product.dto.request.ProductCreateRequestDto;
import com.ddib.product.product.dto.request.ProductLikeRequestDto;
import com.ddib.product.product.dto.request.ProductStockDecreaseRequestDto;
import com.ddib.product.product.dto.request.ProductStockUpdateRequestDto;
import com.ddib.product.product.dto.response.ProductMainResponseDto;
import com.ddib.product.product.dto.response.ProductResponseDto;
import com.ddib.product.product.dto.response.ProductViewResponseDto;
import com.ddib.product.product.exception.ProductNotFoundException;
import com.ddib.product.product.repository.ProductRepository;
import com.ddib.product.product.repository.ProductRepositorySupport;
import com.ddib.product.user.domain.Seller;
import com.ddib.product.user.domain.User;
import com.ddib.product.user.exception.SellerNotFoundException;
import com.ddib.product.user.exception.UserNotFoundException;
import com.ddib.product.user.repository.SellerRepository;
import com.ddib.product.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final SellerRepository sellerRepository;

    private final ProductRepositorySupport productRepositorySupport;

    public void createProduct(List<MultipartFile> thumbnails, List<MultipartFile> details, ProductCreateRequestDto dto) {
        log.info("PRODUCT SERVICE : SAVE PRODUCT : {}", dto.getName());
        Seller seller = sellerRepository.findBySellerId(dto.getSellerId())
                .orElseThrow(SellerNotFoundException::new);

        Product product = dto.toEntity(seller);

        productRepository.save(product);
    }

    public void decreaseStock(ProductStockDecreaseRequestDto dto) {
        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        product.decreaseStock(dto.getAmount());
    }

    public void updateStock(ProductStockUpdateRequestDto dto) {
        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        product.updateStock(dto.getAmount());
    }

    public void likeProduct(ProductLikeRequestDto dto) {
        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(UserNotFoundException::new);

        FavoriteProduct favoriteProduct = FavoriteProduct.builder()
                .product(product)
                .user(user)
                .build();

        product.getLikedUsers().add(favoriteProduct);
        user.getLikedProducts().add(favoriteProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findFavoriteProductByUserId(int userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new)
                .getLikedProducts()
                .stream()
                .map(FavoriteProduct::getProduct)
                .map(ProductResponseDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsBySellerId(int sellerId) {
        return sellerRepository.findBySellerId(sellerId)
                .orElseThrow(SellerNotFoundException::new)
                .getProducts()
                .stream()
                .map(ProductResponseDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductViewResponseDto findProductByProductId(int productId, int userId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(ProductNotFoundException::new);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        boolean isLiked = user.getLikedProducts()
                .stream()
                .anyMatch(fp -> fp.getProduct().equals(product));
        return ProductViewResponseDto.of(product, isLiked);
    }

    public void cancelFavoriteProduct(int productId, int userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        Product product = productRepository.findByProductId(productId).orElseThrow(ProductNotFoundException::new);

        List<FavoriteProduct> fps = user.getLikedProducts();
        for (FavoriteProduct fp : fps) {
            if (fp.getProduct().equals(product)) {
                user.getLikedProducts().remove(fp);
                product.getLikedUsers().remove(fp);
                break;
            }
        }
    }
}

