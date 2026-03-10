package swadha.collection.rental;

public class HistoryBooking {

    private String timestamp;
    private String itemNo;
    private String name;
    private String phone;
    private String pickupDateTime;
    private String returnDateTime;

    private double totalRent;
    private double deposit;
    private double rentPaid;
    private double balance;

    private String status;
    private String actualPickup;
    private String actualReceive;

    private double refundAmount;


    public HistoryBooking(String timestamp,
                          String itemNo,
                          String name,
                          String phone,
                          String pickupDateTime,
                          String returnDateTime,
                          double totalRent,
                          double deposit,
                          double rentPaid,
                          double refundAmount,
                          String status,
                          String actualPickup,
                          String actualReceive) {

        this.timestamp = timestamp;
        this.itemNo = itemNo;
        this.name = name;
        this.phone = phone;
        this.pickupDateTime = pickupDateTime;
        this.returnDateTime = returnDateTime;
        this.totalRent = totalRent;
        this.deposit = deposit;
        this.rentPaid = rentPaid;
        this.refundAmount = refundAmount;
        this.status = status;
        this.actualPickup = actualPickup;
        this.actualReceive = actualReceive;
    }

    // Getters
    public String getTimestamp() { return timestamp; }
    public String getItemNo() { return itemNo; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPickupDateTime() { return pickupDateTime; }
    public String getReturnDateTime() { return returnDateTime; }
    public double getNetIncome() {
        return (deposit + rentPaid) - refundAmount;
    }
    public double getTotalRent() { return totalRent; }
    public double getDeposit() { return deposit; }
    public double getRentPaid() { return rentPaid; }
    public double getBalance() { return balance; }

    public String getStatus() { return status; }
    public String getActualPickup() { return actualPickup; }
    public String getActualReceive() { return actualReceive; }
}