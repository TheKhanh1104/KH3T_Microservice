package fit.iuh.kh3tshopbe.entities;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "wishlist_detail")
public class WishListDetail {
    @Id
    @Column(name = "wishlist_detail_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id")
    private WishList wishlist;

    @Column(name = "product_id")
    private int productId;

    public WishListDetail() {}

    public WishListDetail(int id, String note, Date created_at, WishList wishlist, int productId) {
        this.id = id;
        this.note = note;
        this.created_at = created_at;
        this.wishlist = wishlist;
        this.productId = productId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public WishList getWishlist() { return wishlist; }
    public void setWishlist(WishList wishlist) { this.wishlist = wishlist; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    // manual builder
    public static WishListDetailBuilder builder() {
        return new WishListDetailBuilder();
    }

    public static class WishListDetailBuilder {
        private int id;
        private String note;
        private Date created_at;
        private WishList wishlist;
        private int productId;

        public WishListDetailBuilder id(int id) { this.id = id; return this; }
        public WishListDetailBuilder note(String note) { this.note = note; return this; }
        public WishListDetailBuilder created_at(Date created_at) { this.created_at = created_at; return this; }
        public WishListDetailBuilder wishlist(WishList wishlist) { this.wishlist = wishlist; return this; }
        public WishListDetailBuilder productId(int productId) { this.productId = productId; return this; }

        public WishListDetail build() {
            return new WishListDetail(id, note, created_at, wishlist, productId);
        }
    }
}
