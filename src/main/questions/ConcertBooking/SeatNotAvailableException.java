package ConcertBooking;

public class SeatNotAvailableException  extends  RuntimeException{
    public  SeatNotAvailableException(String msg){
        super(msg);
    }
}
