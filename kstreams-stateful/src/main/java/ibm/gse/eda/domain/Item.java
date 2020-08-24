package ibm.gse.eda.domain;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Item {
    public Long id;
    public String storeName;
    public String itemCode;
    public int quantity;
    public Double price;
    public String timestamp;

    public Item(){}

	public Item(String store, String item, int quantity, double price) {
        this.storeName = store;
        this.itemCode = item;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now().toString();
	}
}