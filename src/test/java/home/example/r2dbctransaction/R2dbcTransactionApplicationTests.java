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
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
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
        Account block = bankService.deposit(request)
                .then(getAccountDetails(request)).block();
        assert block!=null;
    /*    StepVerifier.create(mono)
                .expectNextMatches(
                        ac -> ac.getBalance() == 500)
                .verifyComplete();*/

    }

    @DisplayName("""
            Flux model:
            Deposit money successfully without calling remote service
            """)
    @Test
    void transactionSuccessFlux() {

        List<Account> accountList = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(100, 999)))
                .flatMap(request -> bankService.deposit(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))

                ).collectList().block();

        assert accountList != null;
        boolean allMatch = accountList.stream().map(Account::getBalance).allMatch(balance -> balance >0);
        assert allMatch==Boolean.TRUE;

    }


    @Test
    void depositWithTransactionFailureWhenThrowCustomExceptionFlux(){
        List<Account> accountList = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(100, 999)))
                .flatMap(request -> bankService.depositWithRemoteServiceCall(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))


                ).collectList().block();
        assert accountList != null;
        boolean allMatch = accountList.stream().map(Account::getBalance).allMatch(balance -> balance == 0);
        assert allMatch==Boolean.TRUE;


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
                        
            Deposit Money with calling remote service.
            Too slow remote service throws a custom exception.
            """)
    @Test
    void transactionFailureWhenThrowCustomExceptionWithTooSlowRemoteService() {
        DepositRequest request = DepositRequest.create(1, 500);
        Mono<Account> mono = bankService.depositWithTooSlowRemoteServiceCall(request)
                .onErrorResume(ex -> {
                    logger.general().error("remote service calling went with an custom exception", ex);
                    return Mono.empty();
                })
                .then(getAccountDetails(request));
        StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 500)
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
        Account account = bankService.depositWithDeclarativeTransaction(request)
                .then(getAccountDetails(request)).block();
        assert account!=null;
     /*   StepVerifier.create(mono)
                .expectNextMatches(ac -> ac.getBalance() == 500)
                .verifyComplete();*/

    }



    @DisplayName("""
            Flux model:
            Deposit money successfully  without calling remote service (declarative transaction style)
            """)

    @Test
    void depositWithDeclarativeTransactionSuccessFlux() {

        List<Account> accountList = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(100, 999)))
                .flatMap(request -> bankService.depositWithDeclarativeTransaction(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))

                ).collectList().block();

        assert accountList != null;
        boolean allMatch = accountList.stream().map(Account::getBalance).allMatch(balance -> balance >0);
        assert allMatch==Boolean.TRUE;

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
            Flux Model:           
            Deposit money without calling remote service with declarative transaction.
            depositing money less than 100 will result in DataIntegrityViolationException(DB Exception).
            """)

    @Test
    void depositWithDeclarativeTransactionFailureFlux(){

        List<Account> accountList = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(100, 999)))
                .flatMap(request -> bankService.depositWithDeclarativeTransaction(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))

                ).collectList().block();

        assert accountList != null;
        boolean allMatch = accountList.stream().map(Account::getBalance).allMatch(balance -> balance >0);
        assert allMatch==Boolean.TRUE;


        List<Account> accountList2 = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(0, 99)))
                .flatMap(request -> bankService.depositWithDeclarativeTransaction(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))

                ).collectList().block();

        assert accountList2 != null;
        accountList2.removeAll(accountList);
        assert accountList2.size()==0;
    }




    @DisplayName("""
                        
            Deposit Money with calling remote service with declarative transaction.
            Remote service throws a custom exception.
             deposit action is successful but remote service throws an exception.
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

    @DisplayName("""
            Flux model :          
            Deposit Money with calling remote service with declarative transaction.
            Remote service throws a custom exception.
            deposit action is successful but remote service throws an exception.
            """)

    @Test
    void depositWithDeclarativeTransactionFailureWhenThrowCustomExceptionFlux(){
        List<Account> accountList = Flux.range(1, 4)
                .map(i -> DepositRequest.create(i, ThreadLocalRandom.current().nextInt(100, 999)))
                .flatMap(request -> bankService.depositWithDeclarativeTransactionWithCallingRemoteService(request)
                        .onErrorResume(ex -> {
                            logger.general().error("remote service calling went with an custom exception", ex);
                            return Mono.empty();
                        })
                        .then(getAccountDetails(request))


                ).collectList().block();
        assert accountList != null;
        boolean allMatch = accountList.stream().map(Account::getBalance).allMatch(balance -> balance == 0);
        assert allMatch==Boolean.TRUE;


    }


    private Mono<Account> getAccountDetails(DepositRequest request) {
        return this.accountRepository.findById(request.getAccount())
                .doOnNext(System.out::println);
    }


}
