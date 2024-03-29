package ibm.gse.eda.kstream.domain;

import java.util.Date;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Purchase {
    String customerId;
    String creditCardNumber;
    private String itemPurchased;
    private int quantity;
    private double price;
    private Date purchaseDate;
    private String zipCode;
    private long creationTime;

    private Purchase(Builder builder) {
        customerId = builder.customerId;
        creditCardNumber = builder.creditCardNumber;
        itemPurchased = builder.itemPurchased;
        quantity = builder.quantity;
        price = builder.price;
        purchaseDate = builder.purchaseDate;
        zipCode = builder.zipCode;
        creationTime = builder.creationTime;
    }
    
    public Purchase() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Purchase copy) {
        Builder builder = new Builder();
        builder.creditCardNumber = copy.creditCardNumber;
        builder.itemPurchased = copy.itemPurchased;
        builder.quantity = copy.quantity;
        builder.price = copy.price;
        builder.purchaseDate = copy.purchaseDate;
        builder.zipCode = copy.zipCode;
        builder.customerId = copy.customerId;
        builder.creationTime = copy.creationTime;
        return builder;
    }

    @Override
    public String toString() {
        return "Purchase{ 'customerId='" + customerId + '\'' +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                ", itemPurchased='" + itemPurchased + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", purchaseDate=" + purchaseDate +
                '}';
    }

    public String getCustomerId() {
        return customerId;
    }


    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }


    public String getCreditCardNumber() {
        return creditCardNumber;
    }


    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }


    public static final class Builder {
        private static final String CC_NUMBER_REPLACEMENT="xxxx-xxxx-xxxx-";
        private String customerId;
        private String creditCardNumber;
        private String itemPurchased;
        private int quantity;
        private double price;
        private Date purchaseDate;
        private String zipCode;
        private long creationTime;
        
        private Builder() {
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder maskCreditCard(){
            Objects.requireNonNull(this.creditCardNumber, "Credit Card can't be null");
            String[] parts = this.creditCardNumber.split("-");
            if (parts.length < 4 ) {
                this.creditCardNumber = "xxxx";
            } else {
                String last4Digits = this.creditCardNumber.split("-")[3];
                this.creditCardNumber = CC_NUMBER_REPLACEMENT + last4Digits;
            }
            return this;
        }

        public Purchase build() {
            return new Purchase(this);
        }
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getItemPurchased() {
        return this.itemPurchased;
    }

    public void setItemPurchased(String itemPurchased) {
        this.itemPurchased = itemPurchased;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getPurchaseDate() {
        return this.purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getZipCode() {
        return this.zipCode;
    }

    public void setZipCode(String zip) {
        this.zipCode = zip;
    }

}
