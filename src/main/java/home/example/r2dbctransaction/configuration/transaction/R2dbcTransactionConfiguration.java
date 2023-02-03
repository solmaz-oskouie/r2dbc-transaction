package home.example.r2dbctransaction.configuration.transaction;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;


@Configuration
public class R2dbcTransactionConfiguration {

    private final ReactiveTransactionManager reactiveTransactionManager;

    public R2dbcTransactionConfiguration(ReactiveTransactionManager reactiveTransactionManager) {
        this.reactiveTransactionManager = reactiveTransactionManager;
    }

    // ConnectionFactory--> ReactiveTransactionManager--------------------------->|
   //                                                                                TransactionalOperator.create(ReactiveTransactionManager,DefaultTransactionAttribute)
   //                      TransactionDefinition(DefaultTransactionAttribute)--->|





/*    @Bean
    public ConnectionFactory postgresConnectionFactory(@Value("${spring.r2dbc.url}") String url){
        return ConnectionFactories.get(url);
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory){
        return new R2dbcTransactionManager( connectionFactory);
    }*/


    public TransactionalOperator getTransactionOperator(TransactionDefinition transactionDefinition) {
        return TransactionalOperator.create(reactiveTransactionManager, transactionDefinition);

    }



}
