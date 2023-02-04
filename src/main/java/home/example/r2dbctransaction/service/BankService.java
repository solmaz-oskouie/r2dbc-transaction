package home.example.r2dbctransaction.service;


import home.example.r2dbctransaction.configuration.transaction.R2dbcTransactionConfiguration;
import home.example.r2dbctransaction.dto.DepositRequest;
import home.example.r2dbctransaction.entity.MoneyDepositEvent;
import home.example.r2dbctransaction.exception.MyCustomException;
import home.example.r2dbctransaction.repository.AccountRepository;
import home.example.r2dbctransaction.repository.MoneyDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class BankService {


    private final AccountRepository accountRepository;

    private final MoneyDepositRepository eventRepository;

    private final R2dbcTransactionConfiguration r2dbcTransactionConfiguration;

    public BankService(AccountRepository accountRepository, MoneyDepositRepository eventRepository,R2dbcTransactionConfiguration r2dbcTransactionConfiguration) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
        this.r2dbcTransactionConfiguration = r2dbcTransactionConfiguration;
    }


    public Mono<Void> deposit(DepositRequest request) {
        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
        RollbackRuleAttribute rollbackRuleAttribute = new RollbackRuleAttribute(MyCustomException.class);
        transactionAttribute.setRollbackRules(List.of(rollbackRuleAttribute));
        transactionAttribute.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        transactionAttribute.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        TransactionalOperator transactionalOperator = r2dbcTransactionConfiguration.getTransactionOperator(transactionAttribute);

        return transactionalOperator.execute(tx -> this.accountRepository.findEntityById(request.getAccount())
                .doOnNext(ac -> ac.setBalance(ac.getBalance() + request.getAmount()))
                .flatMap(this.accountRepository::saveEntity)
                .thenReturn(toEvent(request))
                .flatMap(eventRepository::save)
        ).then();
    }

    public Mono<Void> depositWithRemoteServiceCall(DepositRequest request) {
        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
        RollbackRuleAttribute rollbackRuleAttribute = new RollbackRuleAttribute(MyCustomException.class);
        transactionAttribute.setRollbackRules(List.of(rollbackRuleAttribute));
        transactionAttribute.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        transactionAttribute.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        TransactionalOperator transactionalOperator = r2dbcTransactionConfiguration.getTransactionOperator(transactionAttribute);

        return transactionalOperator.execute(tx -> this.accountRepository.findEntityById(request.getAccount())
                .doOnNext(ac -> ac.setBalance(ac.getBalance() + request.getAmount()))
                .flatMap(this.accountRepository::saveEntity)
                .thenReturn(toEvent(request))
                .flatMap(eventRepository::save)
                .flatMap(moneyDepositEvent -> callRemoteService())
        ).then();
    }


    public Mono<Void> depositWithTooSlowRemoteServiceCall(DepositRequest request) {
        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
        RollbackRuleAttribute rollbackRuleAttribute = new RollbackRuleAttribute(MyCustomException.class);
        transactionAttribute.setRollbackRules(List.of(rollbackRuleAttribute));
        transactionAttribute.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        transactionAttribute.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        TransactionalOperator transactionalOperator = r2dbcTransactionConfiguration.getTransactionOperator(transactionAttribute);

        return transactionalOperator.execute(tx -> this.accountRepository.findEntityById(request.getAccount())
                .doOnNext(ac -> ac.setBalance(ac.getBalance() + request.getAmount()))
                .flatMap(this.accountRepository::saveEntity)
                .thenReturn(toEvent(request))
                .flatMap(eventRepository::save)
                .flatMap(moneyDepositEvent -> callTooSlowRemoteService())
        ).then();
    }




    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<Void> depositWithDeclarativeTransaction(DepositRequest request) {
        return  this.accountRepository.findEntityById(request.getAccount())
                .doOnNext(ac -> ac.setBalance(ac.getBalance() + request.getAmount()))
                .flatMap(this.accountRepository::saveEntity)
                .thenReturn(toEvent(request))
                .flatMap(eventRepository::save)
        .then();
    }


    @Transactional(isolation = Isolation.READ_COMMITTED,rollbackFor = MyCustomException.class)
    public Mono<Void> depositWithDeclarativeTransactionWithCallingRemoteService(DepositRequest request) {
        return  this.accountRepository.findEntityById(request.getAccount())
                .doOnNext(ac -> ac.setBalance(ac.getBalance() + request.getAmount()))
                .flatMap(this.accountRepository::saveEntity)
                .thenReturn(toEvent(request))
                .flatMap(eventRepository::save)
                .flatMap(moneyDepositEvent -> callRemoteService())
                .then();
    }


    // create money deposit event from request
    private MoneyDepositEvent toEvent(DepositRequest request) {
        return MoneyDepositEvent.create(
                null,
                request.getAccount(),
                request.getAmount()
        );
    }




   //call remote service
    Mono<Void>callRemoteService(){
        return Mono.error(new MyCustomException());

    }


    Mono<Void>callTooSlowRemoteService(){
        return Mono.just(30)
                .log()
                .delayElement(Duration.ofSeconds(2))
                .log()
                .then();

    }

}