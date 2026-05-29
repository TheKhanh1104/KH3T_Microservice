package fit.iuh.kh3tshopbe.product.pipeline;

import fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.shared.entity.Product;

/**
 * [PIPE & FILTER] — Context object truyền qua toàn bộ pipeline.
 *
 * Mỗi Filter nhận vào context này, đọc thông tin cần thiết,
 * xử lý và ghi kết quả vào context để filter tiếp theo dùng.
 *
 * Tương tự như "pipe" trong Unix: cat file | grep | sort | head
 * Ở đây: request | validate | enrich | persist
 */
public class ProductPipelineContext {

    private final ProductRequest request;
    private Product product;              // Product entity đang được build dần qua pipeline
    private boolean isUpdate;            // true = update, false = create
    private int productIdToUpdate;       // Chỉ dùng khi isUpdate = true

    public ProductPipelineContext(ProductRequest request) {
        this.request = request;
        this.isUpdate = false;
    }

    public ProductPipelineContext(ProductRequest request, int productIdToUpdate) {
        this.request = request;
        this.isUpdate = true;
        this.productIdToUpdate = productIdToUpdate;
    }

    public ProductRequest getRequest() { return request; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public boolean isUpdate() { return isUpdate; }
    public int getProductIdToUpdate() { return productIdToUpdate; }
}
