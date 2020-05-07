package ibm.gse.eda.domain;

import java.time.Instant;

public class Transaction {
    public String id;
    public String cardNumber;
    public String merchandID;
    public String merchantType;
    public double amount;
    public long timestamp;

    public Transaction(){}

    public Transaction(String id, String cardNumber, String merchandID, String merchantType, double amount) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.merchandID = merchandID;
        this.merchantType = merchantType;
        this.amount = amount;
        this.timestamp = Instant.now().toEpochMilli();
    }

    
}