package home.example.r2dbctransaction;

import com.sepantasolutions.utils.logs.LogUtil;
import com.sepantasolutions.utils.logs.Logger;
import home.example.r2dbctransaction.dto.DepositRequest;
import home.example.r2dbctransaction.entity.Account;
import home.example.r2dbctransaction.repository.AccountRepository;
import home.example.r2dbctransaction.repository.MoneyDepositRepository;
import home.example.r2dbctransaction.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class R2dbcTransactionApplicationTests {
    Logger logger = LogUtil.getLogger();
    @Autowired
    private BankService bankService;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MoneyDepositRepository moneyDepositRepository;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    public void initDB() {
        StepVerifier.create(moneyDepositRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        Flux<Account> accountFlux = accountRepository.findAll()
                .map(account -> {
                    account.setBalance(0);
                    return account;
                })
                .flatMap(updatedAccount -> accountRepository.save(updatedAccount));
        StepVerifier.create(accountFlux)
                .expectNextCount(4)
                .verifyComplete();
    }

    @DisplayName("""
            Deposit money successfully without calling remote service
            """)
    @Test
    void transactionSuccess() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono = bankService.deposit(request)
                .then(getAccountDetails(request));
        StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

    }





    @DisplayName("""
                        
            Deposit Money with calling remote service.
            Remote service throws a custom exception.
            """)
    @Test
    void transactionFailureWhenThrowCustomException() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono = bankService.depositWithRemoteServiceCall(request)
                .onErrorResume(ex -> {
                    logger.general().error("remote service calling went with an custom exception", ex);
                    return Mono.empty();
                })
                .then(getAccountDetails(request));
        StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 0)
                .verifyComplete();

    }





    @DisplayName("""
            Deposit money without calling remote service.
            depositing money less than 100 will result in DataIntegrityViolationException(DB Exception).
            """)
    @Test
    void transactionFailure() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono1 = bankService.deposit(request)
                .then(getAccountDetails(request));
        StepVerifier.create(mono1)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

        request = DepositRequest.create(1, 99);
        Mono<Account> mono2 = this.bankService.deposit(request)
                .onErrorResume(ex -> {
                    logger.general().error("amount of money is less than 100", ex);
                    return Mono.empty();
                })
                .then(getAccountDetails(request));
        StepVerifier.create(mono2)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

    }





    @DisplayName("""
            Deposit money successfully  without calling remote service (declarative transaction style)
            """)

    @Test
    void depositWithDeclarativeTransactionSuccess() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono = bankService.depositWithDeclarativeTransaction(request)
                .then(getAccountDetails(request));
        StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

    }




    @DisplayName("""
            Deposit money without calling remote service with declarative transaction.
            depositing money less than 100 will result in DataIntegrityViolationException(DB Exception).
            """)
    @Test
    void depositWithDeclarativeTransactionFailure(){
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono1 = bankService.depositWithDeclarativeTransaction(request)
                .then(getAccountDetails(request));
        StepVerifier.create(mono1)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

        request = DepositRequest.create(1, 99);
        Mono<Account> mono2 = this.bankService.depositWithDeclarativeTransaction(request)
                .onErrorResume(ex -> {
                    logger.general().error("amount of money is less than 100", ex);
                    return Mono.empty();
                })
                .then(getAccountDetails(request));
        StepVerifier.create(mono2)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();

    }




    @DisplayName("""
                        
            Deposit Money with calling remote service with declarative transaction.
            Remote service throws a custom exception.
            """)
    @Test
    void depositWithDeclarativeTransactionFailureWhenThrowCustomException() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono = bankService.depositWithDeclarativeTransactionWithCallingRemoteService(request)
                .onErrorResume(ex -> {
                    logger.general().error("remote service calling went with an custom exception", ex);
                    return Mono.empty();
                })
                .then(getAccountDetails(request));
        StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 0)
                .verifyComplete();

    }


    private Mono<Account> getAccountDetails(DepositRequest request) {
        return this.accountRepository.findById(request.getAccount())
                .doOnNext(System.out::println);
    }


}
