package home.example.r2dbctransaction.repository;


import home.example.r2dbctransaction.entity.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Integer>,CustomAccountRepository {
}