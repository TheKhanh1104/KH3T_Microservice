package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse.SizeDetailResponse;
import fit.iuh.kh3tshopbe.dto.response.TopProductResponse;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    SizeRepository sizeRepository;
    ProductMapper productMapper;

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> convertToProductResponse(product, 0L))
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(int id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id);
        }
        Product product = optionalProduct.get();
        return convertToProductResponse(product, 0L);
    }

    private ProductResponse convertToProductResponse(Product product, Long soldQuantity) {
        List<SizeDetailResponse> sizeDetailResponses = product.getSizeDetails().stream()
                .map(sd -> SizeDetailResponse.builder()
                        .id(sd.getId())
                        .sizeName(sd.getSize().getNameSize().name())
                        .quantity(sd.getQuantity())
                        .build())
                .collect(Collectors.toList());
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .unit(product.getUnit())
                .quantity(product.getQuantity())
                .imageUrlFront(product.getImageUrlFront())
                .imageUrlBack(product.getImageUrlBack())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .rating(product.getRating())
                .discountAmount(product.getDiscountAmount())
                .material(product.getMaterial())
                .form(product.getForm())
                .soldQuantity(soldQuantity)
                .status(product.getStatus())
                .category(
                        CategoryResponse.builder()
                                .id(product.getCategory().getId())
                                .name(product.getCategory().getName())
                                .imageUrl(product.getCategory().getImageUrl())
                                .build()
                )
                .sizeDetails(sizeDetailResponses)
                .build();
    }

    public List<ProductResponse> getProductsByIds(List<Integer> ids) {
        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(product -> convertToProductResponse(product, 0L))
                .collect(Collectors.toList());
    }

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
        Category category = categoryRepository.findByName(productRequest.getCategoryRequest().getName()).orElseThrow(
                ()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        List<SizeDetail> sizeDetails = new ArrayList<>();
        if(sizeDetails!= null){
            productRequest.getSizeDetailRequests().forEach(sizeDetailRequest -> {
                SizeDetail sizeDetail = new SizeDetail();
                Size size = sizeRepository.findByNameSize(sizeDetailRequest.getSizeRequest().getNameSize()).orElseThrow(
                        ()-> new AppException(ErrorCode.UnknownError));
                sizeDetail.setSize(size);
                sizeDetail.setProduct(product);
                sizeDetail.setQuantity(sizeDetailRequest.getQuantity());
                sizeDetails.add(sizeDetail);
            });
        }
        int quantity = 0;
        for (SizeDetail sd : sizeDetails) {
            quantity += sd.getQuantity();
        }
        double costPrice = productRequest.getPrice() - (productRequest.getPrice() * productRequest.getDiscountAmount()/100);
        double rating = 0.0;
        product.setCostPrice(costPrice);
        product.setRating(rating);
        product.setQuantity(quantity);
        product.setSizeDetails(sizeDetails);
        product.setCategory(category);
        product.setBrand("HK3T");
        product.setStatus(Status.ACTIVE);
        product.setCreatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        product.setUpdatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(int id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
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

        Category category = categoryRepository.findByName(productRequest.getCategoryRequest().getName()).orElseThrow(
                ()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        existingProduct.setCategory(category);
        existingProduct.setUpdatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        List<SizeDetail> sizeDetails = existingProduct.getSizeDetails();
        List<SizeDetailRequest> requestedSizeDetails = productRequest.getSizeDetailRequests();
        for (SizeDetail sd : sizeDetails) {
            for (SizeDetailRequest sdr : requestedSizeDetails) {
                if (sd.getSize().getNameSize().equals(sdr.getSizeRequest().getNameSize())) {
                    sd.setQuantity(sdr.getQuantity());
                    break;
                }
            }
        }

        int quantity = 0;
        for (SizeDetail sd : sizeDetails) {
            quantity += sd.getQuantity();
        }
        double costPrice = productRequest.getPrice() - (productRequest.getPrice() * productRequest.getDiscountAmount()/100);
        existingProduct.setCostPrice(costPrice);
        existingProduct.setQuantity(quantity);
        existingProduct.setSizeDetails(sizeDetails);
        existingProduct.setCategory(category);
        existingProduct.setBrand("HK3T");
        existingProduct.setStatus(Status.ACTIVE);
        existingProduct.setUpdatedAt(Date.from(LocalDate.now().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()));

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    public void deleteProduct(int id) {
        Product existingProduct = productRepository.findById(id).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        existingProduct.setStatus(Status.INACTIVE);
        productRepository.save(existingProduct);
    }

    public List<Product> getSaleProducts() {
        return productRepository.findByDiscountAmountGreaterThan(0.1);
    }

    public List<TopProductResponse> getTopTrending(String type) {
        // Trả về danh sách rỗng vì dữ liệu Order được chuyển sang Order Microservice
        return Collections.emptyList();
    }

    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.getTotalProducts());
        stats.put("lowStock", productRepository.getLowStockProducts(10));
        return stats;
    }
}