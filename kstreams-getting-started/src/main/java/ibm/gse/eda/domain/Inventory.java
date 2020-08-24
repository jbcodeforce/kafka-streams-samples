package ibm.gse.eda.domain;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Inventory {
    public String itemID;
    public Integer quantity;

    public Inventory(){}
}