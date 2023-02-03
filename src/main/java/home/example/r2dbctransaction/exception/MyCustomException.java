package home.example.r2dbctransaction.exception;

public class MyCustomException  extends Exception{


    public MyCustomException(){
        super();
    }
    public MyCustomException(String message, Throwable e){
        super(message,e);
    }
}
