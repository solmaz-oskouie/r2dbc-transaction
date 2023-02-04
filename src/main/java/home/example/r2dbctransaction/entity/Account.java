package home.example.r2dbctransaction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Data
@ToString
@EqualsAndHashCode
public class Account {
    @Id
    private Long id;
    private String userName;
    private Integer balance;
}
