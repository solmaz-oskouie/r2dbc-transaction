package home.example.r2dbctransaction.repository;


import home.example.r2dbctransaction.entity.Account;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import static org.springframework.data.relational.core.query.Criteria.where;

public class CustomAccountRepositoryImpl  implements CustomAccountRepository{

    private final R2dbcEntityTemplate template;
    private DatabaseClient databaseClient;

    private  final ConnectionFactory connectionFactory;

    public CustomAccountRepositoryImpl(R2dbcEntityTemplate template, ConnectionFactory connectionFactory) {
        this.template = template;

        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Account> saveEntity(Account account) {
     //return    this.template.update(account);
      return   this.template.getDatabaseClient().sql("update account set balance= :balance where id= :id")
                .bind("balance",account.getBalance())
                .bind("id",account.getId())
                .map(row -> {
                    Account ac=new Account();
                    ac.setId(  row.get("id",Integer.class));
                    ac.setUserName(  row.get("user_name",String.class));
                    ac.setBalance(  row.get("balance",Integer.class));
                    return ac;
                }).first();

    }

    @Override
    public Mono<Account> findEntityById(int id) {
     return    this.template.selectOne(Query.query(where("id").is(id)),Account.class);
 /*     return   this.databaseClient.sql("select * from account where id=:id")
                .bind("id",id)
                .map(row -> {
                    Account ac=new Account();
                    ac.setId(  row.get("id",Integer.class));
                    ac.setUserName(  row.get("user_name",String.class));
                    ac.setBalance(  row.get("balance",Integer.class));
                    return ac;
                }).first();*/

    }

/*    @Override
    public TransactionalOperator get(){
        this.databaseClient= DatabaseClient.create(connectionFactory);
        return TransactionalOperator
                .create(new R2dbcTransactionManager(connectionFactory));
    }*/
}
