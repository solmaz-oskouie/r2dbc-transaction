package home.example.r2dbctransaction.repository;

import home.example.r2dbctransaction.entity.Account;
import reactor.core.publisher.Mono;

public interface CustomAccountRepository {
    Mono<Account>saveEntity(Account account);
    Mono<Account> findEntityById(int id);
   // TransactionalOperator get();
}
