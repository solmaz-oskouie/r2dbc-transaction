package home.example.r2dbctransaction.repository;


import home.example.r2dbctransaction.entity.MoneyDepositEvent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyDepositRepository extends ReactiveCrudRepository<MoneyDepositEvent, Integer> {
}