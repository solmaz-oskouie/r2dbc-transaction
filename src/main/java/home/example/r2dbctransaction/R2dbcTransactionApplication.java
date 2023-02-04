package home.example.r2dbctransaction;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class R2dbcTransactionApplication {



    public static void main(String[] args) {
        SpringApplication.run(R2dbcTransactionApplication.class, args);
    }




}
