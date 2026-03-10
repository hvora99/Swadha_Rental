package swadha.collection.rental;

public class BookingItem {

    private String itemNo;
    private long pickupMs;
    private long returnMs;
    private long washMs;
    private boolean requiresWash;

    public BookingItem(String itemNo,long pickupMs,long returnMs,long washMs,boolean requiresWash){
        this.itemNo = itemNo;
        this.pickupMs = pickupMs;
        this.returnMs = returnMs;
        this.washMs = washMs;
        this.requiresWash = requiresWash;
    }

    public String getItemNo(){ return itemNo; }

    public long getPickupMs(){ return pickupMs; }
    public long getReturnMs(){ return returnMs; }
    public long getWashMs(){ return washMs; }

    public boolean isRequiresWash(){ return requiresWash; }

    public void setPickupMs(long pickupMs){
        this.pickupMs = pickupMs;
    }

    public void setReturnMs(long returnMs){
        this.returnMs = returnMs;
    }

    public void setWashMs(long washMs){
        this.washMs = washMs;
    }
}