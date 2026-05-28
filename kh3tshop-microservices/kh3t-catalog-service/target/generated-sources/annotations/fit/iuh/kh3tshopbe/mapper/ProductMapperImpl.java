package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.request.CategoryRequest;
import fit.iuh.kh3tshopbe.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.entities.Category;
import fit.iuh.kh3tshopbe.entities.Product;
import fit.iuh.kh3tshopbe.entities.SizeDetail;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toProduct(ProductRequest productRequest) {
        if ( productRequest == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.category( categoryRequestToCategory( productRequest.getCategoryRequest() ) );
        product.sizeDetails( sizeDetailRequestListToSizeDetailList( productRequest.getSizeDetailRequests() ) );
        product.id( productRequest.getId() );
        product.name( productRequest.getName() );
        product.description( productRequest.getDescription() );
        product.price( productRequest.getPrice() );
        product.unit( productRequest.getUnit() );
        product.imageUrlFront( productRequest.getImageUrlFront() );
        product.imageUrlBack( productRequest.getImageUrlBack() );
        product.discountAmount( productRequest.getDiscountAmount() );
        product.material( productRequest.getMaterial() );
        product.form( productRequest.getForm() );

        return product.build();
    }

    @Override
    public ProductResponse toProductResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.category( categoryToCategoryResponse( product.getCategory() ) );
        productResponse.sizeDetails( sizeDetailListToSizeDetailResponseList( product.getSizeDetails() ) );
        productResponse.status( product.getStatus() );
        productResponse.id( product.getId() );
        productResponse.name( product.getName() );
        productResponse.description( product.getDescription() );
        productResponse.price( product.getPrice() );
        productResponse.costPrice( product.getCostPrice() );
        productResponse.unit( product.getUnit() );
        productResponse.quantity( product.getQuantity() );
        productResponse.imageUrlFront( product.getImageUrlFront() );
        productResponse.imageUrlBack( product.getImageUrlBack() );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.updatedAt( product.getUpdatedAt() );
        productResponse.rating( product.getRating() );
        productResponse.discountAmount( product.getDiscountAmount() );
        productResponse.material( product.getMaterial() );
        productResponse.form( product.getForm() );

        return productResponse.build();
    }

    protected Category categoryRequestToCategory(CategoryRequest categoryRequest) {
        if ( categoryRequest == null ) {
            return null;
        }

        Category category = new Category();

        category.setName( categoryRequest.getName() );
        category.setDescription( categoryRequest.getDescription() );
        category.setImageUrl( categoryRequest.getImageUrl() );
        category.setDisplay_order( categoryRequest.getDisplay_order() );
        category.setActive( categoryRequest.isActive() );

        return category;
    }

    protected SizeDetail sizeDetailRequestToSizeDetail(SizeDetailRequest sizeDetailRequest) {
        if ( sizeDetailRequest == null ) {
            return null;
        }

        SizeDetail sizeDetail = new SizeDetail();

        sizeDetail.setQuantity( sizeDetailRequest.getQuantity() );

        return sizeDetail;
    }

    protected List<SizeDetail> sizeDetailRequestListToSizeDetailList(List<SizeDetailRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<SizeDetail> list1 = new ArrayList<SizeDetail>( list.size() );
        for ( SizeDetailRequest sizeDetailRequest : list ) {
            list1.add( sizeDetailRequestToSizeDetail( sizeDetailRequest ) );
        }

        return list1;
    }

    protected CategoryResponse categoryToCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.imageUrl( category.getImageUrl() );

        return categoryResponse.build();
    }

    protected ProductResponse.SizeDetailResponse sizeDetailToSizeDetailResponse(SizeDetail sizeDetail) {
        if ( sizeDetail == null ) {
            return null;
        }

        ProductResponse.SizeDetailResponse.SizeDetailResponseBuilder sizeDetailResponse = ProductResponse.SizeDetailResponse.builder();

        sizeDetailResponse.id( sizeDetail.getId() );
        sizeDetailResponse.quantity( sizeDetail.getQuantity() );

        return sizeDetailResponse.build();
    }

    protected List<ProductResponse.SizeDetailResponse> sizeDetailListToSizeDetailResponseList(List<SizeDetail> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductResponse.SizeDetailResponse> list1 = new ArrayList<ProductResponse.SizeDetailResponse>( list.size() );
        for ( SizeDetail sizeDetail : list ) {
            list1.add( sizeDetailToSizeDetailResponse( sizeDetail ) );
        }

        return list1;
    }
}
