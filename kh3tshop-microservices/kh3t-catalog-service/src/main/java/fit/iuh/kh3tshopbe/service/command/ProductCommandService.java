package fit.iuh.kh3tshopbe.service.command;

import fit.iuh.kh3tshopbe.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.entities.Category;
import fit.iuh.kh3tshopbe.entities.Product;
import fit.iuh.kh3tshopbe.entities.Size;
import fit.iuh.kh3tshopbe.entities.SizeDetail;
import fit.iuh.kh3tshopbe.enums.Status;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.mapper.ProductMapper;
import fit.iuh.kh3tshopbe.repository.CategoryRepository;
import fit.iuh.kh3tshopbe.repository.ProductRepository;
import fit.iuh.kh3tshopbe.repository.SizeRepository;
import fit.iuh.kh3tshopbe.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .unit(productRequest.getUnit())
                .imageUrlFront(productRequest.getImageUrlFront())
                .imageUrlBack(productRequest.getImageUrlBack())
                .discountAmount(productRequest.getDiscountAmount())
                .material(productRequest.getMaterial())
                .form(productRequest.getForm())
                .build();

        Category category = categoryRepository.findByName(productRequest.getCategoryRequest().getName())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<SizeDetail> sizeDetails = buildSizeDetails(productRequest, product);
        int quantity = sizeDetails.stream().mapToInt(SizeDetail::getQuantity).sum();

        product.setCostPrice(productRequest.getPrice() - (productRequest.getPrice() * productRequest.getDiscountAmount() / 100));
        product.setRating(0.0);
        product.setQuantity(quantity);
        product.setSizeDetails(sizeDetails);
        product.setCategory(category);
        product.setBrand("HK3T");
        product.setStatus(Status.ACTIVE);
        product.setCreatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        product.setUpdatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        Product savedProduct = productRepository.save(product);
        productCacheService.evictProductsCache();
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(int id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setUnit(productRequest.getUnit());
        existingProduct.setImageUrlFront(productRequest.getImageUrlFront());
        existingProduct.setImageUrlBack(productRequest.getImageUrlBack());
        existingProduct.setDiscountAmount(productRequest.getDiscountAmount());
        existingProduct.setMaterial(productRequest.getMaterial());
        existingProduct.setForm(productRequest.getForm());
        existingProduct.setStatus(Status.ACTIVE);

        Category category = categoryRepository.findByName(productRequest.getCategoryRequest().getName())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        existingProduct.setCategory(category);
        existingProduct.setUpdatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        List<SizeDetail> sizeDetails = existingProduct.getSizeDetails();
        for (SizeDetail sd : sizeDetails) {
            for (SizeDetailRequest sdr : productRequest.getSizeDetailRequests()) {
                if (sd.getSize().getNameSize().equals(sdr.getSizeRequest().getNameSize())) {
                    sd.setQuantity(sdr.getQuantity());
                    break;
                }
            }
        }

        existingProduct.setQuantity(sizeDetails.stream().mapToInt(SizeDetail::getQuantity).sum());
        existingProduct.setCostPrice(productRequest.getPrice() - (productRequest.getPrice() * productRequest.getDiscountAmount() / 100));
        existingProduct.setBrand("HK3T");
        existingProduct.setStatus(Status.ACTIVE);

        Product updatedProduct = productRepository.save(existingProduct);
        productCacheService.evictProductsCache();
        return productMapper.toProductResponse(updatedProduct);
    }

    public void deleteProduct(int id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        existingProduct.setStatus(Status.INACTIVE);
        productRepository.save(existingProduct);
        productCacheService.evictProductsCache();
    }

    private List<SizeDetail> buildSizeDetails(ProductRequest productRequest, Product product) {
        List<SizeDetail> sizeDetails = new ArrayList<>();
        if (productRequest.getSizeDetailRequests() != null) {
            productRequest.getSizeDetailRequests().forEach(sizeDetailRequest -> {
                SizeDetail sizeDetail = new SizeDetail();
                Size size = sizeRepository.findByNameSize(sizeDetailRequest.getSizeRequest().getNameSize())
                        .orElseThrow(() -> new AppException(ErrorCode.UnknownError));
                sizeDetail.setSize(size);
                sizeDetail.setProduct(product);
                sizeDetail.setQuantity(sizeDetailRequest.getQuantity());
                sizeDetails.add(sizeDetail);
            });
        }
        return sizeDetails;
    }
}
